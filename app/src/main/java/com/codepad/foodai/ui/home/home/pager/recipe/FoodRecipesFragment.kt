package com.codepad.foodai.ui.home.home.pager.recipe

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentFoodRecipesBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodRecipesFragment : BaseFragment<FragmentFoodRecipesBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.fragment_food_recipes

    override fun onReadyView() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val mealTypes = listOf("Breakfast", "Lunch", "Dinner")
        val adapter = RecipeCardAdapter(mealTypes)
        binding.rvRecipes.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecipes.adapter = adapter
    }
}