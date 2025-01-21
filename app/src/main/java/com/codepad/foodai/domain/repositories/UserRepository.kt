package com.codepad.foodai.domain.repositories

import com.codepad.foodai.domain.api.APIError
import com.codepad.foodai.domain.api.RestApi
import com.codepad.foodai.domain.models.APIResponse
import com.codepad.foodai.domain.models.exercise.ExerciseData
import com.codepad.foodai.domain.models.exercise.LogExerciseCustomRequest
import com.codepad.foodai.domain.models.exercise.SubmitExerciseDescriptionRequest
import com.codepad.foodai.domain.models.exercise.UpdateCustomExerciseRequest
import com.codepad.foodai.domain.models.exercise.UpdateExerciseDescriptionRequest
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
import com.codepad.foodai.domain.models.user.WeightLogData
import com.codepad.foodai.domain.models.recommendation.RequestRecommendationRequest
import com.codepad.foodai.domain.models.recommendation.RequestRecommendationResponse
import com.codepad.foodai.domain.models.recommendation.Recommendation
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val restApi: RestApi,
) {
    private data class ErrorResponse(
        val success: Boolean,
        val errorCode: String?,
        val message: String?,
        val data: Any?
    )

    private fun <T> handleException(e: Exception): RepositoryResult<T> {
        return when {
            e is retrofit2.HttpException -> {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = com.google.gson.Gson().fromJson(errorBody, ErrorResponse::class.java)
                    RepositoryResult.Error(
                        message = errorResponse.message ?: "Server error",
                        code = e.code(),
                        exception = APIError.ServerError(
                            errorMessage = errorResponse.message ?: "Server error",
                            code = errorResponse.errorCode
                        )
                    )
                } catch (e2: Exception) {
                    RepositoryResult.Error(
                        message = e.message ?: "Network error",
                        code = -1,
                        exception = APIError.NetworkError(e)
                    )
                }
            }
            else -> RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }

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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<User>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<User>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<UpdateUserFieldResponseData>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<UpdateUserFieldResponseData>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<NutritionResponseData>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<ImageUploadResponse>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<ImageData>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<StreakResponseData>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<DailySummaryResponseData>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<GenerateRecipeResponseData>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<Recipe>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<Unit>(e)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<ImageData?>(e)
        }
    }

    suspend fun logExerciseCustom(
        userID: String,
        type: String,
        intensity: String,
        duration: Int,
        localDate: String,
    ): RepositoryResult<ExerciseData> {
        return try {
            val request = LogExerciseCustomRequest(userID, type, intensity, duration, localDate)
            val response = restApi.logExerciseCustom(request)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<ExerciseData>(e)
        }
    }

    suspend fun submitExerciseDescription(
        userID: String,
        exerciseDescription: String,
        localDate: String,
    ): RepositoryResult<ExerciseData> {
        return try {
            val request = SubmitExerciseDescriptionRequest(userID, exerciseDescription, localDate)
            val response = restApi.submitExerciseDescription(request)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<ExerciseData>(e)
        }
    }

    suspend fun deleteExercise(exerciseId: String): RepositoryResult<Unit> {
        return try {
            val response = restApi.deleteExercise(exerciseId)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<Unit>(e)
        }
    }

    suspend fun updateExerciseDescription(
        exerciseID: String,
        exerciseDescription: String,
    ): RepositoryResult<ExerciseData> {
        return try {
            val request = UpdateExerciseDescriptionRequest(exerciseID, exerciseDescription)
            val response = restApi.updateExerciseDescription(request)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<ExerciseData>(e)
        }
    }

    suspend fun updateCustomExercise(
        exerciseID: String,
        type: String,
        intensity: String,
        duration: Int,
    ): RepositoryResult<ExerciseData> {
        return try {
            val request = UpdateCustomExerciseRequest(exerciseID, type, intensity, duration)
            val response = restApi.updateCustomExercise(request)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<ExerciseData>(e)
        }
    }

    suspend fun getUserWeightLogs(userID: String): RepositoryResult<List<WeightLogData>> {
        return try {
            val response = restApi.getUserWeightLogs(userID)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<List<WeightLogData>>(e)
        }
    }

    suspend fun requestRecommendations(imageID: String): RepositoryResult<RequestRecommendationResponse> {
        return try {
            val request = RequestRecommendationRequest(imageID = imageID)
            val response = restApi.requestRecommendations(request)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<RequestRecommendationResponse>(e)
        }
    }

    suspend fun getRecommendation(recommendationID: String): RepositoryResult<Recommendation> {
        return try {
            val response = restApi.getRecommendation(recommendationID)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<Recommendation>(e)
        }
    }

    suspend fun deleteAccount(userID: String): RepositoryResult<Unit> {
        return try {
            val response = restApi.deleteAccount(userID)
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
                        errorMessage = response.message ?: "Unknown error",
                        code = response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            handleException<Unit>(e)
        }
    }
}