package com.digitopia.casestudy.organization

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

@Repository
interface OrganizationRepository : JpaRepository<Organization, UUID> {


    fun findByRegistryNumber(registryNumber: String): Optional<Organization>

    @Query("SELECT o FROM Organization o WHERE " +
            "LOWER(o.normalizedOrganizationName) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
            "o.yearFounded = :year AND " +
            "o.companySize = :size")
    fun search(name: String, year: Int, size: Int, pageable: Pageable): Page<Organization>
}