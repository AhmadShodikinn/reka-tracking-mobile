package com.project.rekatrack.data.request

data class ForgotPasswordRequest(
    val email: String,
    val password: String,
    val password_confirmation: String
)
