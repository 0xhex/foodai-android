package com.codepad.foodai.ui.home.home.fooddetail

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentFoodDetailBinding
import com.codepad.foodai.extensions.toHourString
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodDetailFragment : BaseFragment<FragmentFoodDetailBinding>() {
    private val sharedViewModel: HomePagerViewModel by activityViewModels()
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_food_detail

    override fun onReadyView() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnShare.setOnClickListener {
            // Implement share functionality
        }

        binding.btnDelete.setOnClickListener {
            // Implement delete functionality
        }

        sharedViewModel.foodDetail.observe(viewLifecycleOwner) { foodDetail ->
            binding.apply {
                txtTime.text = "⏱ ${foodDetail.createdAt?.toHourString()}"
                txtTitle.text = foodDetail.ingredients?.firstOrNull()?.name ?: "N/A"
                Glide.with(binding.imgFood)
                    .load(foodDetail.url)
                    .centerCrop()
                    .into(binding.imgFood)
                val nutritionItems = foodDetail.ingredients?.map {
                    NutritionItem(
                        it.name.orEmpty(),
                        it.calory.toString()
                    )
                }
                val adapter = nutritionItems?.let { NutritionAdapter(it) }
                binding.rvNutritions.layoutManager = GridLayoutManager(context, 2)
                binding.rvNutritions.adapter = adapter

                binding.txtCaloriesDesc.text = foodDetail.calories.toString() + " kcal"
                binding.txtProteinDesc.text = foodDetail.protein.toString() + " g"
                binding.txtCarbDesc.text = foodDetail.carbs.toString() + " g"
                binding.txtFatDesc.text = foodDetail.fats.toString() + " g"

                binding.apply {
                    progressBar.progress = (foodDetail.healthScore?.times(10)) ?: 0
                    txtProgress.text = "${foodDetail.healthScore}/10"
                }

            }
        }

        binding.btnFix.setOnClickListener {
            // Navigate to FixResultFragment
        }

        binding.btnSave.setOnClickListener {
            // Implement save functionality
        }
    }

}