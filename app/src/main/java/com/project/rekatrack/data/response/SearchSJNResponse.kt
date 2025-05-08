package com.project.rekatrack.data.response

import com.google.gson.annotations.SerializedName

data class SearchSJNResponse(

	@field:SerializedName("data")
	val data: DataPengiriman? = null
)

data class ItemsItem(

	@field:SerializedName("item_code")
	val itemCode: String? = null,

	@field:SerializedName("unit")
	val unit: String? = null,

	@field:SerializedName("qty_send")
	val qtySend: Int? = null,

	@field:SerializedName("travel_document_id")
	val travelDocumentId: Int? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("qty_po")
	val qtyPo: Int? = null,

	@field:SerializedName("total_send")
	val totalSend: Int? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("item_name")
	val itemName: String? = null,

	@field:SerializedName("information")
	val information: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class DataPengiriman(

	@field:SerializedName("send_to")
	val sendTo: String? = null,

	@field:SerializedName("po_number")
	val poNumber: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("no_travel_document")
	val noTravelDocument: String? = null,

	@field:SerializedName("date_no_travel_document")
	val dateNoTravelDocument: String? = null,

	@field:SerializedName("project")
	val project: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("reference_number")
	val referenceNumber: String? = null,

	@field:SerializedName("deleted_at")
	val deletedAt: Any? = null,

	@field:SerializedName("items")
	val items: List<ItemsItem?>? = null,

	@field:SerializedName("status")
	val status: String? = null
)
