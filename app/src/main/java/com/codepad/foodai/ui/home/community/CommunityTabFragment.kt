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
        
        // Set filter button texts with emojis
        binding.btnWorld.text = "🌍 World"
        binding.btnCountry.text = "${getCountryFlag()} Country"
        binding.btnLanguage.text = "🗣️ Language"
        
        // ... rest of your code
    }

    private fun setupViews() {
        // Setup adapter
        postsAdapter = CommunityPostsAdapter { post ->
            // Navigate to post detail
            // findNavController().navigate(
            //     CommunityTabFragmentDirections.actionCommunityTabToCommunityPostDetail(post.id)
            //)
        }

        binding.rvPosts.adapter = postsAdapter

        // Setup filter buttons
        binding.apply {
            btnWorld.setOnClickListener {
                viewModel?.setFilter(CommunityTabViewModel.FilterType.WORLD)
            }
            btnCountry.setOnClickListener {
                viewModel?.setFilter(CommunityTabViewModel.FilterType.COUNTRY)
            }
            btnLanguage.setOnClickListener {
                viewModel?.setFilter(CommunityTabViewModel.FilterType.LANGUAGE)
            }

            // Set initial country text
            btnCountry.text = "${viewModel?.getCountryFlag(UserSession.user?.countryCode)} Country"

            btnWorld.text = "\uD83C\uDF0D World"  // 🌍
            btnLanguage.text = "\uD83D\uDDE3 Language"  // 🗣️

            // Country text will be set when updating filter UI
            viewModel?.selectedFilter?.value?.let { filter ->
                updateFilterUI(filter)
            } ?: updateFilterUI(CommunityTabViewModel.FilterType.WORLD)
        }

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
                binding.txtError.text = "Error: $error"
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

    private fun updateFilterUI(filter: CommunityTabViewModel.FilterType) {
        binding.apply {
            // Update text styles
            btnWorld.apply {
                isSelected = filter == CommunityTabViewModel.FilterType.WORLD
                alpha = if (isSelected) 1f else 0.35f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                typeface = ResourcesCompat.getFont(
                    requireContext(),
                    if (isSelected) R.font.euro_stile_bold else R.font.euro_stile_regular
                )
            }

            btnCountry.apply {
                text = "${viewModel?.getCountryFlag(UserSession.user?.countryCode)} Country"
                isSelected = filter == CommunityTabViewModel.FilterType.COUNTRY
                alpha = if (isSelected) 1f else 0.35f
                typeface = ResourcesCompat.getFont(
                    requireContext(),
                    if (isSelected) R.font.euro_stile_bold else R.font.euro_stile_regular
                )
            }

            btnLanguage.apply {
                isSelected = filter == CommunityTabViewModel.FilterType.LANGUAGE
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