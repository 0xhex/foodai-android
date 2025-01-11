package com.codepad.foodai.ui.home.home.pager.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentRecipeDetailBinding
import com.codepad.foodai.databinding.LayoutRecipeSectionBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeDetailFragment : BaseFragment<FragmentRecipeDetailBinding>() {
    private val args: RecipeDetailFragmentArgs by navArgs()

    override fun getLayoutId(): Int = R.layout.fragment_recipe_detail
    override fun onReadyView() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecipeDetail()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecipeDetail() {
        binding.recipe = args.recipe

        // Setup Why This Recipe section
        setupSection(
            binding.whyThisRecipeSection,
            listOf(args.recipe.personalizedExplanation ?: "")
        )

        // Setup Ingredients section
        setupSection(
            binding.ingredientsSection,
            args.recipe.ingredients?.map { "• $it" } ?: emptyList()
        )

        // Setup Instructions section
        setupSection(
            binding.instructionsSection,
            args.recipe.instructions?.mapIndexed { index, instruction ->
                "${index + 1}. $instruction"
            } ?: emptyList()
        )

        // Setup Cooking Tips section if available
        if (!args.recipe.cookingTips.isNullOrEmpty()) {
            setupSection(
                binding.cookingTipsSection,
                args.recipe.cookingTips?.map { "• $it" } ?: emptyList()
            )
        } else {
            binding.cookingTipsSection.root.visibility = View.GONE
        }

        // Setup Nutritional Information section
        setupSection(
            binding.nutritionalInfoSection,
            args.recipe.nutritionalInformation?.mapIndexed { index, info ->
                "${index + 1}. $info"
            } ?: emptyList()
        )

        // Log recipe opened event
        // TODO: Implement Firebase analytics
        // FirebaseAnalytics.logEvent("recipe_opened", bundleOf("recipe_id" to args.recipe.id))
    }

    private fun setupSection(
        sectionBinding: LayoutRecipeSectionBinding,
        items: List<String>
    ) {
        sectionBinding.sectionContent.adapter = RecipeSectionAdapter(items)
    }
}

class RecipeSectionAdapter(private val items: List<String>) :
    RecyclerView.Adapter<RecipeSectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_section_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.text)

        fun bind(text: String) {
            textView.text = text
        }
    }
} 