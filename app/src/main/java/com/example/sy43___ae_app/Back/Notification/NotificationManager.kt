package com.example.sy43___ae_app.Back.Notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.sy43___ae_app.Back.FrontDTO.NewUI
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * NotificationManager - Handles scheduling and canceling notifications for followed news
 */
class NotificationManager(private val context: Context) {

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Suivi d'actualités"
            val descriptionText = "Notifications pour les événements suivis"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("NEWS_FOLLOW_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNewsNotifications(news: NewUI) {
        val now = LocalDateTime.now()
        
        // 24h notification
        val time24h = news.startDate.minusHours(24)
        if (time24h.isAfter(now)) {
            scheduleNotification(
                news.id * 2,
                news.title,
                "Cet événement commence dans 24h !",
                time24h
            )
        }

        // 1h notification
        val time1h = news.startDate.minusHours(1)
        if (time1h.isAfter(now)) {
            scheduleNotification(
                news.id * 2 + 1,
                news.title,
                "Cet événement commence dans 1h !",
                time1h
            )
        }
    }

    fun cancelNewsNotifications(newsId: Int) {
        cancelNotification(newsId * 2)
        cancelNotification(newsId * 2 + 1)
    }

    private fun scheduleNotification(id: Int, title: String, message: String, time: LocalDateTime) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelNotification(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
