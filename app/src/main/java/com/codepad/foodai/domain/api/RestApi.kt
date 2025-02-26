package com.codepad.foodai.domain.api

import AddCommentRequest
import CommunityPost
import CommunityResponseData
import CreateCommunityPostRequest
import DeleteCommentRequest
import LikePostRequest
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
import com.codepad.foodai.domain.models.recommendation.Recommendation
import com.codepad.foodai.domain.models.recommendation.RequestRecommendationRequest
import com.codepad.foodai.domain.models.recommendation.RequestRecommendationResponse
import com.codepad.foodai.domain.models.user.RegisterRequest
import com.codepad.foodai.domain.models.user.StreakResponseData
import com.codepad.foodai.domain.models.user.UpdateUserFieldRequest
import com.codepad.foodai.domain.models.user.UpdateUserFieldRequestArray
import com.codepad.foodai.domain.models.user.UpdateUserFieldResponseData
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.models.user.WeightLogData
import com.codepad.foodai.domain.use_cases.user.DailySummaryResponseData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RestApi {

    @GET("users/{userID}/data")
    suspend fun getUserData(@Path("userID") userID: String): APIResponse<User>


    @POST("users/register")
    suspend fun registerUser(@Body request: RegisterRequest): APIResponse<User>

    @PATCH("users/{userID}")
    suspend fun updateUserFields(
        @Path("userID") userID: String,
        @Body request: UpdateUserFieldRequest,
    ): APIResponse<UpdateUserFieldResponseData>

    @PATCH("users/{userID}")
    suspend fun updateUserFieldsArray(
        @Path("userID") userID: String,
        @Body request: UpdateUserFieldRequestArray,
    ): APIResponse<UpdateUserFieldResponseData>

    @GET("users/{userID}/nutrition")
    suspend fun getUserNutrition(@Path("userID") userID: String): APIResponse<NutritionResponseData>

    @Multipart
    @POST("users/upload")
    suspend fun uploadImage(
        @Part("userID") userID: RequestBody,
        @Part file: MultipartBody.Part,
    ): APIResponse<ImageUploadResponse>

    @GET("users/{imageID}/getImageData")
    suspend fun fetchImage(@Path("imageID") imageID: String): APIResponse<ImageData>

    @GET("users/{userID}/streak")
    suspend fun getUserStreak(@Path("userID") userID: String): APIResponse<StreakResponseData>

    @GET("users/{userID}/daily-summary")
    suspend fun getUserDailySummary(
        @Path("userID") userID: String,
        @Query("date") date: String,
    ): APIResponse<DailySummaryResponseData>

    @POST("recipes/generate")
    suspend fun generateRecipe(
        @Body request: GenerateRecipeRequest,
    ): APIResponse<GenerateRecipeResponseData>

    @GET("recipes/{recipeID}")
    suspend fun getRecipeStatus(
        @Path("recipeID") recipeID: String,
    ): APIResponse<Recipe>

    @DELETE("users/{imageId}/deleteImage")
    suspend fun deleteImage(@Path("imageId") imageId: String): APIResponse<Unit>

    @POST("users/images/{imageId}/fix")
    suspend fun fixImageResults(
        @Path("imageId") imageId: String,
        @Body request: FixImageResultsRequest,
    ): APIResponse<ImageData?>

    @POST("users/exercises/custom")
    suspend fun logExerciseCustom(
        @Body request: LogExerciseCustomRequest
    ): APIResponse<ExerciseData>

    @POST("users/exercises")
    suspend fun submitExerciseDescription(
        @Body request: SubmitExerciseDescriptionRequest
    ): APIResponse<ExerciseData>

    @DELETE("users/exercises/{exerciseId}")
    suspend fun deleteExercise(
        @Path("exerciseId") exerciseId: String
    ): APIResponse<Unit>

    @PUT("users/exercises/description")
    suspend fun updateExerciseDescription(
        @Body request: UpdateExerciseDescriptionRequest
    ): APIResponse<ExerciseData>

    @PUT("users/exercises/custom")
    suspend fun updateCustomExercise(
        @Body request: UpdateCustomExerciseRequest
    ): APIResponse<ExerciseData>

    @GET("users/{userID}/weight-logs")
    suspend fun getUserWeightLogs(
        @Path("userID") userID: String
    ): APIResponse<List<WeightLogData>>

    @POST("recommendations/generate")
    suspend fun requestRecommendations(
        @Body request: RequestRecommendationRequest
    ): APIResponse<RequestRecommendationResponse>

    @GET("recommendations/{recommendationID}")
    suspend fun getRecommendation(
        @Path("recommendationID") recommendationID: String
    ): APIResponse<Recommendation>

    @DELETE("users/{userID}/deleteAccount")
    suspend fun deleteAccount(@Path("userID") userID: String): APIResponse<Unit>

    @GET("community")
    suspend fun getCommunityPosts(
        @Query("userID") userID: String
    ): APIResponse<CommunityResponseData>

    @POST("community")
    suspend fun createCommunityPost(
        @Body request: CreateCommunityPostRequest
    ): APIResponse<CommunityPost>

    @POST("community/{postID}/like")
    suspend fun likePost(
        @Path("postID") postID: String,
        @Body request: LikePostRequest
    ): APIResponse<CommunityPost>

    @DELETE("community/{postID}/like")
    suspend fun unlikePost(
        @Path("postID") postID: String,
        @Body request: LikePostRequest
    ): APIResponse<CommunityPost>

    @POST("community/{postID}/comments")
    suspend fun addComment(
        @Path("postID") postID: String,
        @Body request: AddCommentRequest
    ): APIResponse<CommunityPost>

    @DELETE("community/{postID}/comments")
    suspend fun deleteComment(
        @Path("postID") postID: String,
        @Body request: DeleteCommentRequest
    ): APIResponse<CommunityPost>

}