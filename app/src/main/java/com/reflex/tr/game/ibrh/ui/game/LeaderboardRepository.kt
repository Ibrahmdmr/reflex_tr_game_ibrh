package com.reflex.tr.game.ibrh.ui.game

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.absoluteValue

private const val DEFAULT_LEADERBOARD_PLAYER_NAME = "Oyuncu"

interface LeaderboardRepository {
    fun getLocalLeaderboard(
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int
    ): LeaderboardSnapshot

    suspend fun refreshLeaderboard(
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        playerLevel: Int,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int
    ): LeaderboardSnapshot

    suspend fun uploadScore(
        playerName: String,
        score: Int,
        level: Int,
        selectedTheme: PlayerTheme,
        mode: GameMode
    ): Boolean
}

class LocalLeaderboardRepository : LeaderboardRepository {
    override fun getLocalLeaderboard(
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int
    ): LeaderboardSnapshot {
        val weekKey = currentWeekKey()
        val safeName = sanitizeFirestorePlayerName(playerName)
        val safePlayerScore = sanitizeScore(playerScore)
        if (!BuildConfig.DEBUG) {
            return LeaderboardSnapshot(
                weekKey = weekKey,
                selectedMode = selectedMode,
                selectedPeriod = selectedPeriod,
                entries = emptyList(),
                playerRank = 0,
                refreshedTick = refreshTick,
                isLoading = false,
                isOffline = true
            )
        }
        val seed = (weekKey + safeName + selectedMode.storageKey + selectedPeriod.name + refreshTick).hashCode().absoluteValue
        val names = listOf("Nova", "Blitz", "Pulse", "Echo", "Shadow", "NeonX", "Cyber", "ReflexPro", "TargetKing", "Matrix")
            .shuffledSeeded(seed)
            .take(7)
        val fakeEntries = names.mapIndexed { index, name ->
            val scoreOffset = ((seed / (index + 3)) % 18) - 8
            val anchor = when (index) {
                0 -> safePlayerScore + 12
                1 -> safePlayerScore + 6
                2 -> safePlayerScore + 2
                3 -> safePlayerScore - 3
                else -> safePlayerScore - 8 - index * 2
            }
            val minimumScore = if (selectedPeriod == LeaderboardPeriod.Weekly) 2 else 4
            val score = maxOf(minimumScore, anchor + scoreOffset)
            LeaderboardEntry(
                rank = 0,
                name = name,
                score = score,
                theme = PlayerTheme.entries[(seed + index) % PlayerTheme.entries.size],
                rankTier = rankFor(level = 1 + score / 20)
            )
        }
        val playerEntry = LeaderboardEntry(
            rank = 0,
            name = safeName,
            score = safePlayerScore,
            theme = playerTheme,
            rankTier = playerRankTier,
            isPlayer = true
        )
        val rankedEntries = (fakeEntries + playerEntry)
            .sortedByDescending { it.score }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        val playerRank = rankedEntries.firstOrNull { it.isPlayer }?.rank ?: rankedEntries.size
        val nextOpponent = rankedEntries
            .filter { !it.isPlayer && it.rank < playerRank }
            .minByOrNull { it.rank }
        val motivationRes = when {
            playerRank <= 3 -> R.string.leaderboard_motivation_top3
            nextOpponent != null -> R.string.leaderboard_motivation_pass_player
            else -> R.string.leaderboard_motivation_default
        }
        return LeaderboardSnapshot(
            weekKey = weekKey,
            selectedMode = selectedMode,
            selectedPeriod = selectedPeriod,
            entries = rankedEntries.take(8),
            playerRank = playerRank,
            motivationRes = motivationRes,
            motivationPlayerName = nextOpponent?.name.orEmpty(),
            motivationScoreGap = nextOpponent?.let {
                (it.score - safePlayerScore + 1).coerceAtLeast(1)
            } ?: 0,
            refreshedTick = refreshTick
        )
    }

    override suspend fun refreshLeaderboard(
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        playerLevel: Int,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int
    ): LeaderboardSnapshot {
        return getLocalLeaderboard(
            playerName = playerName,
            playerScore = playerScore,
            playerTheme = playerTheme,
            playerRankTier = playerRankTier,
            selectedMode = selectedMode,
            selectedPeriod = selectedPeriod,
            refreshTick = refreshTick
        )
    }

    override suspend fun uploadScore(
        playerName: String,
        score: Int,
        level: Int,
        selectedTheme: PlayerTheme,
        mode: GameMode
    ): Boolean = false

    private fun currentWeekKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"
    }
}

class FirestoreLeaderboardRepository(
    private val localFallback: LeaderboardRepository = LocalLeaderboardRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LeaderboardRepository {

    private companion object {
        const val TAG = "LeaderboardRepository"
        const val ROOT_COLLECTION = "leaderboards"
        const val SCORES_COLLECTION = "scores"
    }

    private val refreshMutex = Mutex()
    private val lastSuccessfulSnapshots = mutableMapOf<String, LeaderboardSnapshot>()

    override fun getLocalLeaderboard(
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int
    ): LeaderboardSnapshot {
        val safeScore = sanitizeScore(playerScore)
        val playerEntry = LeaderboardEntry(
            rank = 1,
            name = sanitizeFirestorePlayerName(playerName),
            score = safeScore,
            theme = playerTheme,
            rankTier = playerRankTier,
            isPlayer = true
        )
        return buildSnapshot(
            entries = listOf(playerEntry),
            selectedMode = selectedMode,
            selectedPeriod = selectedPeriod,
            playerScore = safeScore,
            playerRank = 1,
            refreshTick = refreshTick,
            statusMessageRes = null
        )
    }

    override suspend fun refreshLeaderboard(
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        playerLevel: Int,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int
    ): LeaderboardSnapshot {
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.LeaderboardRefreshed,
            params = Bundle().apply {
                putString(FirebaseParam.ModeName.key, selectedMode.leaderboardCollectionKey)
                putString(FirebaseParam.Period.key, selectedPeriod.name.lowercase())
            }
        )

        val modeKey = selectedMode.leaderboardCollectionKey
        val cacheKey = leaderboardCacheKey(selectedMode, selectedPeriod)
        if (!refreshMutex.tryLock()) {
            return cachedOrEmptySnapshot(
                cacheKey = cacheKey,
                playerName = playerName,
                playerScore = playerScore,
                playerTheme = playerTheme,
                playerRankTier = playerRankTier,
                selectedMode = selectedMode,
                selectedPeriod = selectedPeriod,
                refreshTick = refreshTick,
                statusMessageRes = R.string.leaderboard_loading
            )
        }

        return try {
            runCatching {
                val uid = requireUserId()
                val querySnapshot = scoresCollection(modeKey)
                    .orderBy("score", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()

                val entries = querySnapshot.documents.mapIndexedNotNull { index, document ->
                    val score = sanitizeScore(document.getLong("score")?.toInt() ?: return@mapIndexedNotNull null)
                    val name = sanitizeFirestorePlayerName(document.getString("playerName"))
                    val level = document.getLong("level")?.toInt() ?: 1
                    val theme = playerThemeFromStorageKey(document.getString("selectedTheme").orEmpty())
                    LeaderboardEntry(
                        rank = index + 1,
                        name = name,
                        score = score,
                        theme = theme,
                        rankTier = rankFor(level = level),
                        isPlayer = document.id == uid || document.getString("uid") == uid
                    )
                }.toMutableList()

                val safePlayerScore = sanitizeScore(playerScore)
                if (entries.none { it.isPlayer } && safePlayerScore > 0) {
                    entries += LeaderboardEntry(
                        rank = 0,
                        name = sanitizeFirestorePlayerName(playerName),
                        score = safePlayerScore,
                        theme = playerTheme,
                        rankTier = playerRankTier,
                        isPlayer = true
                    )
                }

                val rankedEntries = entries
                    .sortedByDescending { it.score }
                    .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
                logDebug("Firestore leaderboard read success mode=$modeKey count=${rankedEntries.size}")
                val playerRank = rankedEntries.firstOrNull { it.isPlayer }?.rank ?: 0
                val snapshot = buildSnapshot(
                    entries = rankedEntries,
                    selectedMode = selectedMode,
                    selectedPeriod = selectedPeriod,
                    playerScore = safePlayerScore,
                    playerRank = playerRank,
                    refreshTick = refreshTick,
                    statusMessageRes = R.string.leaderboard_refreshed
                )
                lastSuccessfulSnapshots[cacheKey] = snapshot
                snapshot
            }.getOrElse { error ->
                FirebaseGameServices.recordNonFatal("Leaderboard refresh failed", error)
                cachedOrEmptySnapshot(
                    cacheKey = cacheKey,
                    playerName = playerName,
                    playerScore = playerScore,
                    playerTheme = playerTheme,
                    playerRankTier = playerRankTier,
                    selectedMode = selectedMode,
                    selectedPeriod = selectedPeriod,
                    refreshTick = refreshTick,
                    statusMessageRes = R.string.leaderboard_refresh_failed
                )
            }
        } finally {
            refreshMutex.unlock()
        }
    }

    override suspend fun uploadScore(
        playerName: String,
        score: Int,
        level: Int,
        selectedTheme: PlayerTheme,
        mode: GameMode
    ): Boolean {
        return runCatching {
            val uid = requireUserId()
            val modeKey = mode.leaderboardCollectionKey
            val safeScore = sanitizeScore(score)
            val document = scoresCollection(modeKey).document(uid)
            val safeName = sanitizeFirestorePlayerName(playerName)
            val didWrite = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val remoteScore = sanitizeScore(snapshot.getLong("score")?.toInt() ?: -1)
                if (!snapshot.exists() || safeScore > remoteScore) {
                    val payload = mutableMapOf<String, Any>(
                        "uid" to uid,
                        "playerName" to safeName,
                        "score" to safeScore,
                        "level" to level.coerceAtLeast(1),
                        "selectedTheme" to selectedTheme.storageKey,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    if (!snapshot.exists()) {
                        payload["createdAt"] = FieldValue.serverTimestamp()
                    }
                    transaction.set(document, payload, com.google.firebase.firestore.SetOptions.merge())
                    true
                } else {
                    false
                }
            }.await()
            if (didWrite) {
                FirebaseGameServices.logEvent(
                    event = FirebaseEvent.LeaderboardScoreUpload,
                    params = Bundle().apply {
                        putString(FirebaseParam.ModeName.key, modeKey)
                        putInt(FirebaseParam.Score.key, safeScore)
                    }
                )
                logDebug("Firestore leaderboard score upload success mode=$modeKey score=$safeScore")
            }
            didWrite
        }.getOrElse { error ->
            FirebaseGameServices.logEvent(
                event = FirebaseEvent.LeaderboardUploadFailed,
                params = Bundle().apply {
                    putString(FirebaseParam.ModeName.key, mode.leaderboardCollectionKey)
                    putInt(FirebaseParam.Score.key, sanitizeScore(score))
                }
            )
            FirebaseGameServices.recordNonFatal("Leaderboard score upload failed", error)
            false
        }
    }

    private suspend fun requireUserId(): String {
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }?.let { return it }
        val result = auth.signInAnonymously().await()
        return result.user?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Anonymous Auth returned blank uid")
    }

    private fun scoresCollection(modeKey: String) = firestore.collection(ROOT_COLLECTION)
        .document(modeKey)
        .collection(SCORES_COLLECTION)

    private fun leaderboardCacheKey(
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod
    ): String {
        return "${selectedMode.leaderboardCollectionKey}_${selectedPeriod.name}"
    }

    private fun cachedOrEmptySnapshot(
        cacheKey: String,
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int,
        statusMessageRes: Int
    ): LeaderboardSnapshot {
        val cached = lastSuccessfulSnapshots[cacheKey]
        if (cached != null) {
            return cached.copy(
                selectedMode = selectedMode,
                selectedPeriod = selectedPeriod,
                refreshedTick = refreshTick,
                isLoading = false,
                isOffline = true,
                statusMessageRes = R.string.leaderboard_offline_cache
            )
        }

        val localSnapshot = localFallback.getLocalLeaderboard(
            playerName = playerName,
            playerScore = playerScore,
            playerTheme = playerTheme,
            playerRankTier = playerRankTier,
            selectedMode = selectedMode,
            selectedPeriod = selectedPeriod,
            refreshTick = refreshTick
        )
        return localSnapshot.copy(
            entries = emptyList(),
            playerRank = 0,
            isLoading = false,
            isOffline = true,
            statusMessageRes = statusMessageRes
        )
    }

    private fun buildSnapshot(
        entries: List<LeaderboardEntry>,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        playerScore: Int,
        playerRank: Int,
        refreshTick: Int,
        statusMessageRes: Int?
    ): LeaderboardSnapshot {
        val nextOpponent = entries
            .filter { !it.isPlayer && it.rank < playerRank }
            .minByOrNull { it.rank }
        val motivationRes = when {
            playerRank in 1..3 -> R.string.leaderboard_motivation_top3
            nextOpponent != null -> R.string.leaderboard_motivation_pass_player
            else -> R.string.leaderboard_motivation_default
        }
        return LeaderboardSnapshot(
            weekKey = currentWeekKey(),
            selectedMode = selectedMode,
            selectedPeriod = selectedPeriod,
            entries = entries,
            playerRank = playerRank,
            motivationRes = motivationRes,
            motivationPlayerName = nextOpponent?.name.orEmpty(),
            motivationScoreGap = nextOpponent?.let {
                (it.score - playerScore + 1).coerceAtLeast(1)
            } ?: 0,
            refreshedTick = refreshTick,
            statusMessageRes = statusMessageRes
        )
    }

    private fun currentWeekKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"
    }
}

/** Rank is derived from the player level alone; score does not affect the tier. */
fun rankFor(level: Int): RankTier {
    val normalizedLevel = level.coerceAtLeast(1)
    return when {
        normalizedLevel >= 40 -> RankTier.ReflexGod
        normalizedLevel >= 25 -> RankTier.NeonMaster
        normalizedLevel >= 15 -> RankTier.Platinum
        normalizedLevel >= 10 -> RankTier.Gold
        normalizedLevel >= 5 -> RankTier.Silver
        else -> RankTier.Bronze
    }
}

private fun <T> List<T>.shuffledSeeded(seed: Int): List<T> {
    return mapIndexed { index, item -> ((seed / (index + 1)) + index * 31) to item }
        .sortedBy { it.first }
        .map { it.second }
}

private val LeaderboardModeKeys = mapOf(
    GameMode.Classic to "classic",
    GameMode.MovingTarget to "moving_target",
    GameMode.FakeTarget to "fake_target",
    GameMode.ColorReflex to "color_reflex"
)

private val GameMode.leaderboardCollectionKey: String
    get() = LeaderboardModeKeys[this] ?: "classic"

private fun sanitizeFirestorePlayerName(name: String?): String {
    return name.orEmpty()
        .trim()
        .take(12)
        .ifBlank { DEFAULT_LEADERBOARD_PLAYER_NAME }
}

private fun sanitizeScore(score: Int): Int {
    return score.coerceAtLeast(0)
}

private fun playerThemeFromStorageKey(key: String): PlayerTheme {
    return PlayerTheme.entries.firstOrNull { it.storageKey == key } ?: PlayerTheme.NeonRed
}

private fun logDebug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TAG, message)
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { error ->
            continuation.resumeWithException(error)
        }
    }
}
