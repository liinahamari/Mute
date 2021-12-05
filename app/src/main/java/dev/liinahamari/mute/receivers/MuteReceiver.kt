package dev.liinahamari.mute.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import dev.liinahamari.mute.*
import java.util.*

const val MUTE_REQUEST_CODE = 101

class MuteReceiver : BroadcastReceiver() {
    companion object {
        fun createIntent(dayOfWeek: Int, context: Context): Intent {
            if (dayOfWeek !in Calendar.MONDAY..Calendar.FRIDAY) throw IllegalStateException()

            return Intent(context, MuteReceiver::class.java)
                .apply {
                    putExtra(ARG_DAY_OF_WEEK, dayOfWeek)
                }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).mute()

        intent.getIntExtra(ARG_DAY_OF_WEEK, -1)
            .also { require(it in Calendar.MONDAY..Calendar.FRIDAY) }
            .also { dayOfWeek ->
                dayOfWeek.toNextMuteTime()
                    .also {
                        val muteIntent = PendingIntent.getBroadcast(context, MUTE_REQUEST_CODE, createIntent(dayOfWeek, context), 0)
                        (context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager).set(it, muteIntent)
                    }
            }
    }
}