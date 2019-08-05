package com.example.medicinealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

//アラーム処理
class Alarm (calendar: Calendar){
    val calendar: Calendar = calendar //アラーム時刻

    //AlarmManagerにアラーム時刻を登録する処理
    fun setLoopAlarmManager(alarmManager: AlarmManager, context: Context){
        val intent = Intent(context, AlarmBroadcastReceiver:: class.java)
        val pending = PendingIntent.getBroadcast(context,0,intent,0)
        //正確なアラームではない　スリープ時はならない
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.timeInMillis, AlarmManager.INTERVAL_DAY,pending) //指定時刻から１日ごとにアラーム
    }

    //AlarmManagerにアラーム時刻を登録する処理
    fun setAlarmManager(alarmManager: AlarmManager, context: Context){
        val intent = Intent(context, AlarmBroadcastReceiver:: class.java)
        val pending = PendingIntent.getBroadcast(context,0,intent,0)
        //AndroidOSのバージョンに合わせて登録処理
        when{
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis,null)
                alarmManager.setAlarmClock(info,pending)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            }
            else -> {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            }
        }
    }

    fun snooze(alarmManager: AlarmManager, context: Context){
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE,30)
        val intent = Intent(context, AlarmBroadcastReceiver:: class.java)
        val pending = PendingIntent.getBroadcast(context,0,intent,0)
        setAlarmManager(alarmManager,context)
    }
}