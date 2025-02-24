import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.databinding.ItemMoodChipBinding

class MoodChipsAdapter(
    private val onMoodSelected: (List<String>) -> Unit
) : RecyclerView.Adapter<MoodChipsAdapter.ViewHolder>() {

    private val selectedMoods = mutableSetOf<String>()
    
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMoodChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
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
                    isSelected = !isSelected
                    onMoodSelected(selectedMoods.toList())
                }
            }
        }
    }
} 