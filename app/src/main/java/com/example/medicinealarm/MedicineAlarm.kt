package com.example.medicinealarm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class MedicineAlarm: RealmObject() {
    @PrimaryKey
    var id: Long=0
    var drinktime: Date=Date() //薬を飲む時間　初期値と型はこれでいい？
    var title: String=""       //アラームのタイトル
    var drinkflag=true         //アラームのON・OFF判定
}