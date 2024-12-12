package com.codepad.foodai.ui.user_property.rating

import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentRatingBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import com.codepad.foodai.ui.user_property.loading.LoadingFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class RatingFragment : BaseFragment<FragmentRatingBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()
    private var currentReviewIndex = 0
    private var timer: Timer? = null

    override fun getLayoutId() = R.layout.fragment_rating

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        startTimer()
    }

    private fun startTimer() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    updateReview()
                }
            }
        }, 0, 4000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun updateReview() {
        val reviews = sharedViewModel.reviews
        if (reviews.isNotEmpty()) {
            currentReviewIndex = (currentReviewIndex + 1) % reviews.size
            val currentReview = reviews[currentReviewIndex]

            val imgReviewer = binding.root.findViewById<ImageView>(R.id.imgReviewer)
            val tvReviewerName = binding.root.findViewById<TextView>(R.id.tvReviewerName)
            val tvReviewComment = binding.root.findViewById<TextView>(R.id.tvReviewComment)

            // Fade out
            imgReviewer.animate().alpha(0f).setDuration(500).withEndAction {
                imgReviewer.setImageResource(
                    if (currentReview.gender == Gender.MALE) R.drawable.male else R.drawable.female
                )
                imgReviewer.animate().alpha(1f).setDuration(500).start()
            }.start()

            tvReviewerName.animate().alpha(0f).setDuration(500).withEndAction {
                tvReviewerName.text = currentReview.reviewerName
                tvReviewerName.animate().alpha(1f).setDuration(500).start()
            }.start()

            tvReviewComment.animate().alpha(0f).setDuration(500).withEndAction {
                tvReviewComment.text = currentReview.comment
                tvReviewComment.animate().alpha(1f).setDuration(500).start()
            }.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }
}