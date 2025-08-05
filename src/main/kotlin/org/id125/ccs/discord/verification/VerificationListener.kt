package org.id125.ccs.discord.verification

import me.centauri07.dc.api.response.Response
import me.centauri07.promptlin.core.form.FormSessionRegistry
import me.centauri07.promptlin.jda.ButtonClickedListener
import me.centauri07.promptlin.jda.JDAContext
import me.centauri07.promptlin.jda.MessageReceivedListener
import me.centauri07.promptlin.jda.SelectionMenuListener
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent

object VerificationListener {

    val responses = listOf(
        """
        Yo, relax lang.  
        Wala pa tayong DLSU email, so di ka pa pwede mag-verify.  
        Chill ka muna, maybe grab water or something. 🥤
        """.trimIndent(),

        """
        Easy lang, bro.  
        Like, di pa tayo verified kasi no DLSU email yet.  
        Just wait it out, it’s coming soon. ⏳
        """.trimIndent(),

        """
        Dude, calm down.  
        Super eager ka mag-verify pero we can’t yet—no email pa.  
        Touch some grass muna, then try again later. 🌿
        """.trimIndent()
    )

    @SubscribeEvent
    fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.button.id.equals("dlsu-verify" , true)) {
            event.reply(responses.random()).setEphemeral(true).queue()
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