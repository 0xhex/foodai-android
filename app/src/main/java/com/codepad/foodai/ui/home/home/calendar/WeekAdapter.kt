package com.codepad.foodai.ui.home.home.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeekAdapter(
    private val items: List<Triple<Date, Int, String>>,
    private val mainPosition: Int,
    private val isLastWeek: Boolean,
    private val selectedPosition: Pair<Int, Int>?,
    private val onSubItemSelected: (Int, Int, Triple<Date, Int, String>) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_REGULAR = 0
        private const val TYPE_FUTURE = 1
    }

    private val today: String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size - 1 && items.size == 7 && isLastWeek) TYPE_FUTURE else TYPE_REGULAR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == TYPE_FUTURE) R.layout.item_future_day else R.layout.item_day
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return if (viewType == TYPE_FUTURE) FutureDayViewHolder(view) else RegularDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RegularDayViewHolder) {
            holder.bind(
                items[position], mainPosition, position, onSubItemSelected, selectedPosition, today
            )
        } else if (holder is FutureDayViewHolder) {
            holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    inner class RegularDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayLetter: TextView = itemView.findViewById(R.id.day_letter)
        private val txtDay: TextView = itemView.findViewById(R.id.txt_day)

        fun bind(
            item: Triple<Date, Int, String>,
            mainPosition: Int,
            subPosition: Int,
            onSubItemSelected: (Int, Int, Triple<Date, Int, String>) -> Unit,
            selectedPosition: Pair<Int, Int>?,
            today: String,
        ) {
            dayLetter.text = item.third
            txtDay.text = item.second.toString()

            val itemDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(item.first)
            dayLetter.background = if (itemDate == today) {
                ContextCompat.getDrawable(itemView.context, R.drawable.circle_background_today)
            } else {
                ContextCompat.getDrawable(itemView.context, R.drawable.circle_background)
            }

            val regularFont = ResourcesCompat.getFont(itemView.context, R.font.euro_stile_regular)
            val boldFont = ResourcesCompat.getFont(itemView.context, R.font.euro_stile_bold)

            val typeface =
                if (selectedPosition != null && mainPosition == selectedPosition.first && subPosition == selectedPosition.second) {
                    boldFont
                } else {
                    regularFont
                }

            dayLetter.typeface = typeface
            txtDay.typeface = typeface

            itemView.setOnClickListener {
                onSubItemSelected(mainPosition, subPosition, item)
            }
        }
    }

    inner class FutureDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayLetter: TextView = itemView.findViewById(R.id.day_letter)
        private val txtDay: TextView = itemView.findViewById(R.id.txt_day)

        fun bind(item: Triple<Date, Int, String>) {
            dayLetter.text = item.third
            txtDay.text = item.second.toString()
        }
    }
}