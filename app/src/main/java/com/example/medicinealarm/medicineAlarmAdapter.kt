package com.example.medicinealarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class medicineAlarmAdapter(data: OrderedRealmCollection<MedicineAlarm>) :
    RealmRecyclerViewAdapter<MedicineAlarm, medicineAlarmAdapter.ViewHolder>(data,true){

    private var listener: ((Long?)-> Unit)? =null

    fun setOnItemClickListener(listener: (Long?)-> Unit ){
        this.listener = listener
    }

    init {
        setHasStableIds(true)
    }

    class ViewHolder(cell :View) : RecyclerView.ViewHolder(cell){
        val title : TextView = cell.findViewById(android.R.id.text1)
        val drinktime: TextView = cell.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): medicineAlarmAdapter.ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_2,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: medicineAlarmAdapter.ViewHolder, position: Int) {
        val medicinealarm: MedicineAlarm? =getItem(position)
        holder.title.text = medicinealarm?.title
        holder.drinktime.text = android.text.format.DateFormat.format("HH:mm", medicinealarm?.drinktime)
        //RcyclerViewのセルがタップされたことを通知するコールバック処理
        holder.itemView.setOnClickListener {
            listener?.invoke(medicinealarm?.id)
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }

}