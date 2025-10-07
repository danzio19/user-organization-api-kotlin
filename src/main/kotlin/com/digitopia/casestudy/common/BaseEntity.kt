package com.digitopia.casestudy.common

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.Date
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @CreationTimestamp
    val createdAt: Date = Date(),

    @UpdateTimestamp
    var updatedAt: Date = Date(),

    var createdBy: UUID,
    var updatedBy: UUID
)