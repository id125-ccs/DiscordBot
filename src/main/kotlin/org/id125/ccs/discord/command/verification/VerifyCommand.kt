package org.id125.ccs.discord.command.verification

import kotlinx.coroutines.runBlocking
import me.centauri07.dc.api.argument.Argument
import me.centauri07.dc.api.executor.Executor
import me.centauri07.dc.api.response.Response
import me.centauri07.promptlin.core.form.FormSessionRegistry
import me.centauri07.promptlin.jda.JDAContext
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.id125.ccs.discord.AppContext
import org.id125.ccs.discord.verification.VerificationService

val verifyCommand by lazy {
    AppContext.discordCommandManager.createSlashCommand("verify", "Verify your account") {

        executor = object : Executor {
            override fun onCommand(
                executor: Member,
                arguments: List<Argument>,
                event: Event
            ): Response {
                val slashEvent = event as SlashCommandInteractionEvent

                val user = runBlocking {
                    AppContext.userProfileRepository.findById(executor.idLong)
                }

                if (user != null) {
                    return Response.of("You are already verified.").setEphemeral()
                }

                if (FormSessionRegistry.contains<JDAContext> { it.context.user.idLong == executor.user.idLong }) {
                    return Response.of("You are currently in an ongoing session.").setEphemeral()
                }

                val channel = slashEvent.user.openPrivateChannel().complete()

                VerificationService.startVerification(executor, channel)

                return Response.of("Verification process has began. Please check your DMs. ${channel.asMention})\n\n" +
                        "__**| IMPORTANT |**__ Should you wish to cancel this verification process, type `ccs!fcancel`")
                    .setEphemeral()
            }
        }
    }

}