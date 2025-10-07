package com.digitopia.casestudy.invitation

import com.digitopia.casestudy.common.service.EmailService
import com.digitopia.casestudy.exception.AccessDeniedException
import com.digitopia.casestudy.organization.OrganizationRepository
import com.digitopia.casestudy.security.AuthorizationService
import com.digitopia.casestudy.user.UserRepository
import com.digitopia.casestudy.user.UserRole
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository,
    private val authorizationService: AuthorizationService,
    private val emailService: EmailService,
) {
    private val logger = LoggerFactory.getLogger(InvitationService::class.java)

    private fun getInvitationEntityById(id: UUID): Invitation {
        return invitationRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Invitation with ID $id not found.")
    }

    fun findInvitationById(id: UUID): InvitationResponse {
        return getInvitationEntityById(id).toDto()
    }

    fun getInvitationsForUser(targetUserId: UUID, callerId: UUID, pageable: Pageable): Page<InvitationResponse> {
        if (targetUserId != callerId) {
            throw AccessDeniedException("You are not authorized to view this user's invitations.")
        }
        authorizationService.checkUserRole(callerId, setOf(UserRole.ADMIN, UserRole.MANAGER, UserRole.USER))
        val invitationsPage = invitationRepository.findByUserId(targetUserId, pageable)
        return invitationsPage.map { it.toDto() }
    }

    fun getInvitationsForOrganization(organizationId: UUID, callerId: UUID, pageable: Pageable): Page<InvitationResponse> {
        val caller = authorizationService.checkUserRole(callerId, setOf(UserRole.ADMIN, UserRole.MANAGER))
        val isMember = caller.organizations.any { it.id == organizationId }
        if (!isMember && caller.role != UserRole.ADMIN) {
            throw AccessDeniedException("You are not a member of this organization or do not have permission to view its invitations.")
        }
        val invitationsPage = invitationRepository.findByOrganizationId(organizationId, pageable)
        return invitationsPage.map { it.toDto() }
    }

    fun sendInvitation(request: SendInvitationRequest, callerId: UUID): InvitationResponse {

        val caller = authorizationService.checkUserRole(callerId, setOf(UserRole.ADMIN, UserRole.MANAGER))
        if (caller.role == UserRole.MANAGER) {
            val isMember = caller.organizations.any { it.id == request.organizationId }
            if (!isMember) {
                throw AccessDeniedException("Managers can only send invitations from organizations they are a member of.")
            }
        }
        val targetUser = userRepository.findByIdOrNull(request.userId)
            ?: throw IllegalArgumentException("User with ID ${request.userId} not found.")
        val organization = organizationRepository.findByIdOrNull(request.organizationId)
            ?: throw IllegalArgumentException("Organization with ID ${request.organizationId} not found.")
        val isAlreadyMember = targetUser.organizations.any { it.id == organization.id }
        if (isAlreadyMember) {
            throw IllegalStateException("User ${targetUser.fullName} is already a member of ${organization.organizationName}.")
        }
        val lastInvitation = invitationRepository.findFirstByUserIdAndOrganizationIdOrderByCreatedAtDesc(request.userId, request.organizationId)
        if (lastInvitation != null) {
            if (lastInvitation.status == InvitationStatus.REJECTED) {
                throw IllegalStateException("Cannot re-invite a user whose last invitation was rejected.")
            }
            if (lastInvitation.status == InvitationStatus.PENDING) {
                throw IllegalStateException("A pending invitation for this user and organization already exists.")
            }
        }
        val newInvitation = Invitation(user = targetUser, organization = organization, invitationMessage = request.message, status = InvitationStatus.PENDING, createdBy = caller.id, updatedBy = caller.id)
        val savedInvitation = invitationRepository.save(newInvitation)

        emailService.sendInvitationEmail(savedInvitation)

        return savedInvitation.toDto()
    }

    fun updateInvitationStatus(id: UUID, newStatus: InvitationStatus, updaterId: UUID): InvitationResponse {
        if (newStatus !in listOf(InvitationStatus.ACCEPTED, InvitationStatus.REJECTED)) {
            throw IllegalStateException("Invitation status can only be updated to ACCEPTED or REJECTED.")
        }


        val invitation = getInvitationEntityById(id)

        if (invitation.status != InvitationStatus.PENDING) {
            throw IllegalStateException("Only pending invitations can be updated. This invitation is currently ${invitation.status}.")
        }

        invitation.status = newStatus
        invitation.updatedBy = updaterId

        if (newStatus == InvitationStatus.ACCEPTED) {
            val user = invitation.user
            val organization = invitation.organization
            user.organizations.add(organization)
            userRepository.save(user)
        }


        val savedInvitation = invitationRepository.save(invitation)
        return savedInvitation.toDto()
    }

    fun deleteInvitation(id: UUID, callerId: UUID) {
        val caller = authorizationService.checkUserRole(callerId, setOf(UserRole.ADMIN, UserRole.MANAGER, UserRole.USER))


        val invitation = getInvitationEntityById(id)

        val isAdmin = caller.role == UserRole.ADMIN
        val isOriginalSender = caller.id == invitation.createdBy

        if (!isAdmin && !isOriginalSender) {
            throw AccessDeniedException("You are not authorized to delete this invitation.")
        }

        if (invitation.status != InvitationStatus.PENDING) {
            throw IllegalStateException("Only PENDING invitations can be deleted/revoked.")
        }

        invitationRepository.deleteById(id)
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    fun expireOldInvitations() {

        logger.info("Running scheduled job to expire old invitations...")
        val sevenDaysAgo = Date.from(Instant.now().minus(7, ChronoUnit.DAYS))
        val invitationsToExpire = invitationRepository.findByStatusAndCreatedAtBefore(InvitationStatus.PENDING, sevenDaysAgo)
        if (invitationsToExpire.isEmpty()) {
            logger.info("No pending invitations to expire.")
            return
        }
        logger.info("Found ${invitationsToExpire.size} invitations to expire.")
        val expiredInvitations = invitationsToExpire.map { invitation ->
            invitation.status = InvitationStatus.EXPIRED
            invitation.updatedBy = UUID.fromString("00000000-0000-0000-0000-000000000001")
            invitation
        }
        invitationRepository.saveAll(expiredInvitations)
        logger.info("Successfully expired ${expiredInvitations.size} invitations.")
    }
}