package com.codepad.foodai.ui.home.community

import android.graphics.Color
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentCommunityTabBinding
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.community.adapter.CommunityPostsAdapter
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.ContextCompat
import android.util.Log
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class CommunityTabFragment : BaseFragment<FragmentCommunityTabBinding>() {

    private val viewModel: CommunityTabViewModel by viewModels()
    private lateinit var postsAdapter: CommunityPostsAdapter

    override fun getLayoutId(): Int = R.layout.fragment_community_tab

    override fun onReadyView() {
        setupViews()
        setupObservers()
        viewModel.fetchPosts()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTabs()
        setupClickListeners()
    }

    private fun setupViews() {
        // Setup adapter
        postsAdapter = CommunityPostsAdapter { post ->
            // Verify post has required fields before navigation
            if (post.id?.isNotEmpty() == true && post.user.id.isNotEmpty() && post.image.id.isNotEmpty()) {
                findNavController().navigate(
                    CommunityTabFragmentDirections.actionCommunityTabToCommunityPostDetail(post)
                )
            } else {
                // Show error message
                Snackbar.make(
                    binding.root,
                    "Unable to open post details",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.rvPosts.adapter = postsAdapter

        binding.btnRetry.setOnClickListener {
            viewModel?.fetchPosts()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvPosts.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.errorView.visibility = View.GONE
            Log.d("CommunityTab", "Loading state: $isLoading")
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorView.visibility = View.VISIBLE
                binding.rvPosts.visibility = View.GONE
                binding.txtError.text = getString(R.string.error_message_format, error)
                Log.e("CommunityTab", "Error loading posts: $error")
            } else {
                binding.errorView.visibility = View.GONE
            }
        }

        viewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            Log.d("CommunityTab", "Received ${posts.size} posts")
            postsAdapter.submitList(posts)
        }

        viewModel.selectedFilter.observe(viewLifecycleOwner) { filter ->
            Log.d("CommunityTab", "Filter changed to: $filter")
            updateFilterUI(filter)
        }
    }

    private fun setupTabs() {
        binding.apply {
            // Set initial texts
            btnWorld.text = getString(R.string.filter_world)
            btnCountry.text = "${getCountryFlag()} ${getString(R.string.filter_country)}"
            btnLanguage.text = getString(R.string.filter_language)
            
            // Set initial selection
            updateFilterUI(this@CommunityTabFragment.viewModel.selectedFilter.value ?: CommunityTabViewModel.FilterType.WORLD)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnWorld.setOnClickListener {
                this@CommunityTabFragment.viewModel.setFilter(CommunityTabViewModel.FilterType.WORLD)
            }
            btnCountry.setOnClickListener {
                this@CommunityTabFragment.viewModel.setFilter(CommunityTabViewModel.FilterType.COUNTRY)
            }
            btnLanguage.setOnClickListener {
                this@CommunityTabFragment.viewModel.setFilter(CommunityTabViewModel.FilterType.LANGUAGE)
            }
        }
    }

    private fun updateFilterUI(filter: CommunityTabViewModel.FilterType) {
        binding.apply {
            // Update selection states
            btnWorld.isSelected = filter == CommunityTabViewModel.FilterType.WORLD
            btnCountry.isSelected = filter == CommunityTabViewModel.FilterType.COUNTRY
            btnLanguage.isSelected = filter == CommunityTabViewModel.FilterType.LANGUAGE

            // Update text styles and alpha
            btnWorld.apply {
                alpha = if (isSelected) 1f else 0.35f
                typeface = ResourcesCompat.getFont(
                    requireContext(),
                    if (isSelected) R.font.euro_stile_bold else R.font.euro_stile_regular
                )
            }

            btnCountry.apply {
                text = "${getCountryFlag()} ${getString(R.string.filter_country)}"
                alpha = if (isSelected) 1f else 0.35f
                typeface = ResourcesCompat.getFont(
                    requireContext(),
                    if (isSelected) R.font.euro_stile_bold else R.font.euro_stile_regular
                )
            }

            btnLanguage.apply {
                alpha = if (isSelected) 1f else 0.35f
                typeface = ResourcesCompat.getFont(
                    requireContext(),
                    if (isSelected) R.font.euro_stile_bold else R.font.euro_stile_regular
                )
            }
        }
    }

    private fun getCountryFlag(): String {
        val countryCode = UserSession.user?.countryCode ?: return "🏳️"
        val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
} 