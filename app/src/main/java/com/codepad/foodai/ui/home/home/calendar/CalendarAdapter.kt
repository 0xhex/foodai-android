package com.codepad.foodai.ui.home.home.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import java.util.Date

class CalendarAdapter(private val items: List<List<Triple<Date, Int, String>>>, private val onSubItemSelected: (Int, Int) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val isLastWeek = position == items.size - 1
        holder.bind(items[position], position, isLastWeek, onSubItemSelected)
    }

    override fun getItemCount(): Int = items.size

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subRecyclerView: RecyclerView = itemView.findViewById(R.id.subRecyclerView)

        fun bind(subItems: List<Triple<Date, Int, String>>, mainPosition: Int, isLastWeek: Boolean, onSubItemSelected: (Int, Int) -> Unit) {
            subRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            subRecyclerView.adapter = WeekAdapter(subItems, mainPosition, isLastWeek, onSubItemSelected)
        }
    }
}