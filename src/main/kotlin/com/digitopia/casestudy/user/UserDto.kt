package com.digitopia.casestudy.user

import java.util.UUID

data class CreateUserRequest(
    val email: String,
    val fullName: String,
    val role: UserRole = UserRole.USER
)

data class UpdateUserRequest(
    val fullName: String
)