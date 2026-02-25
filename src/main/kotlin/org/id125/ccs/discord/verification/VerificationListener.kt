package org.id125.ccs.discord.verification

import kotlinx.coroutines.runBlocking
import me.centauri07.promptlin.core.form.FormSessionRegistry
import me.centauri07.promptlin.jda.ButtonClickedListener
import me.centauri07.promptlin.jda.JDAContext
import me.centauri07.promptlin.jda.MessageReceivedListener
import me.centauri07.promptlin.jda.SelectionMenuListener
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.id125.ccs.discord.AppContext
import org.id125.ccs.discord.utility.callback.Callback

object VerificationListener {

    @SubscribeEvent
    fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (!event.button.id.equals("dlsu-verify")) return
        val executor = event.member ?: return

        val user = runBlocking {
            AppContext.userProfileRepository.findById(executor.idLong)
        }

        if (user != null) {
            val member = event.member ?: return

            val serverConfiguration = AppContext.mainConfiguration.serversConfiguration.firstOrNull {
                it.serverId == member.guild.idLong
            } ?: return

            if (!member.roles.any { it.idLong == serverConfiguration.verifiedRoleId }) {
                val id = "callback:verification:name:g_${member.guild.idLong}:u_${member.idLong}"

                val suffix = " | [${user.degreeProgram}] [${user.batchId}]"
                val maxBaseLength = 32 - suffix.length

                val modal = Modal.create(id, "Enter name")
                    .addActionRow(
                        TextInput.create(
                            "name",
                            "Your Name",
                            TextInputStyle.SHORT
                        )
                            .setRequired(true)
                            .setMaxLength(maxBaseLength)
                            .build()
                    )
                    .build()

                Callback.MODAL.register(id) {
                    val baseName = getValue("name")?.asString
                        ?.trim()
                        ?.take(maxBaseLength)
                        ?: return@register

                    val finalName = baseName + suffix

                    if (member.guild.selfMember.canInteract(member)) {
                        member.guild.modifyNickname(member, finalName).queue()

                        listOfNotNull(
                            serverConfiguration.verifiedRoleId,
                            serverConfiguration.collegeRoles[user.college]
                        )
                            .mapNotNull { event.jda.getRoleById(it) }
                            .forEach { member.guild.addRoleToMember(member, it).queue() }
                    }
                }

                event.replyModal(modal).queue()

                return
            }

            event.reply("You are already verified.").setEphemeral(true).queue()
            return
        }

        if (FormSessionRegistry.contains<JDAContext> { it.context.user.idLong == executor.user.idLong }) {
            event.reply("You are currently in an ongoing session.").setEphemeral(true).queue()
            return
        }

        val channel = event.user.openPrivateChannel().complete()

        event.reply(
            "Verification process has began. Please check your DMs. ${channel.asMention})\n\n" +
                    "__**| IMPORTANT |**__ Should you wish to cancel this verification process, type `ccs!fcancel`"
        ).setEphemeral(true).queue()

        VerificationService.startVerification(executor, channel)
    }

    @SubscribeEvent
    fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.contentRaw != "ccs!fcancel") return

        if (!FormSessionRegistry.contains<JDAContext> { it.context.user.id == event.author.id }) {
            return event.message.reply("You have no active session.").mentionRepliedUser(false).queue()
        }

        MessageReceivedListener.remove(event.author.idLong)
        ButtonClickedListener.remove(event.author.idLong)
        SelectionMenuListener.remove(event.author.idLong)

        FormSessionRegistry.unregister<JDAContext> { it.context.user.id == event.author.id }

        event.message.reply("Session has been successfully canceled.").mentionRepliedUser(false).queue()
    }

}