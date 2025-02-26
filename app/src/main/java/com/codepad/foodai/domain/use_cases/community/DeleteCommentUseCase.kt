import com.codepad.foodai.domain.models.community.CommunityPost
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun deleteComment(
        postID: String,
        commentID: String,
        userID: String,
    ): UseCaseResult<CommunityPost> {
        return when (val result = userRepository.deleteComment(postID, commentID, userID)) {
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