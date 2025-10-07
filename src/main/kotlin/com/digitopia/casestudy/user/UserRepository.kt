package com.digitopia.casestudy.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


@Repository
interface UserRepository : JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address. Since emails are unique,
     * this will return at most one user
     *
     */
    fun findByEmail(email: String): Optional<User>

    /**
     * Finds a list of users whose normalized name contains the given search term.
     */
    fun findByNormalizedNameContainingIgnoreCase(name: String, pageable: Pageable): Page<User>

    fun findByStatusNot(status: UserStatus, pageable: Pageable): Page<User>

    fun findByOrganizations_Id(organizationId: UUID, pageable: Pageable): Page<User>
}