package com.project.rekatrack.data.response

import com.google.gson.annotations.SerializedName

data class CompleteTrackingActivityResponse(

	@field:SerializedName("data")
	val data: List<DataItemCompleteTracking?>? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class DataItemCompleteTracking(

	@field:SerializedName("travel_document_id")
	val travelDocumentId: Int? = null,

	@field:SerializedName("tracking_status")
	val trackingStatus: String? = null,

	@field:SerializedName("travel_document_status")
	val travelDocumentStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null
)
