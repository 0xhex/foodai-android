package com.codepad.foodai.ui.home.community.detail

import CommentsAdapter
import LikedUsersAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentCommunityPostDetailBinding
import com.codepad.foodai.domain.models.community.CommunityPost
import com.codepad.foodai.domain.models.community.CommunityUser
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.community.detail.adapter.IngredientsAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import toShortTimeString

@AndroidEntryPoint
class CommunityPostDetailFragment : BaseFragment<FragmentCommunityPostDetailBinding>() {

    private val viewModel: CommunityPostDetailViewModel by viewModels()
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var likedUsersAdapter: LikedUsersAdapter
    override val hideBottomNavBar: Boolean = true

    private var isFoodDetailsExpanded = false
    private var isLikedUsersExpanded = false

    override fun getLayoutId(): Int = R.layout.fragment_community_post_detail

    override fun onReadyView() {
        arguments?.getParcelable<CommunityPost>("post")?.let { post ->
            viewModel.setPost(post)
        }

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        with(binding) {
            // Back button
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            // Setup comments adapter
            commentsAdapter =
                CommentsAdapter(currentUserId = UserSession.user?.id, onDeleteClick = { commentId ->
                    this@CommunityPostDetailFragment.viewModel.deleteComment(commentId)
                })
            sectionComments.apply {
                rvComments.adapter = commentsAdapter
                btnSend.setOnClickListener {
                    val commentText = sectionComments.editComment.text.toString().trim()
                    if (commentText.isNotEmpty()) {
                        this@CommunityPostDetailFragment.viewModel.addComment(commentText)
                        sectionComments.editComment.text?.clear()

                        // Hide keyboard
                        val imm =
                            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(sectionComments.editComment.windowToken, 0)

                        // Show loading indicator if needed
                        // sectionComments.progressBar.isVisible = true
                    }
                }
            }

            // Setup liked users adapter
            likedUsersAdapter = LikedUsersAdapter()
            sectionLikes.contentContainer.adapter = likedUsersAdapter

            // Setup collapsible sections
            sectionFoodDetails.headerContainer.setOnClickListener {
                isFoodDetailsExpanded = !isFoodDetailsExpanded
                toggleFoodDetailsSection(isFoodDetailsExpanded)
            }

            sectionLikes.headerContainer.setOnClickListener {
                isLikedUsersExpanded = !isLikedUsersExpanded
                toggleLikesSection(isLikedUsersExpanded)
            }

            // Like button
            sectionMeta.btnLike.setOnClickListener {
                this@CommunityPostDetailFragment.viewModel.toggleLike()
            }

            // Add this to setupViews method:
            sectionFoodDetails.ingredientsSection.root.setOnClickListener {
                val isExpanded = sectionFoodDetails.ingredientsSection.rvIngredients.isVisible
                sectionFoodDetails.ingredientsSection.rvIngredients.isVisible = !isExpanded
                sectionFoodDetails.ingredientsSection.imgIngredientsExpand.rotation =
                    if (!isExpanded) 180f else 0f
            }
        }
    }

    private fun setupObservers() {
        viewModel.post.observe(viewLifecycleOwner) { post ->
            updateUI(post)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showSnackbar(it)
                //viewModel.errorMessage.value = null
            }
        }

        viewModel.isLiked.observe(viewLifecycleOwner) { isLiked ->
            updateLikeButton(isLiked)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Update loading indicators if needed
            // binding.sectionComments.progressBar.isVisible = isLoading
        }
    }

    private fun updateUI(post: CommunityPost) {
        with(binding) {
            // Load food image
            Glide.with(requireContext()).load(post.image.url)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.dimen_16dp)))
                .into(imgFood)

            // Set meal type
            txtMealType.text = post.image.mealName?.uppercase()

            // Update meta section
            sectionMeta.apply {
                // User info
                if (!post.user.profilePicUrl.isNullOrEmpty()) {
                    Glide.with(requireContext()).load(post.user.profilePicUrl).circleCrop()
                        .into(imgProfile)
                    txtProfileLetter.visibility = android.view.View.GONE
                } else {
                    imgProfile.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.gray
                        )
                    )
                    txtProfileLetter.visibility = android.view.View.VISIBLE
                    txtProfileLetter.text = post.user.name?.firstOrNull()?.uppercase().toString()
                }

                txtUsername.text = post.user.name
                txtCountryFlag.text = getCountryFlag(post.user.countryCode)
                txtDescription.text = post.image.description

                // Update stats chips
                updateUserStats(post.user)

                // Likes and date
                txtLikesCount.text = "${post.likes?.size ?: 0} likes"
                txtDate.text = post.createdAt?.toShortTimeString()

                // Update like button
                btnLike.apply {
                    text = if (post.likes?.any { it.id == UserSession.user?.id } == true) {
                        getString(R.string.unlike)
                    } else {
                        getString(R.string.like)
                    }
                    setIconResource(if (post.likes?.any { it.id == UserSession.user?.id } == true) {
                        R.drawable.ic_heart_filled
                    } else {
                        R.drawable.ic_heart
                    })
                }

                // Only show likes section if there are likes
                post.likes?.let { likes ->
                    if (likes.isNotEmpty()) {
                        sectionLikes.root.isVisible = true

                        // Setup likes section click listener
                        sectionLikes.headerContainer.setOnClickListener {
                            isLikedUsersExpanded = !isLikedUsersExpanded
                            toggleLikesSection(isLikedUsersExpanded)
                        }

                        // Setup liked users adapter
                        likedUsersAdapter = LikedUsersAdapter()
                        sectionLikes.contentContainer.adapter = likedUsersAdapter
                        likedUsersAdapter.submitList(likes)
                    } else {
                        sectionLikes.root.isVisible = false
                    }
                } ?: run {
                    sectionLikes.root.isVisible = false
                }
            }

            // Update food details
            sectionFoodDetails.apply {
                txtCalories.text = "${post.image.calories} kcal"
                txtProtein.text = "${post.image.protein}g"
                txtCarbs.text = "${post.image.carbs}g"
                txtFats.text = "${post.image.fats}g"
            }

            // Update comments
            sectionComments.txtCommentsCount.text =
                getString(R.string.comments_count, post.comments?.size ?: 0)
            commentsAdapter.submitList(post.comments)

            // Update likes section visibility and content
            post.likes?.let { likes ->
                binding.sectionLikes.apply {
                    root.isVisible = likes.isNotEmpty()
                    if (likes.isNotEmpty()) {
                        likedUsersAdapter.submitList(likes)
                    }
                }
            }

            // Setup ingredients adapter if needed
            val ingredientsAdapter = IngredientsAdapter()
            sectionFoodDetails.ingredientsSection.rvIngredients.adapter = ingredientsAdapter
            ingredientsAdapter.submitList(post.image.ingredients)
        }
    }

    private fun updateUserStats(user: CommunityUser) {
        binding.sectionMeta.apply {
            // Goal
            chipGoal.apply {
                imgIcon.setImageResource(R.drawable.ic_target)
                txtLabel.text = getString(R.string.goal_placeholder)
                txtValue.text = user.goal?.toDisplayString()
            }

            // Weight
            chipWeight.apply {
                imgIcon.setImageResource(R.drawable.ic_weight)
                txtLabel.text = getString(R.string.weight)
                txtValue.text = if (user.isMetric == true) {
                    "${user.weight}kg"
                } else {
                    "${user.weight?.times(2.205)?.toInt()}lbs"
                }
            }

            // Target weight
            chipTarget.apply {
                imgIcon.setImageResource(R.drawable.ic_target_weight)
                txtLabel.text = getString(R.string.target)
                txtValue.text = if (user.isMetric == true) {
                    "${user.targetWeight}kg"
                } else {
                    "${user.targetWeight?.times(2.205)?.toInt()}lbs"
                }
            }

            // Workouts
            chipWorkouts.apply {
                this.imgIcon.setImageResource(R.drawable.ic_run)
                this.txtLabel.text = getString(R.string.workouts)
                this.txtValue.text = user.workoutsPerWeek
            }

            // Diet
            chipDiet.apply {
                this.imgIcon.setImageResource(R.drawable.ic_diet)
                this.txtLabel.text = getString(R.string.diet)
                this.txtValue.text = user.dietType?.toDisplayString()
            }
        }
    }

    private fun toggleFoodDetailsSection(show: Boolean) {
        binding.sectionFoodDetails.contentContainer.isVisible = show
        binding.sectionFoodDetails.imgExpand.rotation = if (show) 180f else 0f
    }

    private fun toggleLikesSection(show: Boolean) {
        binding.sectionLikes.apply {
            contentContainer.isVisible = show
            imgExpand.rotation = if (show) 180f else 0f
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun getCountryFlag(countryCode: String?): String {
        if (countryCode == null || countryCode.length != 2) return ""

        val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6

        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }

    fun String?.toDisplayString(): String {
        return when (this) {
            "lose_weight" -> getString(R.string.goal_lose_weight)
            "maintain" -> getString(R.string.goal_maintain)
            "gain_weight" -> getString(R.string.goal_gain_weight)
            else -> "Unknown"
        }
    }

    private fun updateLikeButton(isLiked: Boolean) {
        binding.sectionMeta.btnLike.apply {
            text = if (isLiked) getString(R.string.unlike) else getString(R.string.like)
            icon = ContextCompat.getDrawable(
                requireContext(), if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
            )
            iconTint = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), if (isLiked) R.color.red else R.color.gray)
            )
        }
    }
} 