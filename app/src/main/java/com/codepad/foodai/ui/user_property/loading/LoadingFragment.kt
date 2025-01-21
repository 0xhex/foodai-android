package com.codepad.foodai.ui.user_property.loading

import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentLoadingBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingFragment : BaseFragment<FragmentLoadingBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()
    private var index: Int = 0
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun getLayoutId() = R.layout.fragment_loading

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        val tvSettingUpItem = binding.root.findViewById<TextView>(R.id.tvSettingUpItem)
        val progressBar = binding.root.findViewById<ProgressBar>(R.id.progressBar)

        setupLoadingItems()

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (index < sharedViewModel.settingUpItems.size) {
                    tvSettingUpItem.text = sharedViewModel.settingUpItems[index]
                    progressBar.progress = (index + 1) * 100 / sharedViewModel.settingUpItems.size
                    index++
                    handler.postDelayed(this, 2200)
                } else {
                    findNavController().navigate(R.id.action_loadingFragment_to_resultFragment)
                }
            }
        }
        handler.post(runnable)
    }

    private fun setupLoadingItems() {
        when (sharedViewModel.loadingType) {
            LoadingType.UPLOAD_FILE -> {
                sharedViewModel.settingUpItems =
                    listOf(
                        getString(R.string.uploading),
                        getString(R.string.processing),
                        getString(R.string.finalizing)
                    )
                binding.lottieAnimationView.setAnimation(R.raw.upload_file)
                binding.tvDisplayText.text = getString(R.string.uploading_image)
            }

            LoadingType.USER_CUSTOMIZATION -> {
                sharedViewModel.settingUpItems = listOf(
                    getString(R.string.estimating_your_metabolic_age),
                    getString(R.string.processing_diet_type),
                    getString(R.string.applying_bmr_formula),
                    getString(R.string.finalizing_results)
                )
                binding.lottieAnimationView.setAnimation(R.raw.user)
                binding.tvDisplayText.text = getString(R.string.setting_up)
            }

            LoadingType.EDITING_FOOD -> {
                sharedViewModel.settingUpItems = listOf(
                    getString(R.string.analyzing_food),
                    getString(R.string.calculating_nutritional_values),
                    getString(R.string.updating_database),
                    getString(R.string.finalizing)
                )
                binding.lottieAnimationView.setAnimation(R.raw.food_calorie)
                binding.tvDisplayText.text = getString(R.string.processing_your_food_edits)
            }

            LoadingType.LOADING_DEFAULT -> {
                sharedViewModel.settingUpItems =
                    listOf("3%", "35%", "50%", "65%", getString(R.string.finalizing))
                binding.lottieAnimationView.setAnimation(R.raw.loading)
                binding.tvDisplayText.text = getString(R.string.loading)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }
}