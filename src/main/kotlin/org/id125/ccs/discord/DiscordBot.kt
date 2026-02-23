package org.id125.ccs.discord

import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.centauri07.dc.internal.DiscordCommandManager
import me.centauri07.promptlin.core.Promptlin
import me.centauri07.promptlin.jda.JDAPlatform
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import org.id125.ccs.discord.command.moderation.purgeCommand
import org.id125.ccs.discord.command.user.profileCommand
import org.id125.ccs.discord.command.verification.verifyCommand
import org.id125.ccs.discord.command.verification.verifyPanelCommand
import org.id125.ccs.discord.configuration.MainConfiguration
import org.id125.ccs.discord.configuration.SecretsConfiguration
import org.id125.ccs.discord.configuration.YamlConfiguration
import org.id125.ccs.discord.email.EmailService
import org.id125.ccs.discord.persistence.UserProfileRepository
import org.id125.ccs.discord.profile.UserProfile
import org.id125.ccs.discord.verification.VerificationListener
import org.id125.ccs.discord.verification.VerificationService

fun main() {
    AppContext.start()
}

object AppContext {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val secrets = YamlConfiguration(
        "./conf", "secrets", SecretsConfiguration(), SecretsConfiguration.serializer()
    ).load()

    val mainConfiguration = YamlConfiguration(
        "./conf", "config", MainConfiguration(), MainConfiguration.serializer()
    ).load()

    val emailService = EmailService(
        "smtp.gmail.com", 587, secrets.emailAddress, secrets.emailPassword
    )

    private val client = MongoClient.create(secrets.mongodbConnectionString)
    private val database = client.getDatabase("id125ccs")

    val userProfileRepository by lazy {
        UserProfileRepository(database.getCollection<UserProfile>("profiles"))
    }

    val jda: JDA by lazy {
        JDABuilder.createDefault(secrets.discordToken)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
            .build()
    }

    val discordCommandManager: DiscordCommandManager by lazy {
        DiscordCommandManager(jda, "!")
    }

    fun start() {
        userProfileRepository

        jda.setEventManager(AnnotatedEventManager())
        jda.addEventListener(VerificationListener)

        Promptlin.configure {
            platform(JDAPlatform(jda))
        }

        jda.awaitReady()

        verifyPanelCommand
        verifyCommand
        purgeCommand
        profileCommand
        VerificationService.formSessionExpiration()
    }
}
