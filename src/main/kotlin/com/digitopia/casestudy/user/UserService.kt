package com.digitopia.casestudy.user

import com.digitopia.casestudy.organization.Organization
import com.digitopia.casestudy.security.AuthorizationService
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.nio.file.AccessDeniedException
import org.springframework.data.domain.PageImpl


@Service
class UserService(private val userRepository: UserRepository, private val authorizationService: AuthorizationService) {

    val SYSTEM_USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")

    fun createUser(request: CreateUserRequest, creatorId: UUID?): User {
        // First User in the System: Bootstrap Admin, ONLY FOR DEVELOPMENT PURPOSES
        if (creatorId == null) {
            // Only allow this if the user table is completely empty.
            if (userRepository.count() > 0) {
                throw AccessDeniedException("A creator user ID is required to create new users when the system is not empty.")
            }

            if (userRepository.findByEmail(request.email).isPresent) {
                throw IllegalStateException("User with email ${request.email} already exists.")
            }

            // Create the first user as an ACTIVE ADMIN
            val firstAdmin = User(
                email = request.email,
                fullName = request.fullName,
                normalizedName = request.fullName.lowercase().replace(Regex("[^a-z0-9]"), ""),
                status = UserStatus.ACTIVE,
                role = UserRole.ADMIN,
                createdBy = SYSTEM_USER_ID,
                updatedBy = SYSTEM_USER_ID
            )
            return userRepository.save(firstAdmin) // Save and return early
        }

        val creator = authorizationService.checkUserRole(creatorId, setOf(UserRole.ADMIN, UserRole.MANAGER))

        // A MANAGER can only create new USERs.
        if (creator.role == UserRole.MANAGER && request.role != UserRole.USER) {
            throw AccessDeniedException("Managers are only allowed to create users with the USER role.")
        }

        if (userRepository.findByEmail(request.email).isPresent) {
            throw IllegalStateException("User with email ${request.email} already exists.")
        }

        val newUserStatus = if (creator.role == UserRole.ADMIN) UserStatus.ACTIVE else UserStatus.PENDING

        val newUser = User(
            email = request.email,
            fullName = request.fullName,
            normalizedName = request.fullName.lowercase().replace(Regex("[^a-z0-9]"), ""),
            status = newUserStatus,
            role = request.role,
            createdBy = creator.id,
            updatedBy = creator.id
        )

        return userRepository.save(newUser)
    }

    fun updateUser(id: UUID, request: UpdateUserRequest, updaterId: UUID?): User {

        val updater = authorizationService.checkUserRole(updaterId, setOf(UserRole.ADMIN, UserRole.MANAGER, UserRole.USER))

        val existingUser = findUserById(id)

        if (updater.role == UserRole.USER && updater.id != existingUser.id) {
            throw AccessDeniedException("You are not authorized to update this user.")
        }

        existingUser.fullName = request.fullName
        existingUser.normalizedName = request.fullName
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")

        existingUser.updatedBy = updaterId ?: SYSTEM_USER_ID

        return userRepository.save(existingUser)
    }

    fun deleteUser(id: UUID, deleterId: UUID?) {

        authorizationService.checkUserRole(deleterId, setOf(UserRole.ADMIN))

        val userToDelete = findUserById(id)

        if (userToDelete.status == UserStatus.DELETED) {
            throw IllegalStateException("User with ID $id is already deleted.")
        }

        userToDelete.status = UserStatus.DELETED
        userToDelete.updatedBy = deleterId ?: SYSTEM_USER_ID

        userRepository.save(userToDelete)
    }

    fun getAllUsers(callerId: UUID, pageable: Pageable): Page<User> {

        authorizationService.checkUserRole(callerId, setOf(UserRole.ADMIN, UserRole.MANAGER))


        return userRepository.findByStatusNot(UserStatus.DELETED, pageable)
    }

    fun findUserByEmail(email: String): User? {
        return userRepository.findByEmail(email).orElse(null)
    }

    fun findUserById(id: UUID): User {
        return userRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("User with ID $id not found.")
    }

    fun searchUsersByName(name: String, pageable: Pageable): Page<User> {
        return userRepository.findByNormalizedNameContainingIgnoreCase(name, pageable)
    }

    fun getOrganizationsForUser(targetUserId: UUID, callerId: UUID, pageable: Pageable): Page<Organization> {

        val caller = authorizationService.checkUserRole(callerId, setOf(UserRole.ADMIN, UserRole.MANAGER, UserRole.USER))

        if (caller.role != UserRole.ADMIN && targetUserId != caller.id) {
            throw AccessDeniedException("You are not authorized to view this user's organizations.")
        }

        val user = findUserById(targetUserId)


        val organizationsList = user.organizations.toList()
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(organizationsList.size)

        val pagedList = if (start > end) {
            emptyList()
        } else {
            organizationsList.subList(start, end)
        }

        return PageImpl(pagedList, pageable, organizationsList.size.toLong())
    }
}