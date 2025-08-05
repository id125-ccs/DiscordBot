package org.id125.ccs.discord.command.verification

import kotlinx.coroutines.runBlocking
import me.centauri07.dc.api.argument.Argument
import me.centauri07.dc.api.executor.Executor
import me.centauri07.dc.api.response.Response
import me.centauri07.promptlin.core.form.FormSessionRegistry
import me.centauri07.promptlin.discord.prompt.choice.ButtonOption
import me.centauri07.promptlin.discord.prompt.choice.SelectOption
import me.centauri07.promptlin.jda.JDAContext
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.id125.ccs.discord.AppContext
import org.id125.ccs.discord.profile.Campus
import org.id125.ccs.discord.profile.College
import org.id125.ccs.discord.profile.DegreeProgram
import org.id125.ccs.discord.profile.UserProfile
import org.id125.ccs.discord.verification.verificationForm

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

                    runBlocking {
                        AppContext.userProfileRepository.insert(userProfile)
                    }

                    channel.sendMessage("You are now verified. Enjoy your stay!").queue()

                    if (!executor.guild.selfMember.canInteract(executor)) return@start

                    if (executor.guild.idLong == AppContext.mainConfiguration.serverId) {
                        val role = executor.guild.getRoleById(AppContext.mainConfiguration.verifiedRoleId) ?: return@start

                        executor.guild.addRoleToMember(executor, role).queue()
                    }

                    executor.modifyNickname("[$batchId] $name | ${college.abbreviation}" + if (department != null) " | ${department.code}" else "").queue()
                }

                return Response.of("Verification process has began. Please check your DMs. ${channel.asMention}")
                    .setEphemeral()
            }
        }
    }

}