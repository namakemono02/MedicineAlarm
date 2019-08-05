package com.example.medicinealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_alarm_edit.*
import java.lang.IllegalArgumentException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

//Alarmの登録処理
class AlarmEditActivity : AppCompatActivity(),  TimePickerFragment.OnTimeSelectedListener{

    private lateinit var Alarm: Realm

    private fun registerInRealm(alarm : MedicineAlarm) {
        alarm.title = timezoneText.text.toString()
        //薬を飲む時刻を登録
        val drinktime = drinktimeText.text.toString().toDate("HH:mm")
        if (drinktime != null) {
            alarm.drinktime = drinktime
        } else {
            Alarm.cancelTransaction()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_edit)

        //AlarmEditActivtyのデータ表示処理
        Alarm = Realm.getDefaultInstance()
        val alarmId = intent?.getLongExtra("alarm_id",-1L)
        if(alarmId != -1L){ //既にDBに登録されている場合は、登録されたデータを表示する
            val alarm = Alarm.where<MedicineAlarm>()
                .equalTo("id",alarmId).findFirst()
            timezoneText.setText(alarm?.title)
            drinktimeText.setText(DateFormat.format("HH:mm",alarm?.drinktime))
            delete.visibility=View.VISIBLE
        }else{
            delete.visibility=View.INVISIBLE
        }

        //データベースへの登録処理
        save.setOnClickListener { view: View ->
            //アラーム時刻設定
            val calendar = Calendar.getInstance()
            val year =calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)+1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val alartDay = "%1$04d/%2$02d/%3$02d".format(year,month,day)

            val alartTime = "${alartDay} ${drinktimeText.text}".toDate()
            when{
                alartTime != null -> {
                    calendar.time = alartTime
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarm = Alarm(calendar)
                    alarm.setAlarmManager(alarmManager,this)
                    Toast.makeText(this,"${calendar.time}にアラームをセットしました", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this,"時刻の形式がただしくありません",Toast.LENGTH_SHORT).show()
                }
            }

            //データベースへの登録処理
            when(alarmId){
                //新規登録処理
                -1L -> {
                    Alarm.executeTransaction{ db: Realm ->
                        val maxId =db.where<MedicineAlarm>().max("id")
                        val nextId = (maxId?.toLong() ?: 0L) +1
                        val alarm = db.createObject<MedicineAlarm>(nextId)

                        registerInRealm(alarm)
//                        val drinktime = drinktimeText.text.toString().toDate("HH:mm")
//                        if (drinktime!=null) {
//                            alarm.drinktime=drinktime
//                        }else{
//                            Alarm.cancelTransaction()
//                        }
//                        alarm.title=timezoneText.text.toString()
//                        //radiobuttonを戻す処理がわからん。whenを使えば力技でできないこともなさそう？
                    }
                    Snackbar.make(view, "追加しました", Snackbar.LENGTH_SHORT)
                        .setAction("戻る") {finish()}
                        .setActionTextColor(Color.RED)
                        .show()
                }
                //更新処理
                else -> {
                    Alarm.executeTransaction{db: Realm ->
                        val alarm = db.where<MedicineAlarm>()
                            .equalTo("id",alarmId).findFirst()
                        if(alarm != null){
                            registerInRealm(alarm)
                        }else{
                            Alarm.cancelTransaction()
                        }
                    }
                    Snackbar.make(view,"更新しました",Snackbar.LENGTH_SHORT)
                        .setAction("戻る") {finish()}
                        .setActionTextColor(Color.RED)
                        .show()
                }
            }
        }

        //データの削除処理
        delete.setOnClickListener { view : View ->
            Alarm.executeTransaction{db: Realm ->
                db.where<MedicineAlarm>().equalTo("id",alarmId)
                    ?.findFirst()
                    ?.deleteFromRealm()
            }
            Snackbar.make(view, "削除しました", Snackbar.LENGTH_SHORT)
                .setAction("戻る") {finish()}
                .setActionTextColor(Color.YELLOW)
                .show()
        }

        //時刻入力ダイアログを表示
        drinktimeText.setOnClickListener {
            val dialog = TimePickerFragment()
            dialog.show(supportFragmentManager,"time_dialog")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Alarm.close()
    }

    //ダイアログで選択した時刻をテキストビューに表示する処理
    override fun onSelected(hourOfDay: Int, minute: Int) {
        drinktimeText.text="%1$02d:%2$02d".format(hourOfDay,minute)
    }

    //入力時刻の形式判定処理
    private fun String.toDate(pattern: String ="yyyy/MM/dd HH:mm"): Date? {
        return try {
            SimpleDateFormat(pattern).parse(this)
        }catch (e : IllegalArgumentException){
            return null
        }catch (e : ParseException){
            return null
        }
    }
}
