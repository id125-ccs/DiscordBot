package org.id125.ccs.discord.command.user

import kotlinx.coroutines.runBlocking
import me.centauri07.dc.api.argument.Argument
import me.centauri07.dc.api.executor.Executor
import me.centauri07.dc.api.response.Response
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.id125.ccs.discord.AppContext
import java.awt.Color

val profileCommand by lazy {
    AppContext.discordCommandManager.createSlashCommand("profile", "Manage and view profiles") {

        subCommand("view", "View someone's profile") {
            option(OptionType.USER, "user", "user to view") {
                required = false
            }

            executor = object : Executor {
                override fun onCommand(
                    executor: Member,
                    arguments: List<Argument>,
                    event: Event
                ): Response {
                    val target = if (arguments.size == 1) arguments[0].value as User else executor.user

                    val profile = runBlocking { AppContext.userProfileRepository.findById(target.idLong) }
                        ?: return Response.of("User ${target.asMention} has no profile").setEphemeral()

                    return Response.of(
                        EmbedBuilder().apply {
                            setTitle("${target.effectiveName}'s Profile")
                            setColor(Color(0x1ABC9C)) // Teal color

                            addField("🎓 Batch", profile.batchId, true)
                            addField("🏫 College", profile.college.displayName, true)
                            addBlankField(true)
                            profile.degreeProgram?.let {
                                addField("📖 Degree Program", profile.degreeProgram.displayName, true)
                            }
                            addField("📍 Campus", profile.campus.name, true)
                            addBlankField(true)
                            addField("💻 GitHub", profile.github.ifBlank { "N/A" }, true)

                            setThumbnail(target.effectiveAvatarUrl)

                            setImage("https://i.ibb.co/B9pQgyS/id125-ccs-logo-banner.png")

                            setFooter(
                                "id125.ccs • 2025",
                                target.jda.selfUser.effectiveAvatarUrl
                            )
                        }.build()
                    )
                }

            }
        }

    }
}