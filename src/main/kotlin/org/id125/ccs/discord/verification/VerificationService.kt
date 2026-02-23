package org.id125.ccs.discord.verification

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import me.centauri07.promptlin.discord.prompt.choice.ButtonOption
import me.centauri07.promptlin.discord.prompt.choice.SelectOption
import me.centauri07.promptlin.core.form.FormSessionRegistry
import me.centauri07.promptlin.jda.JDAContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.id125.ccs.discord.AppContext
import org.id125.ccs.discord.persistence.UserProfileRepository
import org.id125.ccs.discord.profile.Campus
import org.id125.ccs.discord.profile.College
import org.id125.ccs.discord.profile.DegreeProgram
import org.id125.ccs.discord.profile.UserProfile
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class VerificationKey(val email: String)

object VerificationService {
    private val codes = ConcurrentHashMap<VerificationKey, String>() // (user, email) -> code
    private val random = SecureRandom()
    private val userProfileRepository: UserProfileRepository = AppContext.userProfileRepository

    @OptIn(ExperimentalTime::class)
    fun startVerification(executor: Member, channel: MessageChannel) {
        verificationForm.start(JDAContext(channel, executor.user)) {
            val consent = get<ButtonOption>("consent").value

            if (consent == "disagree") {
                it.sendMessage(
                    MessageCreateBuilder()
                        .setContent(
                            "You have chosen to **disagree** with the consent. We cannot proceed with verification unless you agree. " +
                                    "If this was a mistake, please restart the process."
                        )
                        .build()
                )

                return@start
            }

            val name = get<String>("name")

            val university = get<ButtonOption>("university").value

            if (university == "other") {
                if (!executor.guild.selfMember.canInteract(executor)) return@start

                executor.modifyNickname("[Visitor] $name").queue()

                if (executor.guild.idLong == AppContext.mainConfiguration.serverId) {
                    val role = executor.guild.getRoleById(AppContext.mainConfiguration.visitorRoleId) ?: return@start

                    executor.guild.addRoleToMember(executor, role).queue()
                }
                return@start
            }

            val batchId = get<String>("batch_id")

            val campus = Campus.entries.first { campus -> campus.id == get<ButtonOption>("campus").value }

            val college =
                College.entries.first { college -> college.abbreviation.lowercase() == get<SelectOption>("college").value }

            val department =
                getOrNull<SelectOption>("department")?.value?.let { department -> DegreeProgram.entries.firstOrNull { degreeProgram -> degreeProgram.id == department } }

            val email = get<String>("email")

            val userProfile = UserProfile(
                executor.idLong,
                email, batchId, college, department, campus, ""
            )

            AppContext.coroutineScope.launch(Dispatchers.IO) {
                AppContext.userProfileRepository.insert(userProfile)
            }

            channel.sendMessage(
                MessageCreateBuilder()
                    .setEmbeds(
                        EmbedBuilder().apply {
                            setColor(0x57F287) // Discord green
                            setTitle("You’re now verified!")
                            setDescription(
                                "${executor.asMention}, welcome to **id125.ccs**! 🎉\n\n" +
                                        "We’re excited to have you join our community. Here’s your quick-start guide to getting settled:\n\n" +

                                        "📢 **Stay Updated**\n" +
                                        "Check out <#1393396190669836388> for important news and server updates.\n\n" +

                                        "📝 **Know the Rules**\n" +
                                        "Before posting, please read <#1393390398772215912> so everyone can have a safe and friendly experience.\n\n" +

                                        "👤 **Set Up Your Profile & Roles**\n" +
                                        "Go to <#1398187454153883729> to choose your roles and unlock access to different parts of the server.\n\n" +

                                        "🙋 **Introduce Yourself**\n" +
                                        "After setting up your roles, say hi in <#1393601890566013191> and tell us a bit about yourself!\n\n" +

                                        "🚀 **Explore the Onboarding Hub**\n" +
                                        "Visit <#1393585851593789581> for tips, resources, and to learn more about what we offer.\n\n" +

                                        "💬 **Join the Conversation**\n" +
                                        "Head to <#1393401288619724942> to meet other members and start chatting.\n\n" +

                                        "Once you’ve gone through these steps, you’re all set to enjoy your stay! 🎯"
                            )

                            setImage("https://i.ibb.co/B9pQgyS/id125-ccs-logo-banner.png")

                            setFooter("id125.ccs • Welcome aboard!")
                        }.build()
                    )
                    .build()
            ).queue()

            if (!executor.guild.selfMember.canInteract(executor)) return@start

            if (executor.guild.idLong == AppContext.mainConfiguration.serverId) {
                val role = executor.guild.getRoleById(AppContext.mainConfiguration.verifiedRoleId) ?: return@start

                executor.guild.addRoleToMember(executor, role).queue()
            }

            executor.modifyNickname("$name | [${(department?.code ?: college.abbreviation)}] [$batchId]").queue()
        }

        val sessions = FormSessionRegistry.filter<JDAContext> { it.context.user.idLong == executor.user.idLong }
        sessions.first().timeStarted = Clock.System.now()
    }

    fun isEmailRegistered(email: String): Boolean = runBlocking { userProfileRepository.findByEmail(email) } != null

    /**
     * Generates a code, stores it, and sends the email.
     */
    fun sendVerificationCode(email: String): Result<String> {
        val code = generateVerificationCode()
        codes[VerificationKey(email)] = code // consider ConcurrentHashMap
        sendEmailVerification(email, code)

        return Result.success(code)
    }

    /**
     * Validates a verification code for a specific user and email.
     */
    fun verify(email: String, inputCode: String): Boolean {
        val key = VerificationKey(email)

        val code = codes[key] ?: return false

        val isValid = code == inputCode
        if (isValid) {
            codes.remove(key)
        }

        return isValid
    }

    @OptIn(ExperimentalTime::class)
    fun formSessionExpiration(){
        AppContext.coroutineScope.launch(Dispatchers.IO) {
            while (true) {

                // 5 seconds is hard-coded, preferably gets from a config
                delay(5.seconds)

                val time = Clock.System.now()

                for (form in FormSessionRegistry.getSessions()){

                    val elapsed: Duration = time - form.timeStarted

                    // 5 minutes is also hard-coded, preferably gets from a config
                    if (elapsed > 5.minutes)
                        FormSessionRegistry.unregister(form)

                }
            }
        }
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
                        
                        <p>Please use it to complete your verification.</p>
                        
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