package com.codepad.foodai.ui.home.home.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import java.util.Date

class WeekAdapter(private val items: List<Triple<Date, Int, String>>, private val mainPosition: Int, private val isLastWeek: Boolean, private val onSubItemSelected: (Int, Int) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_REGULAR = 0
        private const val TYPE_FUTURE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size - 1 && items.size == 7 && isLastWeek) TYPE_FUTURE else TYPE_REGULAR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_FUTURE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_future_day, parent, false)
            FutureDayViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
            RegularDayViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RegularDayViewHolder) {
            holder.bind(items[position], mainPosition, position, onSubItemSelected)
        } else if (holder is FutureDayViewHolder) {
            holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    class RegularDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayLetter: TextView = itemView.findViewById(R.id.day_letter)
        private val txtDay: TextView = itemView.findViewById(R.id.txt_day)

        fun bind(item: Triple<Date, Int, String>, mainPosition: Int, subPosition: Int, onSubItemSelected: (Int, Int) -> Unit) {
            dayLetter.text = item.third
            txtDay.text = item.second.toString()
            itemView.setOnClickListener {
                onSubItemSelected(mainPosition, subPosition)
            }
        }
    }

    class FutureDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayLetter: TextView = itemView.findViewById(R.id.day_letter)
        private val txtDay: TextView = itemView.findViewById(R.id.txt_day)

        fun bind(item: Triple<Date, Int, String>) {
            dayLetter.text = item.third
            txtDay.text = item.second.toString()
        }
    }
}