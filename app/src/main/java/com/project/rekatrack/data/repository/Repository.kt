package com.project.rekatrack.data.repository

import com.project.rekatrack.data.request.SendLocationRequest
import com.project.rekatrack.data.request.UpdateStateTrackingRequest
import com.project.rekatrack.data.request.UserLoginRequest
import com.project.rekatrack.data.response.SearchSJNResponse
import com.project.rekatrack.data.response.SendLocationResponse
import com.project.rekatrack.data.response.UpdateStateTrackingResponse
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

    suspend fun sendCurrentLocation(
        travelDocumentIds: List<Int>,
        latitude: Double,
        longitude: Double,
        driverId: Int
    ): Response<SendLocationResponse> {
        val request = SendLocationRequest(
            travel_document_id = travelDocumentIds,
            latitude = latitude,
            longitude = longitude,
            driver_id = driverId
        )
        return apiService.sendCurrentLocation(request)
    }

    suspend fun updateStateTracking(
        travelDocumentIds: List<Int>
    ): Response<UpdateStateTrackingResponse> {
        val request = UpdateStateTrackingRequest(
            travel_document_id = travelDocumentIds
        )
        return apiService.updateStateTracking(request)
    }
}