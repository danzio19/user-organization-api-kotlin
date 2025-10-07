package com.digitopia.casestudy.security

import com.digitopia.casestudy.exception.AccessDeniedException
import com.digitopia.casestudy.user.User
import com.digitopia.casestudy.user.UserRepository
import com.digitopia.casestudy.user.UserRole
import com.digitopia.casestudy.user.UserStatus
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthorizationService(private val userRepository: UserRepository) {

    /**
     * Verifies if a user has one of the required roles.
     * Throws an exception if the user is not found, not active, or doesn't have the required role.
     */
    fun checkUserRole(userId: UUID?, requiredRoles: Set<UserRole>): User {
        if (userId == null) {
            throw AccessDeniedException("User ID must be provided for this operation.")
        }
        val user = userRepository.findByIdOrNull(userId)
            ?: throw AccessDeniedException("User with ID $userId not found.")

        if (user.status != UserStatus.ACTIVE) {
            throw AccessDeniedException("User ${user.email} is not active and cannot perform this action.")
        }

        if (user.role !in requiredRoles) {
            throw AccessDeniedException("User ${user.email} with role ${user.role} is not authorized. Required roles: $requiredRoles.")
        }
        return user
    }
}