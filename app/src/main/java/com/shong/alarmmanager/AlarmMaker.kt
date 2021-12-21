package com.shong.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import java.time.*

class AlarmMaker(private val context: Context) {
    private val TAG = this::class.java.simpleName + "_sHong"
    private val zoneId = ZoneId.of("Asia/Seoul")
    private val pref by lazy { SharedPreferences(context) }
    internal val REQUEST_CODE = 7512

    internal val timeNotiMap: Map<String, List<String>> =
        mapOf(
            Pair("매일", listOf("")),
            Pair("매주", listOf("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일")),
            Pair("매월", listOf("초일", "말일"))
        )

    private val alarmManager by lazy { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    internal fun transDateToMillis(key: String, index: Int, hour: Int, minute: Int): Long {
        val cdt = LocalDateTime.now().atZone(zoneId)
        val currentToLong = cdt.toInstant().toEpochMilli()

        val now = LocalDate.now()
        val time = LocalTime.of(hour, minute)
        when (key) {
            "매일" -> {
                val localDateTime = LocalDateTime.of(now, time)
                val timeToLong = localDateTime.atZone(zoneId).toInstant().toEpochMilli()

                if (timeToLong >= currentToLong) {
                    return timeToLong
                } else {
                    val ndt = localDateTime.plusDays(1L).atZone(zoneId)
                    return ndt.toInstant().toEpochMilli()
                }
            }
            "매주" -> {
                val pd = if (now.dayOfWeek.value <= index + 1) index + 1 - now.dayOfWeek.value else index + 1 + 7 - now.dayOfWeek.value

                val localDateTime = LocalDateTime.of(now.plusDays(pd.toLong()), time)
                val timeToLong = localDateTime.atZone(zoneId).toInstant().toEpochMilli()

                if (timeToLong >= currentToLong) {
                    return timeToLong
                } else {
                    val ndt = localDateTime.plusDays(7L).atZone(zoneId)
                    return ndt.toInstant().toEpochMilli()
                }
            }
            "매월" -> {
                val firstDayDate = LocalDate.of(now.year, now.month, 1)
                val endDayDate = firstDayDate.plusMonths(1).minusDays(1)
                Log.d(TAG, "$firstDayDate $endDayDate ")

                val localDateTime: LocalDateTime
                if (index == 0) localDateTime = LocalDateTime.of(firstDayDate, time)
                else localDateTime = LocalDateTime.of(endDayDate, time)

                val timeToLong = localDateTime.atZone(zoneId).toInstant().toEpochMilli()

                if (timeToLong >= currentToLong) {
                    return timeToLong
                } else {
                    val nextDateTime: LocalDateTime
                    if (index == 0) nextDateTime =
                        LocalDateTime.of(firstDayDate.plusMonths(1), time)
                    else nextDateTime =
                        LocalDateTime.of(firstDayDate.plusMonths(2).minusDays(1), time)

                    return nextDateTime.atZone(zoneId).toInstant().toEpochMilli()
                }
            }
        }
        return System.currentTimeMillis() + 10 * 1000
    }

    internal fun transMillisToDate(millis: Long): LocalDateTime?{
        try {
            return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        } catch (e: Exception) {
            Log.e(TAG, "transDate ERROR : $e")
            return null
        }
    }

    internal fun updateAlarm(milliTime: Long) {
        Log.d(TAG, "${milliTime} ${Instant.ofEpochMilli(milliTime).atZone(ZoneId.systemDefault()).toLocalDateTime()}")
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,  //requestCode
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            milliTime,
            pendingIntent
        )

        //제일 정확한 알람
//        alarmanager.setAlarmClock(AlarmManager.AlarmClockInfo(milliTime, pendingIntent), pendingIntent)
    }

    internal fun cancelAlarm(requestCode: Int) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,  //requestCode
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent.cancel()
    }

    internal fun remakeAlarm(){
        val key = pref.getAlarmKey()
        val index = pref.getAlarmIndex()
        val millis = pref.getAlarmTime()

        val localDateTime = transMillisToDate(millis) ?: return
        val fixTimeMillis = transDateToMillis(key,index,localDateTime.hour,localDateTime.minute)
        updateAlarm(fixTimeMillis)
        Log.d(TAG,"remake Alarm Complete! $key ${timeNotiMap[key]?.get(index)} ${transMillisToDate(fixTimeMillis)}")
    }

    //10초뒤 알람
    internal fun secAlarm(sec: Int) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sec,  //requestCode
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = (SystemClock.elapsedRealtime() + sec * 1000)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,   //elapsed 시간 쓸떄는 이걸로 써줘야함
            triggerTime,
            pendingIntent
        )

        //제일 정확한 알람
//        alarmanager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent)
    }

    internal fun makeTimeMSG(key: String, index: Int, hour_T: Int, minute_T: Int): String{
        var msg = ""

        var ampm = "am"
        var hour = hour_T
        if (hour > 12) {
            hour -= 12
            ampm = "pm"
        }
        val minute = if (minute_T < 10) "0" + "${minute_T}" else minute_T.toString()

        msg = "$key ${timeNotiMap[key]?.get(index)} ${hour}:${minute} ${ampm}"

        return msg
    }

}