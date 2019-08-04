package com.example.medicinealarm

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class AlartDialogs : DialogFragment() {
    interface Listener{
        fun drinked()
        fun snooze()
        fun cancel()
    }


    private var listener: Listener? = null

    //アクティビティからフラグメントが呼ばれた時の処理
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        when(context){
            is Listener -> listener = context //Listenerインターフェースを持つならcontextを代入
        }
    }

    //ダイアログが生成された時の処理
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("薬の時間です。")
        builder.setNeutralButton("飲んだ") {dialog, which ->
            listener?.drinked()
        }
        builder.setNegativeButton("30分後") {dialog,which ->
            listener?.snooze()
        }
        builder.setPositiveButton("今回はいいや") {dialog, which ->
            listener?.cancel()
        }
        return builder.create()
    }
}

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener{
    interface OnTimeSelectedListener{
        fun onSelected(hourOfDay : Int, minute: Int)
    }

    private var listener: OnTimeSelectedListener? =null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        when(context){
            is OnTimeSelectedListener -> listener=context
        }
    }

    //ダイアログが表示された時の処理
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c= Calendar.getInstance()
        val hour= c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        return TimePickerDialog(context, this , hour , minute ,true )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        listener?.onSelected(hourOfDay,minute)
    }

}