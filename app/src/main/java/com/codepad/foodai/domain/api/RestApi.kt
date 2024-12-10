package com.codepad.foodai.domain.api

import com.codepad.foodai.domain.models.APIResponse
import com.codepad.foodai.domain.models.user.RegisterRequest
import com.codepad.foodai.domain.models.user.UpdateUserFieldRequest
import com.codepad.foodai.domain.models.user.UpdateUserFieldResponseData
import com.codepad.foodai.domain.models.user.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

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
}