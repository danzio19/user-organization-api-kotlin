package com.digitopia.casestudy.common.service

import com.digitopia.casestudy.common.AuditLog
import com.digitopia.casestudy.common.AuditLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class AuditLogService(private val auditLogRepository: AuditLogRepository) {


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveAuditLog(auditLog: AuditLog) {
        auditLogRepository.save(auditLog)
    }
}