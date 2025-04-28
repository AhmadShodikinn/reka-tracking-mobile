package com.project.rekatrack.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor (private val token: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
//        Log.d("TokenInterceptor", "Token: $token")
        val newRequest: Request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }
}