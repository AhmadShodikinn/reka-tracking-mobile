package com.project.rekatrack.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiConfig {
    companion object{

        private const val BASE_URL = "http://192.168.11.9:8000/api/"

//        fun getApiService(token: String): ApiService {
//            val logging = HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }
//
//            val client = OkHttpClient.Builder()
//                .addInterceptor(logging)
//                .addInterceptor(TokenInterceptor(token))
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .build()
//
//            val retrofit = Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
//                .build()
//
//            return retrofit.create(ApiService::class.java)
//        }

        fun getApiService(token: String): ApiService {
            Log.d("ApiConfig", "Mendapatkan ApiService dengan token: $token")

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient(token))
                .build()
                .create(ApiService::class.java)
        }

        private fun getOkHttpClient(token: String): OkHttpClient {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            Log.d("ApiConfig", "Mengonfigurasi OkHttpClient dengan token: $token")

            return OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(TokenInterceptor(token))
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }
}