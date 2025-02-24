import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.ItemMoodChipBinding

class MoodChipsAdapter(
    private val onMoodSelected: (List<String>) -> Unit,
    private val isEditing: Boolean = false
) : RecyclerView.Adapter<MoodChipsAdapter.ViewHolder>() {

    private val selectedMoods = mutableSetOf<String>()
    
    private val moodResIds = listOf(
        R.string.mood_allergy,
        R.string.mood_heartbreak,
        R.string.mood_in_love,
        R.string.mood_overeating,
        R.string.mood_headache,
        R.string.mood_successful,
        R.string.mood_exhausted,
        R.string.mood_balanced,
        R.string.mood_depressed,
        R.string.mood_energetic,
        R.string.mood_gas_pain,
        R.string.mood_proud,
        R.string.mood_hospital,
        R.string.mood_constipation,
        R.string.mood_stomach_ache,
        R.string.mood_concentrated,
        R.string.mood_nausea,
        R.string.mood_bad_mood,
        R.string.mood_slept_poorly,
        R.string.mood_content,
        R.string.mood_queasy,
        R.string.mood_migraine,
        R.string.mood_grateful,
        R.string.mood_motivated
    )

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
        if (isEditing) {
            // Show all moods in edit mode
            holder.bind(moodResIds[position])
        } else {
            // Show only selected moods
            val selectedMoodsList = selectedMoods.toList()
            if (position < selectedMoodsList.size) {
                // Find the resource ID for this selected mood
                val selectedMood = selectedMoodsList[position]
                val moodResId = moodResIds.find { resId ->
                    holder.itemView.context.getString(resId) == selectedMood
                } ?: moodResIds[0]
                holder.bind(moodResId)
            }
        }
    }

    override fun getItemCount() = if (isEditing) moodResIds.size else selectedMoods.size

    fun setSelectedMoods(moodString: String) {
        selectedMoods.clear()
        if (moodString.isNotEmpty()) {
            selectedMoods.addAll(moodString.split(",").map { it.trim() })
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMoodChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(moodResId: Int) {
            val mood = binding.root.context.getString(moodResId)
            binding.chip.apply {
                text = mood
                isSelected = selectedMoods.contains(mood)
                setOnClickListener {
                    if (isEditing) {  // Only allow selection in edit mode
                        if (selectedMoods.contains(mood)) {
                            selectedMoods.remove(mood)
                        } else {
                            selectedMoods.add(mood)
                        }
                        isSelected = !isSelected
                        onMoodSelected(selectedMoods.toList())
                    }
                }
            }
        }
    }
} 