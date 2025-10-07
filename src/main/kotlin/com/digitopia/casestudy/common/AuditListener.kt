package com.digitopia.casestudy.common

import jakarta.persistence.PostPersist
import jakarta.persistence.PostUpdate
import jakarta.persistence.PreUpdate
import java.util.UUID
import com.digitopia.casestudy.common.service.AuditLogService

class AuditListener {

    private val oldState = mutableMapOf<UUID, String>()

    @PreUpdate
    fun onPreUpdate(entity: Any) {
        if (entity is BaseEntity) {
            oldState[entity.id] = entity.toString()
        }
    }

    @PostUpdate
    fun onPostUpdate(entity: Any) {
        if (entity is BaseEntity) {
            val oldEntityState = oldState.remove(entity.id)
            val newEntityState = entity.toString()

            val details = "FROM: $oldEntityState\nTO: $newEntityState"

            val auditLog = AuditLog(
                userId = entity.updatedBy,
                entityType = entity.javaClass.simpleName,
                entityId = entity.id,
                action = "UPDATE",
                changeDetails = details
            )
            saveAuditLog(auditLog)
        }
    }

    @PostPersist
    fun onPostPersist(entity: Any) {
        if (entity is BaseEntity) {
            val details = "CREATED: ${entity.toString()}"

            val auditLog = AuditLog(
                userId = entity.createdBy,
                entityType = entity.javaClass.simpleName,
                entityId = entity.id,
                action = "CREATE",
                changeDetails = details
            )
            saveAuditLog(auditLog)
        }
    }

    private fun saveAuditLog(auditLog: AuditLog) {

        val auditLogService = BeanUtil.ctx.getBean(AuditLogService::class.java)

        auditLogService.saveAuditLog(auditLog)
    }
}