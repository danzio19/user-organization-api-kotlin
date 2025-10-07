package com.digitopia.casestudy.common

import jakarta.persistence.*
import java.util.Date
import java.util.UUID

@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Temporal(TemporalType.TIMESTAMP)
    val timestamp: Date = Date(),

    val userId: UUID,

    val entityType: String,

    val entityId: UUID,

    val action: String,

    @Lob
    @Column(length = 2048)
    val changeDetails: String
)