package com.codepad.foodai.helpers

import android.content.Context
import android.content.SharedPreferences
import com.codepad.foodai.domain.models.user.User
import com.google.gson.Gson

object UserSession {
    var isPremiumUser: Boolean = false
    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_USER = "key_user"
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    var user: User? = null
        private set

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadUser()
    }

    fun updateSession(response: User) {
        user = response
        saveUser()
    }

    fun clearSession() {
        user = null
        sharedPreferences.edit().clear().apply()
    }

    private fun saveUser() {
        sharedPreferences.edit().putString(KEY_USER, gson.toJson(user)).apply()
    }

    private fun loadUser() {
        user = gson.fromJson(sharedPreferences.getString(KEY_USER, null), User::class.java)
    }
}