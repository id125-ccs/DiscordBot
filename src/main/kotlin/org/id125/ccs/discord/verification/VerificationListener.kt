package org.id125.ccs.discord.verification

import kotlinx.coroutines.runBlocking
import me.centauri07.dc.api.response.Response
import me.centauri07.promptlin.core.form.FormSessionRegistry
import me.centauri07.promptlin.jda.ButtonClickedListener
import me.centauri07.promptlin.jda.JDAContext
import me.centauri07.promptlin.jda.MessageReceivedListener
import me.centauri07.promptlin.jda.SelectionMenuListener
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import org.id125.ccs.discord.AppContext

object VerificationListener {

    @SubscribeEvent
    fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (!event.button.id.equals("dlsu-verify")) return
        val executor = event.member ?: return

        val user = runBlocking {
            AppContext.userProfileRepository.findById(executor.idLong)
        }

        if (user != null) {
            event.reply("You are already verified.").setEphemeral(true).queue()
            return
        }

        if (FormSessionRegistry.contains<JDAContext> { it.context.user.idLong == executor.user.idLong }) {
            event.reply("You are currently in an ongoing session.").setEphemeral(true).queue()
            return
        }

        event.user.openPrivateChannel().queue {
            VerificationService.startVerification(executor, it)

            event.reply("Verification process has began. Please check your DMs. ${it.asMention})\n\n" +
                    "__**| IMPORTANT |**__ Should you wish to cancel this verification process, type `ccs!fcancel`").setEphemeral(true).queue()
        }
    }

    @SubscribeEvent
    fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.contentRaw != "ccs!fcancel") return

        if (!FormSessionRegistry.contains<JDAContext>{ it.context.user.id == event.author.id }) {
            return event.message.reply("You have no active session.").mentionRepliedUser(false).queue()
        }

        MessageReceivedListener.remove(event.author.idLong)
        ButtonClickedListener.remove(event.author.idLong)
        SelectionMenuListener.remove(event.author.idLong)

        FormSessionRegistry.unregister<JDAContext> { it.context.user.id == event.author.id }

        event.message.reply("Session has been successfully canceled.").mentionRepliedUser(false).queue()
    }

}