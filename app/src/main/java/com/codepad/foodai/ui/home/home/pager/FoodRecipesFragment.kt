package com.codepad.foodai.ui.home.home.pager

import androidx.fragment.app.viewModels
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
        // Initialize your view here
    }
}