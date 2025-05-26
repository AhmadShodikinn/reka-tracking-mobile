package com.project.rekatrack.support

import android.content.Context
import android.content.SharedPreferences

class TokenHandler(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "userPref", Context.MODE_PRIVATE
    )

    companion object {
        private const val TOKEN_KEY = "token_key"
    }

    fun saveToken(token: String){
        sharedPreferences
            .edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences
            .getString(TOKEN_KEY, null)
    }

    fun removeToken() {
        sharedPreferences.edit()
            .remove(TOKEN_KEY)
            .apply()
    }

    fun setUserInfo(name: String, role: String) {
        sharedPreferences.edit()
            .putString("user_name", name)
            .putString("user_role", role)
            .apply()
    }

    fun getUserName(): String? = sharedPreferences.getString("user_name", "")
    fun getUserRole(): String? = sharedPreferences.getString("user_role", "")
}
