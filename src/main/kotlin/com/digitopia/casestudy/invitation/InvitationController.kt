package com.digitopia.casestudy.invitation

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID


@RestController
@RequestMapping("/invitations")
class InvitationController(private val invitationService: InvitationService) {

    @PostMapping
    fun sendInvitation(@RequestBody request: SendInvitationRequest,
                       @RequestHeader("X-User-ID") callerId: UUID): ResponseEntity<InvitationResponse> {
        val invitation = invitationService.sendInvitation(request, callerId)
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation)
    }

    @GetMapping("/{id}")
    fun getInvitationById(@PathVariable id: UUID): ResponseEntity<InvitationResponse> {
        val invitation = invitationService.findInvitationById(id)
        return ResponseEntity.ok(invitation)
    }





    @PutMapping("/{id}/status")
    fun updateInvitationStatus(
        @PathVariable id: UUID,
        @RequestBody request: UpdateInvitationStatusRequest,
        @RequestHeader("X-User-ID") updaterId: UUID
    ): ResponseEntity<InvitationResponse> {
        val updatedInvitation = invitationService.updateInvitationStatus(id, request.status, updaterId)
        return ResponseEntity.ok(updatedInvitation)
    }

    @DeleteMapping("/{id}")
    fun deleteInvitation(
        @PathVariable id: UUID,
        @RequestHeader("X-User-ID") callerId: UUID
    ): ResponseEntity<Void> {
        invitationService.deleteInvitation(id, callerId)
        return ResponseEntity.noContent().build()
    }
}