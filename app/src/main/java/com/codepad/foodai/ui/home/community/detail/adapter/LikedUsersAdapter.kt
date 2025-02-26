import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.databinding.ItemLikedUserBinding
import com.codepad.foodai.domain.models.community.CommunityUser

class LikedUsersAdapter :
    ListAdapter<CommunityUser, LikedUsersAdapter.LikedUserViewHolder>(LikedUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedUserViewHolder {
        val binding = ItemLikedUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LikedUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikedUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LikedUserViewHolder(
        private val binding: ItemLikedUserBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: CommunityUser) {
            binding.apply {
                // Set user emoji
                txtEmoji.text = user.assignedEmoji ?: "🙂"

                // Set username
                txtUsername.text = user.name
            }
        }
    }

    private class LikedUserDiffCallback : DiffUtil.ItemCallback<CommunityUser>() {
        override fun areItemsTheSame(oldItem: CommunityUser, newItem: CommunityUser): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommunityUser, newItem: CommunityUser): Boolean {
            return oldItem == newItem
        }
    }
} 