package com.reflex.tr.game.ibrh.ui.game

import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.absoluteValue
import kotlinx.coroutines.suspendCancellableCoroutine

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
        val seed = (weekKey + playerName + selectedMode.storageKey + selectedPeriod.name + refreshTick).hashCode().absoluteValue
        val names = listOf("Nova", "Blitz", "Pulse", "Echo", "Shadow", "NeonX", "Cyber", "ReflexPro", "TargetKing", "Matrix")
            .shuffledSeeded(seed)
            .take(7)
        val fakeEntries = names.mapIndexed { index, name ->
            val scoreOffset = ((seed / (index + 3)) % 18) - 8
            val anchor = when (index) {
                0 -> playerScore + 12
                1 -> playerScore + 6
                2 -> playerScore + 2
                3 -> playerScore - 3
                else -> playerScore - 8 - index * 2
            }
            val minimumScore = if (selectedPeriod == LeaderboardPeriod.Weekly) 2 else 4
            val score = maxOf(minimumScore, anchor + scoreOffset)
            LeaderboardEntry(
                rank = 0,
                name = name,
                score = score,
                theme = PlayerTheme.entries[(seed + index) % PlayerTheme.entries.size],
                rankTier = rankFor(score = score, level = 1 + score / 20)
            )
        }
        val playerEntry = LeaderboardEntry(
            rank = 0,
            name = playerName,
            score = playerScore,
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
                (it.score - playerScore + 1).coerceAtLeast(1)
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
    }

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
        val playerEntry = LeaderboardEntry(
            rank = 1,
            name = sanitizeFirestorePlayerName(playerName),
            score = playerScore.coerceAtLeast(0),
            theme = playerTheme,
            rankTier = playerRankTier,
            isPlayer = true
        )
        return buildSnapshot(
            entries = listOf(playerEntry),
            selectedMode = selectedMode,
            selectedPeriod = selectedPeriod,
            playerScore = playerScore,
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

        val cacheKey = "${selectedMode.leaderboardCollectionKey}_${selectedPeriod.name}"
        return runCatching {
            val uid = requireUserId()
            val querySnapshot = firestore.collection("leaderboards")
                .document(selectedMode.leaderboardCollectionKey)
                .collection("scores")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            val entries = querySnapshot.documents.mapIndexedNotNull { index, document ->
                val score = document.getLong("score")?.toInt() ?: return@mapIndexedNotNull null
                val name = sanitizeFirestorePlayerName(document.getString("playerName"))
                val level = document.getLong("level")?.toInt() ?: 1
                val theme = playerThemeFromStorageKey(document.getString("selectedTheme").orEmpty())
                LeaderboardEntry(
                    rank = index + 1,
                    name = name,
                    score = score,
                    theme = theme,
                    rankTier = rankFor(score = score, level = level),
                    isPlayer = document.id == uid || document.getString("uid") == uid
                )
            }.toMutableList()

            if (entries.none { it.isPlayer } && playerScore > 0) {
                entries += LeaderboardEntry(
                    rank = 0,
                    name = sanitizeFirestorePlayerName(playerName),
                    score = playerScore,
                    theme = playerTheme,
                    rankTier = playerRankTier,
                    isPlayer = true
                )
            }

            val rankedEntries = entries
                .sortedByDescending { it.score }
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
            Log.d(TAG, "Firestore leaderboard read success mode=${selectedMode.leaderboardCollectionKey} count=${rankedEntries.size}")
            val playerRank = rankedEntries.firstOrNull { it.isPlayer }?.rank ?: 0
            val snapshot = buildSnapshot(
                entries = rankedEntries,
                selectedMode = selectedMode,
                selectedPeriod = selectedPeriod,
                playerScore = playerScore,
                playerRank = playerRank,
                refreshTick = refreshTick,
                statusMessageRes = R.string.leaderboard_refreshed
            )
            lastSuccessfulSnapshots[cacheKey] = snapshot
            snapshot
        }.getOrElse { error ->
            FirebaseGameServices.recordNonFatal("Leaderboard refresh failed", error)
            val cached = lastSuccessfulSnapshots[cacheKey]
            if (cached != null) {
                cached.copy(
                    selectedMode = selectedMode,
                    selectedPeriod = selectedPeriod,
                    refreshedTick = refreshTick,
                    isOffline = true,
                    statusMessageRes = R.string.leaderboard_offline_cache
                )
            } else {
                getLocalLeaderboard(
                    playerName = playerName,
                    playerScore = playerScore,
                    playerTheme = playerTheme,
                    playerRankTier = playerRankTier,
                    selectedMode = selectedMode,
                    selectedPeriod = selectedPeriod,
                    refreshTick = refreshTick
                ).copy(
                    isOffline = true,
                    statusMessageRes = R.string.leaderboard_refresh_failed
                )
            }
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
            val document = firestore.collection("leaderboards")
                .document(mode.leaderboardCollectionKey)
                .collection("scores")
                .document(uid)
            val safeName = sanitizeFirestorePlayerName(playerName)
            val didWrite = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val remoteScore = snapshot.getLong("score")?.toInt() ?: -1
                if (score > remoteScore) {
                    val payload = mutableMapOf<String, Any>(
                        "uid" to uid,
                        "playerName" to safeName,
                        "score" to score.coerceAtLeast(0),
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
                        putString(FirebaseParam.ModeName.key, mode.leaderboardCollectionKey)
                        putInt(FirebaseParam.Score.key, score)
                    }
                )
                Log.d(TAG, "Firestore leaderboard score upload success mode=${mode.leaderboardCollectionKey} score=$score")
            }
            didWrite
        }.getOrElse { error ->
            FirebaseGameServices.logEvent(
                event = FirebaseEvent.LeaderboardUploadFailed,
                params = Bundle().apply {
                    putString(FirebaseParam.ModeName.key, mode.leaderboardCollectionKey)
                    putInt(FirebaseParam.Score.key, score)
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

fun rankFor(score: Int, level: Int): RankTier {
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

private val GameMode.leaderboardCollectionKey: String
    get() = when (this) {
        GameMode.Classic -> "classic"
        GameMode.MovingTarget -> "moving_target"
        GameMode.FakeTarget -> "fake_target"
        GameMode.ColorReflex -> "color_reflex"
    }

private fun sanitizeFirestorePlayerName(name: String?): String {
    return name.orEmpty().trim().take(12)
}

private fun playerThemeFromStorageKey(key: String): PlayerTheme {
    return PlayerTheme.entries.firstOrNull { it.storageKey == key } ?: PlayerTheme.NeonRed
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
