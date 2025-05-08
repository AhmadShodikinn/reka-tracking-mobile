package com.project.rekatrack.data.response

import com.google.gson.annotations.SerializedName

data class UserLoginResponse(

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("expires_at")
	val expiresAt: String? = null,

	@field:SerializedName("data")
	val data: DataLogin? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("token_type")
	val tokenType: String? = null
)

data class Division(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class DataLogin(

	@field:SerializedName("role")
	val role: Role? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Role(

	@field:SerializedName("division")
	val division: Division? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
