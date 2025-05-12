package com.project.rekatrack.data.response

import com.google.gson.annotations.SerializedName

data class SendLocationResponse(

	@field:SerializedName("data")
	val data: List<LocationStatus>? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class LocationStatus(

	@field:SerializedName("track_id")
	val trackId: Int? = null,

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null,

	@field:SerializedName("status")
	val status: String? = null
)
