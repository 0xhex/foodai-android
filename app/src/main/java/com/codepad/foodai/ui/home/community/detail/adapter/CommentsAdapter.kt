import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.databinding.ItemCommentBinding
import com.codepad.foodai.domain.models.community.CommunityComment

class CommentsAdapter(
    private val currentUserId: String?,
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<CommunityComment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommunityComment) {
            binding.apply {
                // Set user initial
                txtUserInitial.text = comment.user.name?.firstOrNull()?.toString()

                // Set username and date
                txtUsername.text = comment.user.name
                txtDate.text = comment.createdAt?.toShortTimeString()

                // Set comment text
                txtComment.text = comment.text

                // Show delete button only for current user's comments
                btnDelete.isVisible = comment.user.id == currentUserId
                btnDelete.setOnClickListener {
                    onDeleteClick(comment.id)
                }
            }
        }
    }

    private class CommentDiffCallback : DiffUtil.ItemCallback<CommunityComment>() {
        override fun areItemsTheSame(oldItem: CommunityComment, newItem: CommunityComment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommunityComment, newItem: CommunityComment): Boolean {
            return oldItem == newItem
        }
    }
} 