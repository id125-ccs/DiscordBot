package org.id125.ccs.discord.verification

import kotlinx.coroutines.runBlocking
import org.id125.ccs.discord.AppContext
import org.id125.ccs.discord.persistence.UserProfileRepository
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

data class VerificationKey(val email: String)

object VerificationService {
    private val codes = ConcurrentHashMap<VerificationKey, String>() // (user, email) -> code
    private val random = SecureRandom()
    private val userProfileRepository: UserProfileRepository = AppContext.userProfileRepository

    fun isEmailRegistered(email: String): Boolean = runBlocking { userProfileRepository.findByEmail(email) } != null

    /**
     * Start a verification for a specific user and email.
     * Generates a code, stores it, and sends the email.
     */
    fun startVerification(email: String): Result<String> {
        val code = generateVerificationCode()
        codes[VerificationKey(email)] = code // consider ConcurrentHashMap
        sendEmailVerification(email, code)

        return Result.success(code)
    }

    /**
     * Retrieves the verification code for a specific user and email, or null if not found.
     */
    fun getVerificationCode(email: String): String? =
        codes[VerificationKey(email)]

    /**
     * Validates a verification code for a specific user and email.
     */
    fun validateVerification(email: String, inputCode: String): Boolean {
        val key = VerificationKey(email)
        return codes[key] == inputCode
    }

    /**
     * Sends a styled HTML verification email.
     */
    private fun sendEmailVerification(email: String, code: String) {
        val htmlBody = """
            <html>
            <head>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, sans-serif; 
                        background-color: #f4f4f7; 
                        margin: 0; 
                        padding: 0; 
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 40px auto; 
                        background: white; 
                        border-radius: 16px; 
                        overflow: hidden;
                        box-shadow: 0 6px 24px rgba(0,0,0,0.08); 
                    }
                    .header { 
                        background: linear-gradient(135deg, #00743C, #004B25); 
                        color: white; 
                        padding: 20px; 
                        text-align: center; 
                        font-size: 24px; 
                        font-weight: bold; 
                        letter-spacing: 1px;
                    }
                    .content { 
                        padding: 30px; 
                        color: #333; 
                        line-height: 1.6;
                        text-align: center;    
                    }
                    .code-box {
                        display: block;
                        background: #EAF5EF;
                        color: #00743C;
                        font-size: 28px;
                        font-weight: bold;
                        letter-spacing: 6px;
                        text-align: center;
                        padding: 15px 20px;
                        margin: 30px auto;
                        border-radius: 12px;
                        border: 2px solid #C4E3D0;
                        width: fit-content;
                        min-width: 200px;
                    }
                    .notice {
                        background: #FFF8F2;
                        border-left: 4px solid #E67E22;
                        padding: 12px 16px;
                        margin: 20px 0;
                        font-size: 14px;
                        color: #7B3F00;
                        border-radius: 6px;
                    }
                    .footer { 
                        text-align: center; 
                        font-size: 12px; 
                        color: #888; 
                        padding: 20px; 
                        background: #fafafa; 
                        border-top: 1px solid #eee;
                    }
                    .username {
                        font-weight: bold; 
                        color: #00743C;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">id125.ccs Verification</div>
                    <div class="content">
                        <p>Hello,</p>
                        <p>A Discord user has requested to verify this email for their <span class="username">id125.ccs</span> account.</p>
                        
                        <p>Your verification code is:</p>
                        <div class="code-box">$code</div>
                        
                        <p>This code will expire in <b>10 minutes</b>. Please use it to complete your verification.</p>
                        
                        <div class="notice">
                            If you did not request this verification, please report this to our team immediately.
                        </div>
                    </div>
                    <div class="footer">
                        © 2025 id125.ccs — This is an automated email. Do not reply.
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        AppContext.emailService.sendHtmlEmail(
            to = email,
            subject = "Email verification code: $code",
            htmlBody = htmlBody
        )
    }

    /**
     * Generates a 6-digit numeric verification code.
     */
    private fun generateVerificationCode(): String {
        return (100000 + random.nextInt(900000)).toString()
    }

}