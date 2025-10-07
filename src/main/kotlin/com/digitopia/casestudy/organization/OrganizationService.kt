package com.digitopia.casestudy.organization

import com.digitopia.casestudy.exception.AccessDeniedException
import com.digitopia.casestudy.security.AuthorizationService
import com.digitopia.casestudy.user.User
import com.digitopia.casestudy.user.UserRepository
import com.digitopia.casestudy.user.UserRole
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

@Service
class OrganizationService(private val organizationRepository: OrganizationRepository, private val authorizationService: AuthorizationService, private val userRepository: UserRepository) {

    fun createOrganization(request: CreateOrganizationRequest, creatorId: UUID): Organization {

        val creator = authorizationService.checkUserRole(creatorId, setOf(UserRole.ADMIN, UserRole.MANAGER))

        // A registry number can be used only once
        if (organizationRepository.findByRegistryNumber(request.registryNumber).isPresent) {
            throw IllegalStateException("Organization with registry number ${request.registryNumber} already exists.")
        }

        val normalizedName = request.organizationName
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")

        val newOrganization = Organization(
            organizationName = request.organizationName,
            normalizedOrganizationName = normalizedName,
            registryNumber = request.registryNumber,
            contactEmail = request.contactEmail,
            companySize = request.companySize,
            yearFounded = request.yearFounded,
            createdBy = creator.id,
            updatedBy = creator.id
        )

        val savedOrganization = organizationRepository.save(newOrganization)

        creator.organizations.add(savedOrganization)
        userRepository.save(creator)

        return savedOrganization
    }

    fun findByRegistryNumber(registryNumber: String): Organization? {
        return organizationRepository.findByRegistryNumber(registryNumber).orElse(null)
    }

    fun findOrgById(id: UUID): Organization {
        return organizationRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Organization with ID $id not found.")
    }

    fun searchOrganizations(name: String, year: Int, size: Int, pageable: Pageable): Page<Organization> {
        return organizationRepository.search(name, year, size, pageable)
    }

    fun updateOrganization(id: UUID, request: UpdateOrganizationRequest, updaterId: UUID): Organization {

        val updater = authorizationService.checkUserRole(updaterId, setOf(UserRole.ADMIN, UserRole.MANAGER))

        val existingOrg = findOrgById(id)

        existingOrg.organizationName = request.organizationName
        existingOrg.normalizedOrganizationName = request.organizationName
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
        existingOrg.contactEmail = request.contactEmail
        existingOrg.companySize = request.companySize

        existingOrg.updatedBy = updater.id

        return organizationRepository.save(existingOrg)
    }

    fun deleteOrganization(id: UUID, deleterId: UUID) {

        authorizationService.checkUserRole(deleterId, setOf(UserRole.ADMIN))

        if (!organizationRepository.existsById(id)) {
            throw IllegalArgumentException("Organization with ID $id not found.")
        }
        organizationRepository.deleteById(id)
    }

    fun getUsersForOrganization(organizationId: UUID, callerId: UUID, pageable: Pageable): Page<User> {

        val caller = authorizationService.checkUserRole(callerId, setOf(UserRole.ADMIN, UserRole.MANAGER))

        if (caller.role == UserRole.MANAGER) {
            val isMember = caller.organizations.any { it.id == organizationId }
            if (!isMember) {
                throw AccessDeniedException("You are not authorized to view users for this organization.")
            }
        }


        return userRepository.findByOrganizations_Id(organizationId, pageable)
    }
}