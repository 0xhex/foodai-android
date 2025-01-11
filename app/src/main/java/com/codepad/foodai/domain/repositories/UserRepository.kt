package com.codepad.foodai.domain.repositories

import com.codepad.foodai.domain.api.APIError
import com.codepad.foodai.domain.api.RestApi
import com.codepad.foodai.domain.models.image.FixImageResultsRequest
import com.codepad.foodai.domain.models.image.ImageData
import com.codepad.foodai.domain.models.image.ImageUploadResponse
import com.codepad.foodai.domain.models.nutrition.NutritionResponseData
import com.codepad.foodai.domain.models.recipe.GenerateRecipeRequest
import com.codepad.foodai.domain.models.recipe.GenerateRecipeResponseData
import com.codepad.foodai.domain.models.recipe.Recipe
import com.codepad.foodai.domain.models.user.RegisterRequest
import com.codepad.foodai.domain.models.user.StreakResponseData
import com.codepad.foodai.domain.models.user.UpdateUserFieldRequest
import com.codepad.foodai.domain.models.user.UpdateUserFieldRequestArray
import com.codepad.foodai.domain.models.user.UpdateUserFieldResponseData
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.use_cases.user.DailySummaryResponseData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val restApi: RestApi,
) {
    suspend fun getUserData(userId: String): RepositoryResult<User> {
        return try {
            val response = restApi.getUserData(userId)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun registerUser(request: RegisterRequest): RepositoryResult<User> {
        return try {
            val response = restApi.registerUser(request)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }


    suspend fun updateUserFields(
        userID: String,
        request: UpdateUserFieldRequest,
    ): RepositoryResult<UpdateUserFieldResponseData> {
        return try {
            val response = restApi.updateUserFields(userID, request)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun updateUserFieldsArray(
        userID: String,
        request: UpdateUserFieldRequestArray,
    ): RepositoryResult<UpdateUserFieldResponseData> {
        return try {
            val response = restApi.updateUserFieldsArray(userID, request)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun getUserNutrition(userID: String): RepositoryResult<NutritionResponseData> {
        return try {
            val response = restApi.getUserNutrition(userID)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun uploadImage(
        userID: String,
        imageFile: File,
        fileName: String,
        mimeType: String,
    ): RepositoryResult<ImageUploadResponse> {
        return try {
            val userIDRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userID)
            val imageRequestBody = RequestBody.create(mimeType.toMediaTypeOrNull(), imageFile)
            val imagePart = MultipartBody.Part.createFormData("file", fileName, imageRequestBody)

            val response = restApi.uploadImage(userIDRequestBody, imagePart)

            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }


    suspend fun fetchImage(imageID: String): RepositoryResult<ImageData> {
        return try {
            val response = restApi.fetchImage(imageID)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun getUserStreak(userID: String): RepositoryResult<StreakResponseData> {
        return try {
            val response = restApi.getUserStreak(userID)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun getUserDailySummary(
        userID: String,
        date: String,
    ): RepositoryResult<DailySummaryResponseData> {
        return try {
            val response = restApi.getUserDailySummary(userID, date)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun generateRecipe(
        userID: String,
        mealType: String,
    ): RepositoryResult<GenerateRecipeResponseData> {
        return try {
            val request = GenerateRecipeRequest(userID = userID, mealType = mealType)
            val response = restApi.generateRecipe(request)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun getRecipeStatus(recipeID: String): RepositoryResult<Recipe> {
        return try {
            val response = restApi.getRecipeStatus(recipeID)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun deleteImage(imageId: String): RepositoryResult<Unit> {
        return try {
            val response = restApi.deleteImage(imageId)
            if (response.success) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = Unit
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

    suspend fun fixResult(
        imageId: String,
        imageResultsRequest: FixImageResultsRequest,
    ): RepositoryResult<ImageData?> {
        return try {
            val response = restApi.fixImageResults(imageId, imageResultsRequest)
            if (response.success) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }
}