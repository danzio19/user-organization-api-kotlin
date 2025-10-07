package com.digitopia.casestudy.organization

import com.digitopia.casestudy.common.BaseEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import com.digitopia.casestudy.user.User

import java.util.UUID

@Entity
@Table(name = "organizations")
class Organization(
    var organizationName: String,
    var normalizedOrganizationName: String,

    @Column(unique = true)
    var registryNumber: String,

    var contactEmail: String,
    var companySize: Int,
    var yearFounded: Int,

    @ManyToMany(mappedBy = "organizations")
    @JsonIgnore
    val users: MutableSet<User> = mutableSetOf(),

    createdBy: UUID,
    updatedBy: UUID

) : BaseEntity(createdBy = createdBy, updatedBy = updatedBy)