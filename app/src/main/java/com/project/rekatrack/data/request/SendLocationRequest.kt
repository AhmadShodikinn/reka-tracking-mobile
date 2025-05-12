package com.project.rekatrack.data.request

data class SendLocationRequest(
    val travel_document_id: List<Int>,
    val latitude: Double,
    val longitude: Double,
    val driver_id: Int
)