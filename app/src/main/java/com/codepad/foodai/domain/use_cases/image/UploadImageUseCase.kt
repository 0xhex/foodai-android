package com.codepad.foodai.domain.use_cases.image

import com.codepad.foodai.domain.models.image.ImageUploadResponse
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import java.io.File
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun uploadImage(
        userID: String,
        imageFile: File,
        fileName: String,
        mimeType: String,
    ): UseCaseResult<ImageUploadResponse> {
        return when (val result = userRepository.uploadImage(userID, imageFile, fileName, mimeType)) {
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