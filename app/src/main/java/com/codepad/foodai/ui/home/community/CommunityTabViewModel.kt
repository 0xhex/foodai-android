package com.codepad.foodai.ui.home.community

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.models.community.CommunityPost
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.community.GetCommunityPostsUseCase
import com.codepad.foodai.helpers.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityTabViewModel @Inject constructor(
    private val getCommunityPostsUseCase: GetCommunityPostsUseCase,
) : ViewModel() {

    enum class FilterType {
        WORLD, COUNTRY, LANGUAGE
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _posts = MutableLiveData<List<CommunityPost>>()
    val posts: LiveData<List<CommunityPost>> = _posts

    private val _selectedFilter = MutableLiveData(FilterType.WORLD)
    val selectedFilter: LiveData<FilterType> = _selectedFilter

    val filteredPosts = MediatorLiveData<List<CommunityPost>>().apply {
        addSource(_posts) { updateFilteredPosts() }
        addSource(_selectedFilter) { updateFilteredPosts() }
    }

    private fun updateFilteredPosts() {
        val currentPosts = _posts.value ?: return
        val filter = _selectedFilter.value ?: return
        val currentUser = UserSession.user ?: return

        val filtered = when (filter) {
            FilterType.WORLD -> currentPosts
            FilterType.COUNTRY -> currentPosts.filter { it.user.countryCode == currentUser.countryCode }
            FilterType.LANGUAGE -> currentPosts.filter { it.user.deviceLang == currentUser.deviceLang }
        }
        filteredPosts.value = filtered
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = getCommunityPostsUseCase.getCommunityPosts(UserSession.user?.id.orEmpty())) {
                is UseCaseResult.Success -> {
                    Log.d("CommunityTab", "Fetched ${result.data.size} posts")
                    _posts.value = result.data
                    Log.d("CommunityTab", "First post: ${result.data.firstOrNull()?.image?.mealName}")
                }
                is UseCaseResult.Error -> {
                    Log.e("CommunityTab", "Error fetching posts: ${result.message}")
                    _errorMessage.value = result.message
                    _posts.value = emptyList()
                }
            }
            _isLoading.value = false
        }
    }

    fun setFilter(filter: FilterType) {
        _selectedFilter.value = filter
    }

    fun getCountryFlag(countryCode: String?): String {
        if (countryCode == null || countryCode.length != 2) return ""

        val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6

        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
} 