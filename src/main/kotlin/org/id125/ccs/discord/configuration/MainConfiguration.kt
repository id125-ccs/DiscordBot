package org.id125.ccs.discord.configuration

import kotlinx.serialization.Serializable

@Serializable
data class MainConfiguration(
    val serverId: Long = 0,
    val verifiedRoleId: Long = 0,
    val visitorRoleId: Long = 0
)
