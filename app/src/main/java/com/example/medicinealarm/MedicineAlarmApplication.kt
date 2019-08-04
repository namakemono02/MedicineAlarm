package com.example.medicinealarm

import android.app.Application
import io.realm.Realm

class MedicineAlarmApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}