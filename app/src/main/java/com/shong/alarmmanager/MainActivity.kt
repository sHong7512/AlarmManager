package com.shong.alarmmanager

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName + "_sHong"

    private val alarmMaker by lazy { AlarmMaker(this) }
    private val preferences by lazy {SharedPreferences(this)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")

        makeAlarmDialog()
        findViewById<Button>(R.id.showDialogButton).setOnClickListener {
            showAlarmDialog()
        }

        findViewById<Button>(R.id.secAlarmButton).setOnClickListener {
            alarmMaker.secAlarm(5)
        }

        findViewById<Button>(R.id.cancelAlarmButton).setOnClickListener {
            alarmMaker.cancelAlarm(alarmMaker.REQUEST_CODE)
        }

        val key = preferences.getAlarmKey()
        val index = preferences.getAlarmIndex()
        val ldt = alarmMaker.transMillisToDate(preferences.getAlarmTime())
        findViewById<TextView>(R.id.timeAlarmTextView).text =
            alarmMaker.makeTimeMSG(key, index, ldt?.hour ?: 17, ldt?.minute ?: 30)
    }

    private val dialog by lazy { Dialog(this) }
    private fun makeAlarmDialog() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.item_alarmdialog)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }

    private val timeNotiMap by lazy { alarmMaker.timeNotiMap }
    private fun showAlarmDialog() {
        dialog.findViewById<TimePicker>(R.id.alarmTimePicker).apply {
            val ldt = alarmMaker.transMillisToDate(preferences.getAlarmTime())
            hour = ldt?.hour ?: LocalTime.now().hour
            minute = ldt?.minute ?: LocalTime.now().minute
        }

        var key = preferences.getAlarmKey()
        var index = preferences.getAlarmIndex()
        val firstIndexTextView = dialog.findViewById<TextView>(R.id.firstIndexTextView)
        val secondIndexTextView = dialog.findViewById<TextView>(R.id.secondIndexTextView)
        val alarmTimePicker = dialog.findViewById<TimePicker>(R.id.alarmTimePicker)

        firstIndexTextView.text = key
        secondIndexTextView.text = timeNotiMap[key]?.get(index)
        if (key.equals("매일")) secondIndexTextView.isEnabled = false

        firstIndexTextView.setOnClickListener {
            when (key) {
                "매일" -> {
                    key = "매주"
                    index = 0
                    secondIndexTextView.isEnabled = true
                }
                "매주" -> {
                    key = "매월"
                    index = 0
                    secondIndexTextView.isEnabled = true
                }
                "매월" -> {
                    key = "매일"
                    index = 0
                    secondIndexTextView.isEnabled = false
                }
            }
            firstIndexTextView.text = key
            secondIndexTextView.text = timeNotiMap[key]?.get(index)
        }

        secondIndexTextView.setOnClickListener {
            index++
            if (timeNotiMap[key]?.size ?: 0 <= index) index = 0
            secondIndexTextView.text = timeNotiMap[key]?.get(index)
        }

        dialog.findViewById<TextView>(R.id.settingDialogNegativeButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.settingDialogPositiveButton).setOnClickListener {
            val timeMillis = alarmMaker.transDateToMillis(
                key,
                index,
                alarmTimePicker.hour,
                alarmTimePicker.minute
            )
            alarmMaker.updateAlarm(timeMillis)
            preferences.setAlarmKey(key)
            preferences.setAlarmIndex(index)
            preferences.setAlarmTime(timeMillis)

            val msg = alarmMaker.makeTimeMSG(key, index, alarmTimePicker.hour, alarmTimePicker.minute)
            findViewById<TextView>(R.id.timeAlarmTextView).text = msg

            dialog.dismiss()

            Snackbar.make(
                findViewById(R.id.timeAlarmLayout),
                "$msg 입력 알림이 설정되었습니다.",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        dialog.show()
    }



}