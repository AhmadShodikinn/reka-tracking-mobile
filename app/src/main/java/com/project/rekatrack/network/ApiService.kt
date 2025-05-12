package com.project.rekatrack.network

import com.project.rekatrack.data.request.SendLocationRequest
import com.project.rekatrack.data.request.UpdateStateTrackingRequest
import com.project.rekatrack.data.request.UserLoginRequest
import com.project.rekatrack.data.response.SearchSJNResponse
import com.project.rekatrack.data.response.SendLocationResponse
import com.project.rekatrack.data.response.UpdateStateTrackingResponse
import com.project.rekatrack.data.response.UserLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("login")
    suspend fun authUsers(@Body userLoginRequest: UserLoginRequest): Response<UserLoginResponse>

    @GET("travel-document/{id}")
    suspend fun getTravelDocument(@Path("id") id: String): Response<SearchSJNResponse>

    @POST("send-location")
    suspend fun sendCurrentLocation(@Body sendLocationRequest: SendLocationRequest): Response<SendLocationResponse>

    @POST("update-status")
    suspend fun updateStateTracking(@Body updateStateTrackingRequest: UpdateStateTrackingRequest): Response<UpdateStateTrackingResponse>
}