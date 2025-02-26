package com.codepad.foodai.ui.home.community.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.api.APIError
import com.codepad.foodai.domain.models.ErrorCode
import com.codepad.foodai.domain.models.community.CommunityPost
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.community.AddCommentUseCase
import com.codepad.foodai.domain.use_cases.community.DeleteCommentUseCase
import com.codepad.foodai.domain.use_cases.community.LikePostUseCase
import com.codepad.foodai.domain.use_cases.community.UnlikePostUseCase
import com.codepad.foodai.helpers.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityPostDetailViewModel @Inject constructor(
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _post = MutableLiveData<CommunityPost>()
    val post: LiveData<CommunityPost> = _post

    private val _isLiked = MutableLiveData<Boolean>()
    val isLiked: LiveData<Boolean> = _isLiked

    private val _newCommentText = MutableLiveData<String>()
    val newCommentText: LiveData<String> = _newCommentText

    fun setPost(post: CommunityPost) {
        _post.value = post
        initializeLikeState()
    }

    private fun initializeLikeState() {
        val currentUserId = UserSession.user?.id
        val isLiked = post.value?.likes?.any { it.id == currentUserId } ?: false
        _isLiked.value = isLiked
    }

    fun toggleLike() {
        val currentUserId = UserSession.user?.id ?: return
        val currentPost = post.value ?: return

        viewModelScope.launch {
            _isLoading.value = true

            val result = if (_isLiked.value == true) {
                unlikePostUseCase.unlikePost(
                    postID = currentPost.id.orEmpty(),
                    userID = currentUserId
                )
            } else {
                likePostUseCase.likePost(
                    postID = currentPost.id.orEmpty(),
                    userID = currentUserId
                )
            }

            when (result) {
                is UseCaseResult.Success -> {
                    _post.postValue(result.data)
                    _isLiked.postValue(result.data.likes?.any { it.id == currentUserId } ?: false)
                }
                is UseCaseResult.Error -> {
                    _errorMessage.postValue(getCustomErrorMessage(result.exception))
                }
            }

            _isLoading.value = false
        }
    }

    fun addComment(text: String) {
        val currentUserId = UserSession.user?.id ?: return
        val currentPost = post.value ?: return

        if (text.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            val result = addCommentUseCase.addComment(
                postID = currentPost.id.orEmpty(),
                userID = currentUserId,
                text = text
            )

            when (result) {
                is UseCaseResult.Success -> {
                    _post.postValue(result.data)
                    _newCommentText.postValue("")
                }
                is UseCaseResult.Error -> {
                    _errorMessage.postValue(getCustomErrorMessage(result.exception))
                }
            }

            _isLoading.value = false
        }
    }

    fun deleteComment(commentId: String) {
        val currentUserId = UserSession.user?.id ?: return
        val currentPost = post.value ?: return

        viewModelScope.launch {
            _isLoading.value = true

            val result = deleteCommentUseCase.deleteComment(
                postID = currentPost.id.orEmpty(),
                commentID = commentId,
                userID = currentUserId
            )

            when (result) {
                is UseCaseResult.Success -> {
                    _post.postValue(result.data)
                }

                is UseCaseResult.Error -> {
                    _errorMessage.postValue(getCustomErrorMessage(result.exception))
                }
            }

            _isLoading.value = false
        }
    }

    private fun getCustomErrorMessage(error: APIError?): String {
        val message = error?.message
        return when (message) {
            ErrorCode.COMMUNITY_COMMENT_NOT_FOUND.code -> "Comment not found"
            ErrorCode.COMMUNITY_UNAUTHORIZED.code -> "You are not authorized to perform this action"
            ErrorCode.COMMUNITY_POST_NOT_FOUND.code -> "Post not found"
            ErrorCode.COMMUNITY_USER_NOT_FOUND.code -> "User not found"
            ErrorCode.COMMUNITY_INVALID_OPERATION.code -> "Invalid operation"
            ErrorCode.COMMUNITY_INTERNAL_SERVER_ERROR.code -> "An internal server error occurred"
            else -> "An unexpected error occurred"
        }
    }
} 