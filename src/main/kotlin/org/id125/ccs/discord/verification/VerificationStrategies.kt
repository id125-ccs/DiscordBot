package org.id125.ccs.discord.verification

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.id125.ccs.discord.AppContext
import org.id125.ccs.discord.profile.Campus
import org.id125.ccs.discord.profile.College
import org.id125.ccs.discord.profile.UserProfile
import org.id125.ccs.discord.utility.softThrow

interface VerificationStrategy {
    fun verify(context: VerificationContext)
}

data class VerificationContext(
    val channel: MessageChannel,
    val executor: Member,

    val name: String,
    val university: String? = null,
    val batchId: String? = null,
    val campus: Campus? = null,
    val college: College? = null,
    val degreeProgram: String? = null,
    val email: String? = null
)

object ConsentDisagreeStrategy : VerificationStrategy {
    override fun verify(context: VerificationContext) {
        context.channel.sendMessage("You have chosen to **disagree** with the consent. Verification cannot proceed. Please restart.")
            .queue()
    }
}

object VisitorStrategy : VerificationStrategy {
    override fun verify(context: VerificationContext) {
        val executor = context.executor

        if (!executor.guild.selfMember.canInteract(executor)) return

        val roleId =
            AppContext.mainConfiguration.serversConfiguration.firstOrNull { it.serverId == executor.guild.idLong }?.visitorRoleId
                ?: return

//        executor.modifyNickname("[Visitor] ${context.name}").queue()

        executor.guild.getRoleById(roleId)?.let { role ->
            executor.guild.addRoleToMember(executor, role).queue()
        }
            ?: softThrow(NullPointerException("No visitor role with id $roleId found in guild withId ${executor.guild.idLong}"))
    }
}

object DefaultStrategy : VerificationStrategy {
    private val userProfileRepository = AppContext.userProfileRepository

    override fun verify(context: VerificationContext) {
        val executor = context.executor

        val serverConfiguration =
            AppContext.mainConfiguration.serversConfiguration.firstOrNull { it.serverId == executor.guild.idLong }
                ?: return

        val userProfile = with(context) {
            UserProfile(
                executor.idLong,
                email!!, batchId!!, college!!, degreeProgram!!, campus!!
            )
        }

        AppContext.coroutineScope.launch(Dispatchers.IO) {
            userProfileRepository.insert(userProfile)
        }

        val roleId = serverConfiguration.verifiedRoleId

        if (executor.guild.selfMember.canInteract(executor)) {
            executor.modifyNickname("${context.name} | [${context.degreeProgram}] [${context.batchId}]").queue()

            executor.guild.getRoleById(roleId)?.let { role ->
                executor.guild.addRoleToMember(executor, role).queue()
            }
                ?: softThrow(NullPointerException("No verified role with id $roleId found in guild withId ${executor.guild.idLong}"))
        }

        context.channel.sendMessageEmbeds(serverConfiguration.welcomeMessage.toJDA()).queue()
    }
}