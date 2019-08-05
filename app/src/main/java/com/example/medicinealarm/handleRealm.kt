package com.example.medicinealarm

import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_alarm_edit.*

class handleRealm(realm: Realm) {
    val realm : Realm=realm

    fun registerNewlyToDatabase{
        realm.executeTransaction { db: Realm ->
            val maxId = db.where<MedicineAlarm>().max("id")
            val nextId = (maxId?.toLong() ?: 0L) + 1 //新規登録するDBのIDを取得
            val alarm = db.createObject<MedicineAlarm>(nextId)

            val drinktime = drinktimeText.text.toString().toDate("HH:mm")
            if (drinktime != null) {
                alarm.drinktime = drinktime
            } else {
                realm.cancelTransaction()
            }
            alarm.title = timezoneText.text.toString()

        }
    }
}