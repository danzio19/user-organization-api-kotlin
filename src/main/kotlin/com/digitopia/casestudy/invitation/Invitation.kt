package com.digitopia.casestudy.invitation

import com.digitopia.casestudy.common.BaseEntity
import com.digitopia.casestudy.organization.Organization
import com.digitopia.casestudy.user.User
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "invitations")
class Invitation(

    // One User can have many invitations.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    // One Organization can have many invitations.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: Organization,

    var invitationMessage: String?, // can be nullable if a message isn't required

    @Enumerated(EnumType.STRING)
    var status: InvitationStatus = InvitationStatus.PENDING,

    createdBy: UUID,
    updatedBy: UUID

) : BaseEntity(createdBy = createdBy, updatedBy = updatedBy)

enum class InvitationStatus {
    ACCEPTED, REJECTED, PENDING, EXPIRED
}

fun Invitation.toDto(): InvitationResponse {
    return InvitationResponse(
        id = this.id,
        invitationMessage = this.invitationMessage,
        status = this.status,
        createdAt = this.createdAt,
        userSummary = UserSummaryDto(
            id = this.user.id,
            fullName = this.user.fullName,
            email = this.user.email
        ),
        organizationSummary = OrganizationSummaryDto(
            id = this.organization.id,
            organizationName = this.organization.organizationName
        )
    )
}