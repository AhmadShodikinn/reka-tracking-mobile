package com.project.rekatrack.data.response

import com.google.gson.annotations.SerializedName

data class UpdateStateTrackingResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("results")
	val results: List<ResultsItemUpdateStatus>? = null
)

data class ResultsItemUpdateStatus(

	@field:SerializedName("travel_document_id")
	val travelDocumentId: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
