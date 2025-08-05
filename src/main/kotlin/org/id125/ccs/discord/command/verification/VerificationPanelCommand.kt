package org.id125.ccs.discord.command.verification

import me.centauri07.dc.api.argument.Argument
import me.centauri07.dc.api.executor.Executor
import me.centauri07.dc.api.response.Response
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.id125.ccs.discord.AppContext
import java.awt.Color

val verifyPanelCommand by lazy {
    AppContext.discordCommandManager.createSlashCommand("vpanel", "Show verify panel") {
        permissions {
            +Permission.ADMINISTRATOR
        }

        option(OptionType.CHANNEL, "channel", "Channel location to send the verification panel")

        executor = object : Executor {
            override fun onCommand(
                executor: Member,
                arguments: List<Argument>,
                event: Event
            ): Response {
                val channel = arguments[0].value as MessageChannel

                val message = channel.sendMessage(
                    MessageCreateBuilder()
                        .setEmbeds(
                            EmbedBuilder()
                                .setTitle("Welcome to id125.ccs!")
                                .setDescription("To access the rest of the server, you need to verify your identity.")
                                .addField(
                                    "How to Verify",
                                    """
                                    1. Click the **Verify** button below
                                    2. Follow the instructions that appear
                                    3. Once verified, you will gain access to all channels
                                    """.trimIndent(),
                                    false
                                )
                                .addField(
                                    "Everyone is Welcome",
                                    "Whether you're from **DLSU**, another DLSU college, or not from DLSU at all — you are free to join!",
                                    false
                                )
                                .addField(
                                    "Need Help?",
                                    "If you encounter any issues, please **contact a moderator** for assistance.",
                                    false
                                )
                                .setColor(Color(0x22C55E))
                                .setImage("https://i.ibb.co/B9pQgyS/id125-ccs-logo-banner.png")
                                .setFooter("id125.ccs • Safe & Welcoming Community")
                                .build()
                        )
                        .setActionRow(
                            Button.of(
                                ButtonStyle.SUCCESS,
                                "dlsu-verify",
                                "Verify",
                                Emoji.fromUnicode("✅")
                            )
                        )
                        .build()
                ).complete()

                return Response.of("Verification panel has been sent. ${message.channel.asMention}").setEphemeral()
            }
        }
    }
}