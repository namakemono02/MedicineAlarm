package com.example.medicinealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmBroadcastReceiver : BroadcastReceiver() {
    //アラーム受信後の処理
    override fun onReceive(context: Context, intent: Intent) {
        val mainIntent = Intent(context, MainActivity::class.java)
            .putExtra("onReceive",true) //BroadCastReceiverが起動したこと知らせるキー
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(mainIntent)
    }
}
