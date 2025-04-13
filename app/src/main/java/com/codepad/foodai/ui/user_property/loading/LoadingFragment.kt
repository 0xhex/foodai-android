package com.codepad.foodai.ui.user_property.loading

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentLoadingBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LoadingFragment : BaseFragment<FragmentLoadingBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()
    private var currentStepIndex: Int = 0
    private var currentPercentage: Int = 0
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var stepsAdapter: LoadingStepsAdapter

    override fun getLayoutId() = R.layout.fragment_loading

    override fun onReadyView() {
        binding.viewModel = sharedViewModel
        setupLoadingItems()
        setupRecyclerView()
        startLoadingAnimation()
    }

    private fun setupRecyclerView() {
        stepsAdapter = LoadingStepsAdapter(sharedViewModel.settingUpItems)
        binding.stepsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stepsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupLoadingItems() {
        when (sharedViewModel.loadingType) {
            LoadingType.UPLOAD_FILE -> {
                sharedViewModel.settingUpItems = listOf(
                    getString(R.string.uploading),
                    getString(R.string.processing),
                    getString(R.string.finalizing)
                )
                binding.tvDisplayText.text = getString(R.string.uploading_image)
            }
            LoadingType.USER_CUSTOMIZATION -> {
                sharedViewModel.settingUpItems = listOf(
                    getString(R.string.analyzing_your_answers),
                    getString(R.string.calculating_your_calorie_goal),
                    getString(R.string.estimating_your_progress),
                    getString(R.string.finalizing_your_plan)
                )
                binding.tvDisplayText.text = getString(R.string.setting_up)
            }
            LoadingType.EDITING_FOOD -> {
                sharedViewModel.settingUpItems = listOf(
                    getString(R.string.analyzing_food),
                    getString(R.string.calculating_nutritional_values),
                    getString(R.string.updating_database),
                    getString(R.string.finalizing)
                )
                binding.tvDisplayText.text = getString(R.string.processing_your_food_edits)
            }
            LoadingType.LOADING_DEFAULT -> {
                sharedViewModel.settingUpItems = listOf(
                    "Loading...",
                    "Processing...",
                    "Almost there...",
                    getString(R.string.finalizing)
                )
                binding.tvDisplayText.text = getString(R.string.loading)
            }
        }
    }

    private fun startLoadingAnimation() {
        val totalSteps = sharedViewModel.settingUpItems.size
        val incrementsPerStep = 20 // Same as iOS
        val totalIncrements = totalSteps * incrementsPerStep
        var incrementCounter = 0

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                incrementCounter++
                
                // Update percentage with smooth animation
                val targetPercentage = (incrementCounter * 100) / totalIncrements
                animatePercentage(currentPercentage, targetPercentage)
                currentPercentage = targetPercentage
                
                // Update step index with animation
                val newStepIndex = incrementCounter / incrementsPerStep
                if (newStepIndex < totalSteps && newStepIndex != currentStepIndex) {
                    currentStepIndex = newStepIndex
                    animateStepTransition(currentStepIndex)
                }

                if (incrementCounter < totalIncrements) {
                    handler.postDelayed(this, 100) // 0.1s delay like iOS
                } else {
                    // Complete all animations first
                    completeLoadingAnimations {
                        // Then navigate after 2 seconds
                        handler.postDelayed({
                            try {
                                if (!isAdded || isDetached) {
                                    Timber.w("Fragment not attached, skipping navigation")
                                    return@postDelayed
                                }
                                
                                val navController = findNavController()
                                if (navController.currentDestination?.id == R.id.loadingFragment) {
                                    navController.navigate(R.id.action_loadingFragment_to_newResultFragment)
                                } else {
                                    Timber.w("Invalid navigation state: current destination is not loadingFragment")
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Navigation error")
                            }
                        }, 2000) // 2 seconds delay before navigation
                    }
                }
            }
        }
        handler.post(runnable)
    }

    private fun completeLoadingAnimations(onComplete: () -> Unit) {
        val totalSteps = sharedViewModel.settingUpItems.size
        
        // Animate to 100% progress
        animatePercentage(currentPercentage, 100)
        
        // Update the last step with animation
        animateStepTransition(totalSteps - 1) {
            // Animate linear progress to 100%
            ValueAnimator.ofInt(binding.linearProgress.progress, 100).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animator ->
                    binding.linearProgress.progress = animator.animatedValue as Int
                }
                start()
            }
            
            // Call completion callback after animations are done
            handler.postDelayed(onComplete, 300)
        }
    }

    private fun animateStepTransition(newStep: Int, onComplete: (() -> Unit)? = null) {
        stepsAdapter.updateCurrentStep(newStep)
        
        // Animate linear progress
        val progressTarget = (newStep * 100) / (sharedViewModel.settingUpItems.size - 1)
        ValueAnimator.ofInt(binding.linearProgress.progress, progressTarget).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                binding.linearProgress.progress = animator.animatedValue as Int
            }
            if (onComplete != null) {
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        onComplete()
                    }
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
            }
            start()
        }
    }

    private fun animatePercentage(from: Int, to: Int) {
        ValueAnimator.ofInt(from, to).apply {
            duration = 300 // Longer duration for smoother animation
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                binding.percentageText.text = "$value%"
                binding.circularProgress.progress = value
            }
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }
}

class LoadingStepsAdapter(
    private val items: List<String>
) : RecyclerView.Adapter<LoadingStepsAdapter.ViewHolder>() {

    private var currentStep = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stepIcon: ImageView = view.findViewById(R.id.stepIcon)
        val stepText: TextView = view.findViewById(R.id.stepText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loading_step, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.stepText.text = items[position]
        
        // Animate text alpha
        holder.stepText.animate()
            .alpha(if (position <= currentStep) 1f else 0.6f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Set and animate icon
        if (position <= currentStep) {
            holder.stepIcon.setImageResource(R.drawable.ic_check_circle)
            holder.stepIcon.scaleX = 0.9f
            holder.stepIcon.scaleY = 0.9f
            holder.stepIcon.alpha = 0f
            holder.stepIcon.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } else {
            holder.stepIcon.setImageResource(R.drawable.ic_circle)
            holder.stepIcon.scaleX = 1f
            holder.stepIcon.scaleY = 1f
            holder.stepIcon.alpha = 1f
        }
    }

    override fun getItemCount() = items.size

    fun updateCurrentStep(step: Int) {
        val oldStep = currentStep
        currentStep = step
        notifyItemRangeChanged(oldStep, step + 1)
    }
}