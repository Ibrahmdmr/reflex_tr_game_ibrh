package com.reflex.tr.game.ibrh.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.reflex.tr.game.ibrh.MainActivity
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import com.reflex.tr.game.ibrh.ui.game.GamePreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val GAME_PREFERENCES_NAME = "game_preferences"
private const val NOTIFICATION_DAILY_REWARD_KEY = "notification_daily_reward"
private const val NOTIFICATION_STREAK_KEY = "notification_streak"
private const val NOTIFICATION_NEW_MISSION_KEY = "notification_new_mission"
private const val NOTIFICATION_SCHEDULE_DATE_KEY = "notification_schedule_date"
private const val NOTIFICATION_SCHEDULE_MASK_KEY = "notification_schedule_mask"
private const val NOTIFICATION_SENT_DATE_KEY = "notification_sent_date"
private const val NOTIFICATION_SENT_COUNT_KEY = "notification_sent_count"
private const val NOTIFICATION_CHANNEL_ID = "reflex_reminders"
private const val NOTIFICATION_WINDOW_MILLIS = 30 * 60 * 1000L
private const val MAX_DAILY_NOTIFICATIONS = 2

enum class LocalNotificationType(
    val requestCode: Int,
    val notificationId: Int,
    val bit: Int,
    val analyticsName: String
) {
    DailyReward(8101, 9101, 1, "daily_reward"),
    StreakRisk(8102, 9102, 2, "streak_risk"),
    Mission(8103, 9103, 4, "mission_reminder")
}

object LocalNotificationScheduler {

    fun sync(context: Context, force: Boolean = false) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(GAME_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val enabledTypes = buildList {
            if (prefs.getBoolean(NOTIFICATION_DAILY_REWARD_KEY, false)) add(LocalNotificationType.DailyReward)
            if (prefs.getBoolean(NOTIFICATION_STREAK_KEY, false)) add(LocalNotificationType.StreakRisk)
            if (prefs.getBoolean(NOTIFICATION_NEW_MISSION_KEY, false)) add(LocalNotificationType.Mission)
        }.take(MAX_DAILY_NOTIFICATIONS)

        if (!hasNotificationPermission(appContext) || enabledTypes.isEmpty()) {
            cancelAll(appContext, logAnalytics = enabledTypes.isNotEmpty())
            return
        }

        val targetTime = nextReminderBaseTime()
        val targetDate = dateKey(targetTime)
        val mask = enabledTypes.fold(0) { acc, type -> acc or type.bit }
        if (!force &&
            prefs.getString(NOTIFICATION_SCHEDULE_DATE_KEY, "") == targetDate &&
            prefs.getInt(NOTIFICATION_SCHEDULE_MASK_KEY, 0) == mask
        ) {
            return
        }

        cancelAll(appContext, logAnalytics = false)
        enabledTypes.forEachIndexed { index, type ->
            schedule(appContext, type, targetTime + index * NOTIFICATION_WINDOW_MILLIS)
        }
        prefs.edit()
            .putString(NOTIFICATION_SCHEDULE_DATE_KEY, targetDate)
            .putInt(NOTIFICATION_SCHEDULE_MASK_KEY, mask)
            .apply()
    }

    fun cancelAll(context: Context, logAnalytics: Boolean = true) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        LocalNotificationType.entries.forEach { type ->
            alarmManager.cancel(pendingIntent(context, type))
            if (logAnalytics) {
                logNotificationEvent(FirebaseEvent.NotificationCancelled, type)
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun schedule(context: Context, type: LocalNotificationType, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            NOTIFICATION_WINDOW_MILLIS,
            pendingIntent(context, type)
        )
        logNotificationEvent(FirebaseEvent.NotificationScheduled, type)
    }

    private fun pendingIntent(context: Context, type: LocalNotificationType): PendingIntent {
        val intent = Intent(context, LocalNotificationReceiver::class.java).apply {
            action = "com.reflex.tr.game.ibrh.NOTIFICATION_${type.name}"
            putExtra(LocalNotificationReceiver.EXTRA_TYPE, type.name)
        }
        return PendingIntent.getBroadcast(
            context,
            type.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextReminderBaseTime(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }
}

class LocalNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE)
            ?.let { runCatching { LocalNotificationType.valueOf(it) }.getOrNull() }
            ?: return
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(GAME_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (!LocalNotificationScheduler.hasNotificationPermission(appContext)) return
        if (!isEnabled(prefs, type) || !canSendToday(prefs)) {
            LocalNotificationScheduler.sync(appContext, force = true)
            return
        }

        val gamePreferences = GamePreferences(appContext)
        val shouldSend = when (type) {
            LocalNotificationType.DailyReward -> gamePreferences.getProgressionState().dailyReward.canClaim
            LocalNotificationType.StreakRisk -> gamePreferences.getProgressionState().dailyReward.isStreakAtRisk
            LocalNotificationType.Mission -> !gamePreferences.getDailyChallengeState().completed
        }
        if (shouldSend) {
            showNotification(appContext, type)
            markSent(prefs)
        }
        LocalNotificationScheduler.sync(appContext, force = true)
    }

    private fun showNotification(context: Context, type: LocalNotificationType) {
        ensureChannel(context)
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_NOTIFICATION_TYPE, type.analyticsName)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            type.requestCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val message = when (type) {
            LocalNotificationType.DailyReward -> context.getString(R.string.notification_daily_reward_message)
            LocalNotificationType.StreakRisk -> context.getString(R.string.notification_streak_message)
            LocalNotificationType.Mission -> context.getString(R.string.notification_mission_message)
        }
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.refleks_avi_icon)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(type.notificationId, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    private fun isEnabled(
        prefs: android.content.SharedPreferences,
        type: LocalNotificationType
    ): Boolean {
        return when (type) {
            LocalNotificationType.DailyReward -> prefs.getBoolean(NOTIFICATION_DAILY_REWARD_KEY, false)
            LocalNotificationType.StreakRisk -> prefs.getBoolean(NOTIFICATION_STREAK_KEY, false)
            LocalNotificationType.Mission -> prefs.getBoolean(NOTIFICATION_NEW_MISSION_KEY, false)
        }
    }

    private fun canSendToday(prefs: android.content.SharedPreferences): Boolean {
        val today = todayKey()
        val sentDate = prefs.getString(NOTIFICATION_SENT_DATE_KEY, "").orEmpty()
        val sentCount = if (sentDate == today) prefs.getInt(NOTIFICATION_SENT_COUNT_KEY, 0) else 0
        return sentCount < MAX_DAILY_NOTIFICATIONS
    }

    private fun markSent(prefs: android.content.SharedPreferences) {
        val today = todayKey()
        val sentDate = prefs.getString(NOTIFICATION_SENT_DATE_KEY, "").orEmpty()
        val nextCount = if (sentDate == today) prefs.getInt(NOTIFICATION_SENT_COUNT_KEY, 0) + 1 else 1
        prefs.edit()
            .putString(NOTIFICATION_SENT_DATE_KEY, today)
            .putInt(NOTIFICATION_SENT_COUNT_KEY, nextCount.coerceAtMost(MAX_DAILY_NOTIFICATIONS))
            .apply()
    }

    companion object {
        const val EXTRA_TYPE = "notification_type"
    }
}

fun logNotificationEvent(
    event: FirebaseEvent,
    type: LocalNotificationType,
    permissionStatus: String? = null,
    sourceScreen: String = "settings"
) {
    FirebaseGameServices.logEvent(
        event = event,
        params = Bundle().apply {
            putString(FirebaseParam.NotificationType.key, type.analyticsName)
            putString(FirebaseParam.SourceScreen.key, sourceScreen)
            if (permissionStatus != null) {
                putString(FirebaseParam.PermissionStatus.key, permissionStatus)
            }
        }
    )
}

private fun dateKey(timeMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(timeMillis)
}

private fun todayKey(): String {
    return dateKey(System.currentTimeMillis())
}
