package com.shong.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SystemReceiver : BroadcastReceiver() {
    private val TAG = this::class.java.simpleName + "_sHong"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG,"intent action-> ${intent.action}")

        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            val alarmMaker = AlarmMaker(context)
            alarmMaker.remakeAlarm()
        }
    }
}