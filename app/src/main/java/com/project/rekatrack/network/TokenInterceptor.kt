package com.project.rekatrack.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.project.rekatrack.support.SessionHandler
import com.project.rekatrack.support.TokenHandler
import com.project.rekatrack.ui.LoginActivity
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor(
    private val tokenHandler: TokenHandler
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenHandler.getToken()
        val requestBuilder = chain.request().newBuilder()
            .header("Accept", "application/json")

        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            Log.w("TokenInterceptor", "401 Unauthorized – menghapus token dari penyimpanan")
            tokenHandler.removeToken()
            SessionHandler.triggerSessionExpired()
        }

        return response
    }
}