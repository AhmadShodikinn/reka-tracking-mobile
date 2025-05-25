package com.project.rekatrack.data.response

import com.google.gson.annotations.SerializedName

data class DataItemSendLocation(

	@field:SerializedName("travel_document_id")
	val travelDocumentId: Int? = null,

	@field:SerializedName("track_id")
	val trackId: Int? = null,

	@field:SerializedName("latitude")
	val latitude: Any? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("longitude")
	val longitude: Any? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class SendLocationResponse(

	@field:SerializedName("data")
	val data: List<DataItemSendLocation?>? = null,

	@field:SerializedName("message")
	val message: String? = null
)
