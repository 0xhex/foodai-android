package com.codepad.foodai.ui.home.home.pager.recipe

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentFoodRecipesBinding
import com.codepad.foodai.domain.models.recipe.MealType
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodRecipesFragment : BaseFragment<FragmentFoodRecipesBinding>() {
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: RecipeCardAdapter
    private val mealTypes = MealType.entries

    override fun getLayoutId(): Int = R.layout.fragment_food_recipes

    override fun onReadyView() {
        setupRecyclerView()
        observeRecipeStates()
        
        // Check for saved recipes when the fragment is created
        mealTypes.forEach { mealType ->
            viewModel.checkSavedRecipe(mealType.codeName)
        }
    }

    private fun setupRecyclerView() {
        adapter = RecipeCardAdapter(
            mealTypes = mealTypes,
            onCreateRecipeClick = { mealType ->
                viewModel.generateRecipe(mealType.codeName)
            },
            onViewRecipeClick = { recipe ->
                // TODO: Navigate to recipe detail view
                // findNavController().navigate(...)
            }
        )
        
        binding.rvRecipes.layoutManager = 
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecipes.adapter = adapter
    }

    private fun observeRecipeStates() {
        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe?.let {
                adapter.updateRecipe(it.mealType, it)
            }
        }

        viewModel.isRecipeLoading.observe(viewLifecycleOwner) { isLoading ->
            mealTypes.forEach { mealType ->
                adapter.updateLoadingState(mealType.codeName, isLoading)
            }
        }

        viewModel.recipeError.observe(viewLifecycleOwner) { error ->
            mealTypes.forEach { mealType ->
                adapter.updateErrorState(mealType.codeName, error)
            }
        }

        viewModel.isPremiumRequired.observe(viewLifecycleOwner) { isPremiumRequired ->
            mealTypes.forEach { mealType ->
                adapter.updatePremiumRequired(mealType.codeName, isPremiumRequired)
            }
        }
    }
}