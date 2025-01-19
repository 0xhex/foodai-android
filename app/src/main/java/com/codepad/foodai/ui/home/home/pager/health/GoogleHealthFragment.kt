package com.codepad.foodai.ui.home.home.pager.health

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoogleHealthBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleHealthFragment : BaseFragment<FragmentGoogleHealthBinding>() {
    private val sharedViewModel: HomePagerViewModel by activityViewModels()

    override fun getLayoutId(): Int = R.layout.fragment_google_health

    override fun onReadyView() {
        sharedViewModel.dailySummary.observe(viewLifecycleOwner) { dailySummary ->
            binding.apply {
                progressBar.progress = (dailySummary.healthScore?.times(10))?.toInt() ?: 0
                txtProgress.text = "${dailySummary.healthScore}/10"

                // Calculate total calories burned from exercises
                val exerciseCalories = dailySummary.exercises?.sumOf { it.caloriesBurned ?: 0 } ?: 0
                
                // TODO: Add step count calories when available
                // For now, only show exercise calories
                binding.txtBurnedCalories.text = getString(R.string.calories_burned_format, exerciseCalories)

                // Show exercise breakdown
                val runningCalories = dailySummary.exercises
                    ?.filter { it.exerciseType == "run" }
                    ?.sumOf { it.caloriesBurned ?: 0 } ?: 0
                
                val weightliftingCalories = dailySummary.exercises
                    ?.filter { it.exerciseType == "weightlifting" }
                    ?.sumOf { it.caloriesBurned ?: 0 } ?: 0
                
                val otherExerciseCalories = dailySummary.exercises
                    ?.filter { it.exerciseType != "run" && it.exerciseType != "weightlifting" }
                    ?.sumOf { it.caloriesBurned ?: 0 } ?: 0

                // Update exercise type views
                if (dailySummary.exercises?.isNotEmpty() == true) {
                    val firstExercise = dailySummary.exercises.last()
                    when (firstExercise.exerciseType) {
                        "run" -> {
                            txtFirstExerciseType.text = getString(R.string.running)
                            txtFirstExerciseCalories.text = getString(R.string.calories_burned_format, runningCalories)
                        }
                        "weightlifting" -> {
                            txtFirstExerciseType.text = getString(R.string.weightlifting)
                            txtFirstExerciseCalories.text = getString(R.string.calories_burned_format, weightliftingCalories)
                        }
                        else -> {
                            txtFirstExerciseType.text = firstExercise.exerciseType?.capitalize() ?: getString(R.string.exercise)
                            txtFirstExerciseCalories.text = getString(R.string.calories_burned_format, otherExerciseCalories)
                        }
                    }

                    // Show second exercise if exists and different type
                    if (dailySummary.exercises.size > 1) {
                        val secondExercise = dailySummary.exercises[dailySummary.exercises.size - 2]
                        if (secondExercise.exerciseType != firstExercise.exerciseType) {
                            secondExerciseLayout.visibility = View.VISIBLE
                            when (secondExercise.exerciseType) {
                                "run" -> {
                                    txtSecondExerciseType.text = getString(R.string.running)
                                    txtSecondExerciseCalories.text = getString(R.string.calories_burned_format, runningCalories)
                                }
                                "weightlifting" -> {
                                    txtSecondExerciseType.text = getString(R.string.weightlifting)
                                    txtSecondExerciseCalories.text = getString(R.string.calories_burned_format, weightliftingCalories)
                                }
                                else -> {
                                    txtSecondExerciseType.text = secondExercise.exerciseType?.capitalize() ?: getString(R.string.exercise)
                                    txtSecondExerciseCalories.text = getString(R.string.calories_burned_format, otherExerciseCalories)
                                }
                            }
                        } else {
                            secondExerciseLayout.visibility = View.GONE
                        }
                    } else {
                        secondExerciseLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }
}