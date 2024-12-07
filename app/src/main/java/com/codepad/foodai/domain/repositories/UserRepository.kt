package com.codepad.foodai.domain.repositories

import com.codepad.foodai.domain.api.RestApi
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val restApi: RestApi,
) {

}