package com.codepad.foodai.ui.home.home.pager.recipe

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentFoodRecipesBinding
import com.codepad.foodai.domain.models.recipe.MealType
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeFragmentDirections
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.home.HomeTabFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodRecipesFragment : BaseFragment<FragmentFoodRecipesBinding>() {
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: RecipeCardAdapter
    private val mealTypes = MealType.entries
    private var currentLoadingMealType: String? = null

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
                currentLoadingMealType = mealType.codeName
                viewModel.generateRecipe(mealType.codeName)
            },
            onViewRecipeClick = { recipe ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeTabToRecipeDetail(recipe)
                )
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
            currentLoadingMealType?.let { mealType ->
                adapter.updateLoadingState(mealType, isLoading)
                if (!isLoading) {
                    currentLoadingMealType = null
                }
            }
        }

        viewModel.recipeError.observe(viewLifecycleOwner) { error ->
            currentLoadingMealType?.let { mealType ->
                adapter.updateErrorState(mealType, error)
                currentLoadingMealType = null
            }
        }

        viewModel.isPremiumRequired.observe(viewLifecycleOwner) { isPremiumRequired ->
            currentLoadingMealType?.let { mealType ->
                adapter.updatePremiumRequired(mealType, isPremiumRequired)
                currentLoadingMealType = null
            }
        }
    }
}