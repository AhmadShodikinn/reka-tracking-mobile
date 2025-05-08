package com.project.rekatrack.data.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rekatrack.data.repository.Repository
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

    fun getTravelDocument(id: String) {
        viewModelScope.launch {
            try {
                val response = repository.getTravelDocument(id)

                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    _searchSJNResponse.value = searchResponse

                   searchResponse?.data?.let { dataPengiriman ->
                        val travelDocumentInfo = TravelDocumentInfo(
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

}
