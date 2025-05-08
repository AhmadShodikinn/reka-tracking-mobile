package com.project.rekatrack.data.repository

import com.project.rekatrack.data.request.UserLoginRequest
import com.project.rekatrack.data.response.SearchSJNResponse
import com.project.rekatrack.data.response.UserLoginResponse
import com.project.rekatrack.network.ApiService
import retrofit2.Response

class Repository(private val apiService: ApiService) {
    suspend fun authLogin(email: String, password: String): Response<UserLoginResponse> {
        val bodyRequest = UserLoginRequest(email, password)
        return apiService.authUsers(bodyRequest)
    }
    suspend fun getTravelDocument(id: String): Response<SearchSJNResponse> {
        return apiService.getTravelDocument(id)
    }
}