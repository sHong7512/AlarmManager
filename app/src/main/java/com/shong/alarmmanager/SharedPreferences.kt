package com.shong.alarmmanager

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(context : Context){
    private val pref : SharedPreferences = context.getSharedPreferences("alarmPref", Context.MODE_PRIVATE)

    fun setAlarmKey(alarmKey: String) = pref.edit().putString("alarmKey", alarmKey).apply()
    fun getAlarmKey(): String = pref.getString("alarmKey","매일")!!

    fun setAlarmIndex(alarmIndex: Int) = pref.edit().putInt("alarmIndex", alarmIndex).apply()
    fun getAlarmIndex(): Int = pref.getInt("alarmIndex",0)

    fun setAlarmTime(alarmTime: Long) = pref.edit().putLong("alarmTime", alarmTime).apply()
    fun getAlarmTime(): Long = pref.getLong("alarmTime",1635323400000)

}