package org.id125.ccs.discord.command.moderation

import me.centauri07.dc.api.argument.Argument
import me.centauri07.dc.api.executor.Executor
import me.centauri07.dc.api.response.Response
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.Event
import org.id125.ccs.discord.AppContext
import java.util.concurrent.TimeUnit

val purgeCommand by lazy {
    AppContext.discordCommandManager.createSlashCommand("purgeroles", "Purge all roles of all discord members") {
        permissions {
            +Permission.ADMINISTRATOR
        }

        executor = object : Executor {
            override fun onCommand(
                executor: Member,
                arguments: List<Argument>,
                event: Event
            ): Response {
                val guild = executor.guild
                val selfMember = guild.selfMember

                guild.loadMembers().onSuccess { members ->
                    var delay = 0L

                    for (member in members) {
                        // Skip bots and members the bot cannot interact with
                        if (member.user.isBot || !selfMember.canInteract(member) || member.roles.isEmpty()) continue

                        guild.modifyMemberRoles(member, emptyList())
                            .queueAfter(delay, TimeUnit.MILLISECONDS,
                                { println("✅ Removed roles from ${member.user.asTag}") },
                                { println("❌ Failed for ${member.user.asTag}: ${it.message}") }
                            )

                        delay += 250
                    }

                    println("Scheduled ${members.size} members for role removal.")
                }

                return Response.of("Removing roles from all users.").setEphemeral()
            }

        }
    }
}