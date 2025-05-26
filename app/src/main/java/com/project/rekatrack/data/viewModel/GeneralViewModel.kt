package com.project.rekatrack.data.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.response.CompleteTrackingActivityResponse
import com.project.rekatrack.data.response.DataItemCompleteTracking
import com.project.rekatrack.data.response.DataItemSendLocation
import com.project.rekatrack.data.response.ResultsItemUpdateStatus
import com.project.rekatrack.data.response.SearchSJNResponse
import com.project.rekatrack.data.response.SendLocationResponse
import com.project.rekatrack.data.response.TravelDocumentInfo
import com.project.rekatrack.data.response.UpdateStateTrackingResponse
import com.project.rekatrack.data.response.UserLogoutResponse
import kotlinx.coroutines.launch
import org.json.JSONObject

class GeneralViewModel(
    private val repository: Repository,
    private val context: Context
): ViewModel() {
    private val _searchSJNResponse = MutableLiveData<SearchSJNResponse?>()
    val searchSJNResponse: LiveData<SearchSJNResponse?> = _searchSJNResponse

    private val _travelDocumentInfoList = MutableLiveData<List<TravelDocumentInfo>>()
    val travelDocumentInfoList: LiveData<List<TravelDocumentInfo>> = _travelDocumentInfoList

    private val _sendLocationResult = MutableLiveData<List<DataItemSendLocation?>?>()
    val sendLocationResponse: LiveData<List<DataItemSendLocation?>?> = _sendLocationResult

//    private val _sendLocationResult = MutableLiveData<SendLocationResponse>()
//    val sendLocationResponse: LiveData<SendLocationResponse> = _sendLocationResult

    private val _updateStateTracking = MutableLiveData<List<ResultsItemUpdateStatus?>?>()
    val updateStateResponse: LiveData<List<ResultsItemUpdateStatus?>?> = _updateStateTracking

//    private val _updateStateTracking = MutableLiveData<UpdateStateTrackingResponse>()
//    val updateStateResponse: LiveData<UpdateStateTrackingResponse> = _updateStateTracking

    private val _completeTrackingActivity = MutableLiveData<List<DataItemCompleteTracking?>?>()
    val completeTrackingResponse: LiveData<List<DataItemCompleteTracking?>?> = _completeTrackingActivity

//    private val _completeTrackingActivity = MutableLiveData<CompleteTrackingActivityResponse>()
//    val completeTrackingResponse: LiveData<CompleteTrackingActivityResponse> = _completeTrackingActivity

    private val _logoutSession = MutableLiveData<UserLogoutResponse>()
    val logoutResponse: LiveData<UserLogoutResponse> = _logoutSession

    fun authLogout() {
        viewModelScope.launch {
            try {
                val response = repository.authLogout()

                if (response.isSuccessful) {
                    _logoutSession.value = response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = JSONObject(errorBody).getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Server Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getTravelDocument(id: String) {
        viewModelScope.launch {
            try {
                val response = repository.getTravelDocument(id)

                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    _searchSJNResponse.value = searchResponse

                   searchResponse?.data?.let { dataPengiriman ->
                        val travelDocumentInfo = TravelDocumentInfo(
                            id = dataPengiriman.id,
                            noTravelDocument = dataPengiriman.noTravelDocument,
                            sendTo = dataPengiriman.sendTo,
                            status = (dataPengiriman.status == "Terkirim")
                        )

                       val isSent = (dataPengiriman.status == "Terkirim")
                       if (isSent) {
                           return@launch
                       }

                        val currentList = _travelDocumentInfoList.value?.toMutableList() ?: mutableListOf()
                        if (currentList.none { it.noTravelDocument == travelDocumentInfo.noTravelDocument }) {
                            currentList.add(travelDocumentInfo)
                            _travelDocumentInfoList.value = currentList
                        } else {
                            Toast.makeText(context, "Data sudah ditambahkan sebelumnya", Toast.LENGTH_SHORT).show()
                        }

                        _travelDocumentInfoList.value = currentList
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = JSONObject(errorBody).getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Server Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun removeTravelDocument(noTravelDocument: String) {
        val currentList = _travelDocumentInfoList.value?.toMutableList() ?: return
        val updatedList = currentList.filterNot { it.noTravelDocument == noTravelDocument }
        _travelDocumentInfoList.value = updatedList
    }

    fun sendCurrentLocation(
        travelDocumentIds: List<Int>,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            try {
                val response = repository.sendCurrentLocation(
                    travelDocumentIds, latitude, longitude
                )

                if (response.isSuccessful) {
                    response.body().let { sendLocationResponse ->
                        _sendLocationResult.value = sendLocationResponse?.data
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = JSONObject(errorBody).getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Server Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateStateTracking(
        travelDocumentIds: List<Int>,
        latitude: Double,
        longitude: Double,
    ){
        viewModelScope.launch {
            try {
                val response = repository.updateStateTracking(
                    travelDocumentIds, latitude, longitude
                )

                if (response.isSuccessful) {
                    response.body().let { updateStateTrackingResponse ->
                        _updateStateTracking.value = updateStateTrackingResponse?.results
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = JSONObject(errorBody).getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Server Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun completeTrackingActivity(
        travelDocumentIds: List<Int>,
        latitude: Double,
        longitude: Double,
    ) {
        viewModelScope.launch {
            try {
                val response = repository.completeTrackingActivity(
                    travelDocumentIds, latitude, longitude
                )
                if (response.isSuccessful) {
                    response.body().let { completeTrackingResponse ->
                        _completeTrackingActivity.value = completeTrackingResponse?.data
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = JSONObject(errorBody).getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Server Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }




}
