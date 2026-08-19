package com.reflex.tr.game.ibrh.share

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/** Outcome of a share attempt, so the caller can pick the right message. */
enum class ScoreShareResult {
    /** Share sheet opened with the rendered card attached. */
    Launched,

    /** Share sheet opened, but the card could not be produced, so only the text went out. */
    TextOnly,

    /** Nothing opened — no app could handle the intent. */
    Failed
}

/**
 * Renders the score card, publishes it through [FileProvider] and opens the share sheet. A card
 * that cannot be drawn or written degrades to a plain text share.
 */
object ScoreShareManager {

    private const val CARD_DIRECTORY = "share_cards"
    private const val CARD_PREFIX = "reflex_score_"
    private const val CARD_SUFFIX = ".png"
    private const val AUTHORITY_SUFFIX = ".fileprovider"
    private const val CHOSEN_ACTION = "com.reflex.tr.game.ibrh.SHARE_TARGET_CHOSEN"

    /** A receiver left behind by an abandoned share sheet is dropped after this. */
    private const val CHOOSER_CALLBACK_TIMEOUT_MS = 5 * 60 * 1000L

    /**
     * Draws and stores the card off the main thread. Returns null when the bitmap, the cache file
     * or the provider lookup fails.
     */
    suspend fun prepareCard(context: Context, data: ScoreShareData): Uri? =
        withContext(Dispatchers.Default) {
            val appContext = context.applicationContext
            val bitmap = ScoreShareCardGenerator.generate(data) ?: return@withContext null
            try {
                val directory = File(appContext.cacheDir, CARD_DIRECTORY)
                if (!directory.exists() && !directory.mkdirs()) return@withContext null
                // A fresh name per share: the previous file may still be open in a share sheet the
                // player left standing, and overwriting it would swap the image under them.
                directory.listFiles()?.forEach { it.delete() }
                val file = File(directory, CARD_PREFIX + System.currentTimeMillis() + CARD_SUFFIX)
                FileOutputStream(file).use { output ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        return@withContext null
                    }
                }
                FileProvider.getUriForFile(
                    appContext,
                    appContext.packageName + AUTHORITY_SUFFIX,
                    file
                )
            } catch (error: Exception) {
                null
            } catch (error: OutOfMemoryError) {
                null
            } finally {
                bitmap.recycle()
            }
        }

    /**
     * Opens the system share sheet. Main thread only. [onTargetChosen] fires only once the player
     * picks an app — a dismissed sheet reports nothing, so a reward is not paid for it.
     */
    fun launchShareSheet(
        context: Context,
        imageUri: Uri?,
        message: String,
        chooserTitle: String,
        onTargetChosen: () -> Unit = {}
    ): ScoreShareResult {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            if (imageUri != null) {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, message)
        }

        val chooser = createChooser(context, sendIntent, chooserTitle, onTargetChosen).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (imageUri != null) addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val launched = runCatching { context.startActivity(chooser) }.isSuccess
        return when {
            !launched -> ScoreShareResult.Failed
            imageUri == null -> ScoreShareResult.TextOnly
            else -> ScoreShareResult.Launched
        }
    }

    /** Falls back to a plain chooser when the callback cannot be registered. */
    private fun createChooser(
        context: Context,
        sendIntent: Intent,
        chooserTitle: String,
        onTargetChosen: () -> Unit
    ): Intent {
        val sender = runCatching { registerChosenTargetCallback(context, onTargetChosen) }.getOrNull()
        return if (sender != null) {
            Intent.createChooser(sendIntent, chooserTitle, sender)
        } else {
            Intent.createChooser(sendIntent, chooserTitle)
        }
    }

    private fun registerChosenTargetCallback(
        context: Context,
        onTargetChosen: () -> Unit
    ): android.content.IntentSender {
        val appContext = context.applicationContext
        val action = CHOSEN_ACTION + ":" + System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                unregister(appContext, this)
                onTargetChosen()
            }
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(action),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        // The chooser never reports a dismissal, so without this a cancelled share would leave the
        // receiver registered for the life of the process.
        handler.postDelayed({ unregister(appContext, receiver) }, CHOOSER_CALLBACK_TIMEOUT_MS)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        return PendingIntent.getBroadcast(
            appContext,
            0,
            Intent(action).setPackage(appContext.packageName),
            flags
        ).intentSender
    }

    private fun unregister(context: Context, receiver: BroadcastReceiver) {
        // Already-unregistered receivers throw; the timeout and the callback race by design.
        runCatching { context.unregisterReceiver(receiver) }
    }
}
