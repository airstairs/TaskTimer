package com.xthan.tasktimer

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat

class TimerService : Service() {
    private var countDownTimer: CountDownTimer? = null
    private val CHANNEL_ID = "TaskTimerChannel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val durationSeconds = intent?.getLongExtra("DURATION", 0) ?: 0
        val taskName = intent?.getStringExtra("TASK_NAME") ?: "Task"

        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Task Timer Running")
            .setContentText("Timer active for: $taskName")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                triggerAlarmNotification(taskName)
                stopSelf()
            }
        }.start()

        return START_NOT_STICKY
    }

    private fun triggerAlarmNotification(taskName: String) {
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("TASK_NAME", taskName)
        }
        sendBroadcast(alarmIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Task Timer Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
