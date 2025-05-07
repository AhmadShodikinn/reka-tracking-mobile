package com.project.rekatrack.support

import android.content.Context
import android.content.SharedPreferences

class TokenHandler(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "userPref", Context.MODE_PRIVATE
    )

    companion object {
        private val TOKEN_KEY = ""
    }

    fun saveToken(token: String){
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }
}