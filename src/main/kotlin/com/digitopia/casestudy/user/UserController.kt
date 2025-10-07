package com.digitopia.casestudy.user

import com.digitopia.casestudy.invitation.Invitation
import com.digitopia.casestudy.invitation.InvitationResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.digitopia.casestudy.invitation.InvitationService
import com.digitopia.casestudy.organization.Organization

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val invitationService: InvitationService
) {


    @GetMapping
    fun getAllUsers(
        @RequestHeader("X-User-ID") callerId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<User>> {
        val usersPage = userService.getAllUsers(callerId, pageable)
        return ResponseEntity.ok(usersPage)
    }

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest, @RequestHeader("X-User-ID", required = false) userId: UUID?): ResponseEntity<User> {
        val user = userService.createUser(request, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @RequestBody request: UpdateUserRequest,
        @RequestHeader("X-User-ID") updaterId: UUID?
    ): ResponseEntity<User> {
        val updatedUser = userService.updateUser(id, request, updaterId)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: UUID,
        @RequestHeader("X-User-ID", required = false) deleterId: UUID?
    ): ResponseEntity<Void> {
        userService.deleteUser(id, deleterId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<User> {
        val user = userService.findUserById(id)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/search/by-email")
    fun getUserByEmail(@RequestParam email: String): ResponseEntity<User> {
        val user = userService.findUserByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/search/by-name")
    fun searchUsersByName(
        @RequestParam name: String,
        pageable: Pageable
    ): ResponseEntity<Page<User>> {
        val usersPage = userService.searchUsersByName(name, pageable)
        return ResponseEntity.ok(usersPage)
    }

    @GetMapping("/{userId}/invitations")
    fun getUserInvitations(
        @PathVariable userId: UUID,
        @RequestHeader("X-User-ID") callerId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<InvitationResponse>> {
        val invitationsPage = invitationService.getInvitationsForUser(userId, callerId, pageable)
        return ResponseEntity.ok(invitationsPage)
    }

    @GetMapping("/{userId}/organizations")
    fun getUserOrganizations(
        @PathVariable userId: UUID,
        @RequestHeader("X-User-ID") callerId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<Organization>> {
        val orgsPage = userService.getOrganizationsForUser(userId, callerId, pageable)
        return ResponseEntity.ok(orgsPage)
    }

}