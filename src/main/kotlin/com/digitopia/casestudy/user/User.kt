package com.digitopia.casestudy.user

import com.digitopia.casestudy.common.BaseEntity
import jakarta.persistence.*
import java.util.UUID
import com.digitopia.casestudy.organization.Organization

@Entity
@Table(name = "users")
class User(
    var email: String,
    var fullName: String,
    var normalizedName: String,

    @Enumerated(EnumType.STRING)
    var status: UserStatus = UserStatus.PENDING,

    @Enumerated(EnumType.STRING)
    var role: UserRole = UserRole.USER,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_organizations", // Name of the new table
        joinColumns = [JoinColumn(name = "user_id")], // Column in the new table that points to this entity (User)
        inverseJoinColumns = [JoinColumn(name = "organization_id")] // Column that points to the other entity (Organization)
    )
    val organizations: MutableSet<Organization> = mutableSetOf(),

    createdBy: UUID,
    updatedBy: UUID

) : BaseEntity(createdBy = createdBy, updatedBy = updatedBy)

enum class UserStatus {
    ACTIVE, PENDING, DEACTIVATED, DELETED
}

enum class UserRole {
    ADMIN, MANAGER, USER
}