package com.project.rekatrack.data.request

data class UpdateStateTrackingRequest(
    val travel_document_id: List<Int>,
    val latitude: Double,
    val longitude: Double,
)
