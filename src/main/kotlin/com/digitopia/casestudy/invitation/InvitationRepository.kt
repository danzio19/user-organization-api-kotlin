package com.digitopia.casestudy.invitation

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Repository
interface InvitationRepository : JpaRepository<Invitation, UUID> {


    fun findFirstByUserIdAndOrganizationIdOrderByCreatedAtDesc(userId: UUID, organizationId: UUID): Invitation?

    fun findByStatusAndCreatedAtBefore(status: InvitationStatus, date: Date): List<Invitation>

    fun findByUserId(userId: UUID, pageable: Pageable): Page<Invitation>

    fun findByOrganizationId(organizationId: UUID, pageable: Pageable): Page<Invitation>

}