package com.codepad.foodai.ui.user_property.result

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentNewResultBinding
import com.codepad.foodai.databinding.ItemMacroNutrientBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * New Result Fragment that displays the user's nutrition plan with animations
 * Preserves all functionality from original ResultFragment but with updated UI
 */
@AndroidEntryPoint
class NewResultFragment : BaseFragment<FragmentNewResultBinding>() {
    
    private val viewModel: ResultViewModel by activityViewModels()
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val args: NewResultFragmentArgs by navArgs()
    
    // Flag to track if animations have already run
    private var animationsShown = false
    
    override fun getLayoutId() = R.layout.fragment_new_result
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        
        // Add particle view and configure to run animation only first time
        // Only show particles for new user mode, not for adjust goals mode
        if (!args.isAdjustGoals) {
            val particleView = ParticleView(requireContext()).apply {
                shouldRunAnimation = !animationsShown
            }
            binding.particleContainer.addView(particleView)
        }
        
        return view
    }
    
    override fun onReadyView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        
        // Configure UI based on view mode
        configureViewMode()
        
        // Initially hide header until animation
        binding.headerContainer.alpha = 0f
        binding.headerContainer.translationY = 20f
        binding.bottomContainer.alpha = 1f // Bottom container stays visible
        
        setUpObservers()
        setUpListeners()
        configureNutritionItems()
        setupEditButtonListeners()
        
        // Start loading data
        viewModel.fetchNutrition()
        
        // Listen for nutrition updates from EditCaloriesFragment
        setFragmentResultListener("updateRequestKey") { _, bundle ->
            if (bundle.getBoolean("updated")) {
                viewModel.fetchNutrition()
            }
        }
        
        // Only animate if it's the first time
        if (!animationsShown) {
            // Begin entrance animations after a short delay
            mainScope.launch {
                delay(200)
                animateEntrance()
                animationsShown = true
            }
        } else {
            // If returning from EditCaloriesFragment, just show everything immediately
            binding.headerContainer.alpha = 1f
            binding.headerContainer.translationY = 0f
            
            binding.calorieCircleContainer.alpha = 1f
            binding.calorieProgressCircle.progress = 100
            binding.calorieEditButton.alpha = 1f
            
            binding.macroNutrientsContainer.alpha = 1f
            
            // Set up macros visible without animation
            showMacroItemsWithoutAnimation(binding.fatsMacro)
            showMacroItemsWithoutAnimation(binding.proteinMacro)
            showMacroItemsWithoutAnimation(binding.carbsMacro)
        }
    }
    
    private fun configureViewMode() {
        if (args.isAdjustGoals) {
            // Update UI for adjust goals mode
            binding.titleText.text = getString(R.string.your_nutrition_goals)
            binding.subtitleText.text = getString(R.string.current_nutrition_goals)
            binding.descriptionText.text = getString(R.string.tap_values_to_adjust)
            binding.nextButton.text = getString(R.string.done)
        } else {
            // Default UI for new user mode
            binding.titleText.text = getString(R.string.congratulations_custom_plan)
            binding.subtitleText.text = getString(R.string.daily_recommendation)
            binding.descriptionText.text = getString(R.string.you_can_edit_this_anytime)
            binding.nextButton.text = getString(R.string.next)
        }
    }
    
    private fun showMacroItemsWithoutAnimation(binding: ItemMacroNutrientBinding) {
        binding.glowCircle.scaleX = 1f
        binding.glowCircle.scaleY = 1f
        binding.valueCircle.scaleX = 1f
        binding.valueCircle.scaleY = 1f
        binding.macroValueText.alpha = 1f
        binding.macroTitleText.alpha = 1f
        binding.editButton.alpha = 1f
    }
    
    private fun setUpObservers() {
        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Error messages
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                binding.errorMessageText.text = errorMessage
                binding.errorOverlay.visibility = View.VISIBLE
            } else {
                binding.errorOverlay.visibility = View.GONE
            }
        })
    }
    
    private fun setUpListeners() {
        // Next button
        binding.nextButton.setOnClickListener {
            if (args.isAdjustGoals) {
                // Just go back when in adjust goals mode
                findNavController().popBackStack()
            } else {
                // Navigate to next screen in new user mode
                viewModel.navigateToNextScreen()
                findNavController().navigate(R.id.action_newResultFragment_to_homeFragment)
            }
        }
        
        // Error OK button
        binding.errorOkButton.setOnClickListener {
            viewModel.clearErrorMessage()
        }
    }
    
    private fun configureNutritionItems() {
        // Configure macro items with data binding with specific drawables for each
        configureMacroItem(binding.fatsMacro, viewModel.fats, R.color.orange, R.drawable.macro_circle_glow_fats)
        configureMacroItem(binding.proteinMacro, viewModel.protein, R.color.red, R.drawable.macro_circle_glow_protein)
        configureMacroItem(binding.carbsMacro, viewModel.carbs, R.color.blue_button, R.drawable.macro_circle_glow_carbs)
    }
    
    private fun configureMacroItem(
        binding: ItemMacroNutrientBinding,
        nutritionData: androidx.lifecycle.LiveData<Nutrition>,
        tintColorRes: Int,
        glowDrawableRes: Int
    ) {
        // Get the current value safely
        nutritionData.value?.let { nutrition ->
            binding.nutrition = nutrition
        }
        
        // Set the glow background to the specific drawable
        binding.glowCircle.setBackgroundResource(glowDrawableRes)
        
        // Set the edit icon color
        binding.editIcon.setColorFilter(resources.getColor(tintColorRes, null))
        
        // Update the nutrition data when it changes
        nutritionData.observe(viewLifecycleOwner) { nutrition ->
            binding.nutrition = nutrition
            binding.executePendingBindings()
        }
    }
    
    private fun setupEditButtonListeners() {
        // Calories edit button
        binding.calorieEditButton.setOnClickListener {
            val caloriesValue = viewModel.calories.value?.value ?: "0"
            navigateToEdit(getString(R.string.calories), "dailyCalories", caloriesValue, ContextCompat.getColor(requireContext(), R.color.glow_carbs))
        }
        
        // Setup macro edit buttons with matching iOS colors
        setupMacroEditButton(binding.fatsMacro, getString(R.string.fats), "dailyFat", viewModel.fats, ContextCompat.getColor(requireContext(), R.color.glow_fats))
        setupMacroEditButton(binding.proteinMacro, getString(R.string.protein), "dailyProtein", viewModel.protein, ContextCompat.getColor(requireContext(), R.color.glow_protein))
        setupMacroEditButton(binding.carbsMacro, getString(R.string.carbs), "dailyCarb", viewModel.carbs, ContextCompat.getColor(requireContext(), R.color.glow_carbs))
    }
    
    private fun setupMacroEditButton(
        binding: ItemMacroNutrientBinding,
        title: String,
        field: String,
        nutritionData: androidx.lifecycle.LiveData<Nutrition>,
        color: Int
    ) {
        binding.root.setOnClickListener {
            val value = nutritionData.value?.value ?: "0"
            navigateToEdit(title, field, value, color)
        }
    }
    
    private fun navigateToEdit(title: String, field: String, value: String, color: Int) {
        val bundle = Bundle().apply {
            putString("title", title)
            putString("textAreaTitle", getString(R.string.enter_value, title))
            putString("nutritionType", field)
            putString("value", value)
            putInt("color", color)
        }
        
        findNavController().navigate(R.id.action_newResultFragment_to_editCaloriesFragment, bundle)
    }
    
    /**
     * Animates only the calorie and macro nutrient elements, like iOS
     */
    private fun animateEntrance() {
        // First animate header with a nice fade-in and slide-up effect
        val headerAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.headerContainer, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(binding.headerContainer, "translationY", 20f, 0f)
            )
            duration = 600
            interpolator = DecelerateInterpolator()
        }
        
        // Animate calorie circle
        val calorieCircleAnim = AnimatorSet().apply {
            // First make it visible
            val fadeIn = ObjectAnimator.ofFloat(binding.calorieCircleContainer, "alpha", 0f, 1f)
            fadeIn.duration = 300
            
            // Then animate progress circle
            val progressAnim = ValueAnimator.ofInt(0, 100).apply {
                addUpdateListener { animator ->
                    binding.calorieProgressCircle.progress = animator.animatedValue as Int
                }
                duration = 800
                interpolator = AccelerateDecelerateInterpolator()
                startDelay = 300
            }
            
            // Add 3D rotation effect
            val rotationAnim = ObjectAnimator.ofFloat(binding.calorieCircle, "rotationX", 30f, 0f)
            rotationAnim.duration = 800
            rotationAnim.interpolator = DecelerateInterpolator()
            
            // Animate edit button
            val editButtonAnim = ObjectAnimator.ofFloat(binding.calorieEditButton, "alpha", 0f, 1f)
            editButtonAnim.duration = 300
            editButtonAnim.startDelay = 900
            
            playTogether(fadeIn, progressAnim, rotationAnim, editButtonAnim)
        }
        
        // Animate macro nutrients
        val macroAnimations = AnimatorSet()
        val macroAnims = ArrayList<Animator>()
        
        // First make container visible
        macroAnims.add(ObjectAnimator.ofFloat(binding.macroNutrientsContainer, "alpha", 0f, 1f).apply {
            duration = 300
        })
        
        // Add animations for each macro (fats, protein, carbs) with staggered delays
        animateMacroItem(binding.fatsMacro, 0, macroAnims)
        animateMacroItem(binding.proteinMacro, 100, macroAnims)
        animateMacroItem(binding.carbsMacro, 200, macroAnims)
        
        macroAnimations.playTogether(macroAnims)
        
        // Combine all animations in sequence
        val completeAnimation = AnimatorSet()
        completeAnimation.playSequentially(headerAnim, calorieCircleAnim, macroAnimations)
        completeAnimation.start()
    }
    
    private fun animateMacroItem(binding: ItemMacroNutrientBinding, delay: Long, animations: ArrayList<Animator>) {
        // Glow circle scale animation
        val glowScaleX = ObjectAnimator.ofFloat(binding.glowCircle, "scaleX", 0f, 1f).apply {
            duration = 500
            startDelay = delay
            interpolator = OvershootInterpolator(0.7f)
        }
        
        val glowScaleY = ObjectAnimator.ofFloat(binding.glowCircle, "scaleY", 0f, 1f).apply {
            duration = 500
            startDelay = delay
            interpolator = OvershootInterpolator(0.7f)
        }
        
        // Main circle scale animation
        val circleScaleX = ObjectAnimator.ofFloat(binding.valueCircle, "scaleX", 0f, 1f).apply {
            duration = 500
            startDelay = delay + 100
            interpolator = OvershootInterpolator(0.7f)
        }
        
        val circleScaleY = ObjectAnimator.ofFloat(binding.valueCircle, "scaleY", 0f, 1f).apply {
            duration = 500
            startDelay = delay + 100
            interpolator = OvershootInterpolator(0.7f)
        }
        
        // Text fade in
        val textFade = ObjectAnimator.ofFloat(binding.macroValueText, "alpha", 0f, 1f).apply {
            duration = 300
            startDelay = delay + 200
        }
        
        // Label fade in
        val labelFade = ObjectAnimator.ofFloat(binding.macroTitleText, "alpha", 0f, 1f).apply {
            duration = 300
            startDelay = delay + 300
        }
        
        // Edit button fade in
        val editFade = ObjectAnimator.ofFloat(binding.editButton, "alpha", 0f, 1f).apply {
            duration = 300
            startDelay = delay + 400
        }
        
        // Add all animations
        animations.add(glowScaleX)
        animations.add(glowScaleY)
        animations.add(circleScaleX)
        animations.add(circleScaleY)
        animations.add(textFade)
        animations.add(labelFade)
        animations.add(editFade)
    }
    
    companion object {
        const val ARG_IS_ADJUST_GOALS = "isAdjustGoals"
    }
} 