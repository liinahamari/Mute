package dev.liinahamari.mute

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.media.AudioManager
import android.os.Build
import java.util.*

/**
 * @return next mute time in millis
 * */
fun Int.toNextMuteTime(): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_WEEK, this@toNextMuteTime)
    set(Calendar.HOUR_OF_DAY, 11)
    set(Calendar.MINUTE, 30)
    set(Calendar.SECOND, 0)
}.timeInMillis
    .let { if (it < System.currentTimeMillis()) it + AlarmManager.INTERVAL_DAY * 7 else it }

/**
 * @return next unmute time in millis
 * */
fun Int.toNextUnmuteTime(): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_WEEK, this@toNextUnmuteTime)
    set(Calendar.HOUR_OF_DAY, 19)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
}.timeInMillis
    .let { if (it < System.currentTimeMillis()) it + AlarmManager.INTERVAL_DAY * 7 else it }

fun AudioManager.mute() {
    ringerMode = AudioManager.RINGER_MODE_VIBRATE
}

fun AudioManager.unmute() {
    ringerMode = AudioManager.RINGER_MODE_NORMAL
}

@SuppressLint("ObsoleteSdkInt")
fun AlarmManager.set(startTime: Long, pendingIntent: PendingIntent) = if (Build.VERSION.SDK_INT < 23) {
    setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
} else {
    setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
}