import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun addComment(postID: String, userID: String, text: String): UseCaseResult<CommunityPost> {
        return when (val result = userRepository.addComment(postID, userID, text)) {
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