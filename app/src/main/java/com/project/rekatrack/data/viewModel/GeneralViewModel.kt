package com.project.rekatrack.data.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.response.LocationStatus
import com.project.rekatrack.data.response.ResultsItem
import com.project.rekatrack.data.response.SearchSJNResponse
import com.project.rekatrack.data.response.TravelDocumentInfo
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

    private val _locationStatusList = MutableLiveData<List<LocationStatus>?>()
    val locationStatusList: LiveData<List<LocationStatus>?> = _locationStatusList

    private val _updateStateTracking = MutableLiveData<List<ResultsItem>?>()
    val updateStateTracking: LiveData<List<ResultsItem>?> = _updateStateTracking

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
                            sendTo = dataPengiriman.sendTo
                        )

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
        longitude: Double,
        driverId: Int
    ) {
        viewModelScope.launch {
            try {
                val response = repository.sendCurrentLocation(
                    travelDocumentIds, latitude, longitude, driverId
                )

                if (response.isSuccessful) {
                    response.body()?.let { sendResponse ->
                        _locationStatusList.value = sendResponse.data
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
                    response.body().let { updateResponse ->
                        _updateStateTracking.value = updateResponse?.results
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
