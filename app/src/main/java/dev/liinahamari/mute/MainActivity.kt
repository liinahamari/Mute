package dev.liinahamari.mute

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.CheckBox
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import dev.liinahamari.mute.receivers.MUTE_REQUEST_CODE
import dev.liinahamari.mute.receivers.MuteReceiver
import dev.liinahamari.mute.receivers.UNMUTE_REQUEST_CODE
import dev.liinahamari.mute.receivers.UnmuteReceiver
import java.text.DateFormat
import java.util.*

const val ARG_DAY_OF_WEEK = "arg_day_of_week"
private const val FLAG_IS_SAVED = "flag_is_saved"

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val powerManager by lazy { getSystemService(POWER_SERVICE) as PowerManager }
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val alarmManager by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }

    private val batteryOptimization = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        findViewById<CheckBox>(R.id.batteryOptCb).isChecked = it.resultCode == RESULT_OK
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkIgnoringBatteryOptimizations()

        if (sharedPreferences.getBoolean(FLAG_IS_SAVED, false)) return
        sharedPreferences.edit().putBoolean(FLAG_IS_SAVED, true).apply()

        scheduleMuteOnWorkDays()
        scheduleUnMuteOnWorkDays()
    }

    @SuppressLint("BatteryLife")
    private fun checkIgnoringBatteryOptimizations() = if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
        findViewById<CheckBox>(R.id.batteryOptCb).isChecked = true
    } else {
        batteryOptimization.launch(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        })
    }

    private fun scheduleMuteOnWorkDays() {
        (Calendar.MONDAY..Calendar.FRIDAY)
            .map { dayOfWeek ->
                dayOfWeek.toNextMuteTime()
                    .also {
                        println(">>>>>>>> NEXT MUTE: ${DateFormat.getDateInstance(DateFormat.LONG).format(Date(it))}")

                        val muteIntent = PendingIntent.getBroadcast(this, MUTE_REQUEST_CODE, MuteReceiver.createIntent(dayOfWeek, this), 0)
                        alarmManager.set(it, muteIntent)
                    }
            }
    }

    private fun scheduleUnMuteOnWorkDays() {
        (Calendar.MONDAY..Calendar.FRIDAY)
            .map { dayOfWeek ->
                dayOfWeek.toNextUnmuteTime()
                    .also {
                        println(">>>>>>>> NEXT UNMUTE: ${DateFormat.getDateInstance(DateFormat.LONG).format(Date(it))}")

                        val unMuteIntent = PendingIntent.getBroadcast(this, UNMUTE_REQUEST_CODE, UnmuteReceiver.createIntent(dayOfWeek, this), 0)
                        alarmManager.set(it, unMuteIntent)
                    }
            }
    }
}