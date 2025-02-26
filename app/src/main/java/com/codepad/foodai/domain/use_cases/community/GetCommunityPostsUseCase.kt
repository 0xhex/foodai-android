package com.codepad.foodai.domain.use_cases.community

import com.codepad.foodai.domain.models.community.CommunityPost
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class GetCommunityPostsUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun getCommunityPosts(userID: String): UseCaseResult<List<CommunityPost>> {
        return when (val result = userRepository.getCommunityPosts(userID)) {
            is RepositoryResult.Success -> {
                UseCaseResult.Success(
                    message = result.message,
                    code = result.code,
                    data = result.data
                )
            }

            is RepositoryResult.Error -> {
                UseCaseResult.Error(
                    message = result.message,
                    code = result.code,
                    exception = result.exception
                )
            }
        }
    }
} 