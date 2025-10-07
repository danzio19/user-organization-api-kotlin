package com.digitopia.casestudy.organization

import com.digitopia.casestudy.invitation.Invitation
import com.digitopia.casestudy.invitation.InvitationResponse
import com.digitopia.casestudy.invitation.InvitationService
import com.digitopia.casestudy.user.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

@RestController
@RequestMapping("/organizations")
class OrganizationController(private val organizationService: OrganizationService, private val invitationService: InvitationService) {


    @PostMapping
    fun createOrganization(@RequestBody request: CreateOrganizationRequest, @RequestHeader("X-User-ID") creatorId: UUID): ResponseEntity<Organization> {
        val organization = organizationService.createOrganization(request, creatorId)
        return ResponseEntity.status(HttpStatus.CREATED).body(organization)
    }

    @GetMapping("/search/by-registry")
    fun getOrganizationByRegistryNumber(@RequestParam number: String): ResponseEntity<Organization> {
        val organization = organizationService.findByRegistryNumber(number)
        return if (organization != null) {
            ResponseEntity.ok(organization)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}")
    fun getOrganizationById(@PathVariable id: UUID): ResponseEntity<Organization> {
        val organization = organizationService.findOrgById(id)
        return ResponseEntity.ok(organization)
    }


    @GetMapping("/search")
    fun searchOrganizations(
        @RequestParam name: String,
        @RequestParam year: Int,
        @RequestParam size: Int,
        pageable: Pageable
    ): ResponseEntity<Page<Organization>> {
        val organizationsPage = organizationService.searchOrganizations(name, year, size, pageable)
        return ResponseEntity.ok(organizationsPage)
    }

    @PutMapping("/{id}")
    fun updateOrganization(
        @PathVariable id: UUID,
        @RequestBody request: UpdateOrganizationRequest,
        @RequestHeader("X-User-ID") updaterId: UUID
    ): ResponseEntity<Organization> {
        val updatedOrg = organizationService.updateOrganization(id, request, updaterId)
        return ResponseEntity.ok(updatedOrg)
    }

    @DeleteMapping("/{id}")
    fun deleteOrganization(@PathVariable id: UUID, @RequestHeader("X-User-ID") deleterId: UUID): ResponseEntity<Void> {
        organizationService.deleteOrganization(id, deleterId)
        return ResponseEntity.noContent().build() // Return 204 No Content
    }

    @GetMapping("/{organizationId}/invitations")
    fun getOrganizationInvitations(
        @PathVariable organizationId: UUID,
        @RequestHeader("X-User-ID") callerId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<InvitationResponse>> {
        val invitationsPage = invitationService.getInvitationsForOrganization(organizationId, callerId, pageable)
        return ResponseEntity.ok(invitationsPage)
    }

    @GetMapping("/{organizationId}/users")
    fun getOrganizationUsers(
        @PathVariable organizationId: UUID,
        @RequestHeader("X-User-ID") callerId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<User>> {
        val usersPage = organizationService.getUsersForOrganization(organizationId, callerId, pageable)
        return ResponseEntity.ok(usersPage)
    }
}