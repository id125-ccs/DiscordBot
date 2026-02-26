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
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import net.dv8tion.jda.api.hooks.SubscribeEvent
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
import org.id125.ccs.discord.utility.callback.Callback
import org.id125.ccs.discord.verification.VerificationListener
import org.id125.ccs.discord.verification.VerificationService

fun main() {
    AppContext.start()
}

object AppContext {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val secrets = SecretsConfiguration(
        System.getenv("DISCORD_TOKEN"),

        System.getenv("EMAIL_ADDRESS"),
        System.getenv("EMAIL_PASSWORD"),

        System.getenv("DB_CONNECTION"),
        System.getenv("DB_NAME"),
        System.getenv("DB_COLLECTION")
    )

    val mainConfiguration = YamlConfiguration(
        "/app/config", "config", MainConfiguration(listOf()),
        MainConfiguration.serializer()
    ).also { it.load() }.value

    val emailService = EmailService(
        "smtp.gmail.com", 587, secrets.emailAddress, secrets.emailPassword
    )

    private val client = MongoClient.create(secrets.mongodbConnectionString)
    private val database = client.getDatabase(secrets.databaseName)

    val userProfileRepository by lazy {
        UserProfileRepository(database.getCollection<UserProfile>(secrets.databaseCollection))
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
        jda.addEventListener(this)

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

    // Callbacks
    @SubscribeEvent
    fun onEvent(e: Event) {
        if (e is ModalInteractionEvent) Callback.MODAL.handle(e)
    }
}
