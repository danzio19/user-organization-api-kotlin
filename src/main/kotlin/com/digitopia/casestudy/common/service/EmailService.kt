package com.digitopia.casestudy.common.service

import com.digitopia.casestudy.invitation.Invitation
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {

    fun sendInvitationEmail(invitation: Invitation) {
        val recipientEmail = invitation.user.email
        val organizationName = invitation.organization.organizationName

        val message = SimpleMailMessage()
        message.setFrom("noreply@digitopia.com")
        message.setTo(recipientEmail)
        message.setSubject("You have been invited to join $organizationName!")
        message.setText(
            """
            Hello ${invitation.user.fullName},

            You have received an invitation to join the organization "$organizationName".
            
            Message from the sender:
            "${invitation.invitationMessage ?: "No message provided."}"

            Please log in to the application to accept or reject this invitation.

            Thank you,
            The Digitopia Team
            """.trimIndent()
        )

        try {
            mailSender.send(message)

            println("Successfully sent invitation email to $recipientEmail")
        } catch (e: Exception) {

            println("Failed to send email to $recipientEmail: ${e.message}")
        }
    }
}