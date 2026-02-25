package org.id125.ccs.discord.configuration

import kotlinx.serialization.Serializable
import org.id125.ccs.discord.configuration.dto.MessageEmbedDTO
import org.id125.ccs.discord.profile.College

@Serializable
data class MainConfiguration(
    val serversConfiguration: List<ServerConfiguration> = listOf(),
)

@Serializable
data class ServerConfiguration(
    val serverId: Long = 0,
    val dependency: Long = 0,
    val verifiedRoleId: Long = 0,
    val visitorRoleId: Long = 0,
    val collegeRoles: Map<College, Long> = mapOf(),
    val panelMessage: MessageEmbedDTO,
    val welcomeMessage: MessageEmbedDTO
)