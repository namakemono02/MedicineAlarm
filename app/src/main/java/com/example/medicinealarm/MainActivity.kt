package com.example.medicinealarm

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager.LayoutParams.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity(),AlartDialogs.Listener {
    //ダイアログに表示されたボタンを押した後の処理
    //drinked,snooze,cancelはMainActivityに描きたい　setAlarmManagerが障害
    override fun drinked() {
        Toast.makeText(this,"次も続けて飲みましょう", Toast.LENGTH_SHORT).show()
    }

    override fun snooze() {
        Toast.makeText(this,"後で飲みましょう", Toast.LENGTH_SHORT).show()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE,30)
        setAlarmManager(calendar)
    }

    override fun cancel() {
        Toast.makeText(this,"そういう時もあります", Toast.LENGTH_SHORT).show()
    }

    //AlarmManagerにアラーム時刻を登録する処理
    private fun setAlarmManager(calendar: Calendar){
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver:: class.java)
        val pending = PendingIntent.getBroadcast(this,0,intent,0)
        when{
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis,null)
                am.setAlarmClock(info,pending)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            }
            else -> {
                am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            }
        }
    }

    private lateinit var medicineAlarmdatabase: Realm
    private lateinit var soundPool : SoundPool
    private var soundResId =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //薬を飲む時間になったことを知らせるダイアログを表示する処理
        if (intent?.getBooleanExtra("onReceive",false)==true){
            when{
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                    setShowWhenLocked(true) //アクティビティが再開した時にロック画面状に表示
                    setTurnScreenOn(true)   //アクティビティが再開した時に画面をONにする
                    val keyguardManager=getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    keyguardManager.requestDismissKeyguard(this,null) //デバイスがロックされているときにkeyguardを解除する
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    window.addFlags(FLAG_TURN_SCREEN_ON or FLAG_SHOW_WHEN_LOCKED)
                    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    keyguardManager.requestDismissKeyguard(this, null)
                }
                else -> {
                    window.addFlags(FLAG_TURN_SCREEN_ON or FLAG_DISMISS_KEYGUARD)
                }
            }
            soundPool =
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    @Suppress("DEPRECATION")
                    SoundPool(2,AudioManager.STREAM_ALARM,0)
                }else{
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    SoundPool.Builder()
                        .setMaxStreams(2)
                        .setAudioAttributes(audioAttributes)
                        .build()
                }
            soundResId = soundPool.load(this,R.raw.alarmvoice,5)

            var streamId:Int
            do { //play()が成功するまでループ
                streamId=soundPool.play(soundResId,0f,0f,0,0,1.0f)
            }while (streamId==0)
            //※スリープ時に鳴らない!
            soundPool.play(soundResId,100f,100f,5,0,1.0f) //薬の時間を知らせる

            //ダイアログ表示
            val dialog = AlartDialogs()
            dialog.show(supportFragmentManager,"alart_dialog")
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //RycyclerViewへのデータ表示処理
        medicineAlarmdatabase= Realm.getDefaultInstance()
        medicineAlarmlist.layoutManager= LinearLayoutManager(this)
        val alarms = medicineAlarmdatabase.where<MedicineAlarm>().findAll()
        val Alarmadapter= medicineAlarmAdapter(alarms)
        medicineAlarmlist.adapter = Alarmadapter

        //Alarm登録処理へ
        addAlarm.setOnClickListener { view ->
            val intent = Intent(this, AlarmEditActivity::class.java)
            startActivity(intent)
        }

        //RcyclerViewのセルがタップされたことを通知するコールバック処理
        Alarmadapter.setOnItemClickListener { id ->
            val intent=Intent(this, AlarmEditActivity::class.java)
                .putExtra("alarm_id",id)
            startActivity(intent)
        }
    }

    override fun onPause() { //別のアクティビティが最画面になった時に最初に呼ばれる処理
        super.onPause()
        if (intent?.getBooleanExtra("onReceive",false)==true) soundPool.release() //メモリを食うので解放S
    }

    override fun onDestroy() {
        super.onDestroy()
        medicineAlarmdatabase.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
