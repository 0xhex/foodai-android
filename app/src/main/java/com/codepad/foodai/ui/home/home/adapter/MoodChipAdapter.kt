package com.codepad.foodai.ui.home.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.databinding.ItemMoodChipBinding

class MoodChipAdapter(
    private val onMoodSelected: (String) -> Unit,
) : RecyclerView.Adapter<MoodChipAdapter.ViewHolder>() {

    private val moods = listOf(
        "🤧 Allergy",
        "💔 Heartbreak",
        "🥰 In Love",
        "🍔 Overeating",
        "🤯 Headache",
        "🏆 Successful",
        "😞 Exhausted",
        "⚖️ Balanced",
        "🥺 Depressed",
        "💪 Energetic",
        "💨 Gas Pain",
        "🥇 Proud",
        "🏥 Hospital",
        "🚽 Constipation",
        "💣 Stomach Ache",
        "🤔 Concentrated",
        "🤮 Nausea",
        "😔 Bad Mood",
        "👻 Slept Poorly",
        "😌 Content",
        "🤢 Queasy",
        "🤕 Migraine",
        "🙏 Grateful",
        "✊ Motivated"
    )

    private val selectedMoods = mutableSetOf<String>()

    inner class ViewHolder(private val binding: ItemMoodChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: String) {
            binding.chip.apply {
                text = mood
                isSelected = selectedMoods.contains(mood)
                setOnClickListener {
                    if (selectedMoods.contains(mood)) {
                        selectedMoods.remove(mood)
                    } else {
                        selectedMoods.add(mood)
                    }
                    notifyItemChanged(adapterPosition)
                    onMoodSelected(selectedMoods.joinToString(","))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMoodChipBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(moods[position])
    }

    override fun getItemCount() = moods.size

    fun setSelectedMoods(moodString: String) {
        selectedMoods.clear()
        if (moodString.isNotEmpty()) {
            selectedMoods.addAll(moodString.split(",").map { it.trim() })
        }
        notifyDataSetChanged()
    }
} 