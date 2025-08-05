package org.id125.ccs.discord.email

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import java.util.Properties

class EmailService(
    private val smtpHost: String,
    private val smtpPort: Int,
    private val username: String,
    private val password: String,
    private val useTLS: Boolean = true
) {

    private val session: Session by lazy {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", useTLS.toString())
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort.toString())
        }

        Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(username, password)
        })
    }

    /**
     * Sends an HTML email with optional no-reply headers.
     */
    fun sendHtmlEmail(
        to: String,
        subject: String,
        htmlBody: String,
        fromName: String = "No Reply",
        noReply: Boolean = true
    ) {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(username, fromName))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            this.subject = subject

            val htmlPart = MimeBodyPart().apply {
                setContent(htmlBody, "text/html; charset=utf-8")
            }

            val multipart = MimeMultipart().apply {
                addBodyPart(htmlPart)
            }

            setContent(multipart)

            if (noReply) {
                setHeader("Reply-To", "<>")
                setHeader("Auto-Submitted", "auto-generated")
                setHeader("X-Auto-Response-Suppress", "All")
            }
        }

        Transport.send(message)
    }
}