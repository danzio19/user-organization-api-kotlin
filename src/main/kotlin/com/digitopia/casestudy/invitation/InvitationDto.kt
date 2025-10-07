package com.digitopia.casestudy.invitation

import java.util.Date
import java.util.UUID

data class SendInvitationRequest(
    val userId: UUID,
    val organizationId: UUID,
    val message: String?
)

data class UpdateInvitationStatusRequest(
    val status: InvitationStatus
)

data class InvitationResponse(
    val id: UUID,
    val invitationMessage: String?,
    val status: InvitationStatus,
    val createdAt: Date,
    val userSummary: UserSummaryDto,
    val organizationSummary: OrganizationSummaryDto
)

data class UserSummaryDto(
    val id: UUID,
    val fullName: String,
    val email: String
)

data class OrganizationSummaryDto(
    val id: UUID,
    val organizationName: String
)