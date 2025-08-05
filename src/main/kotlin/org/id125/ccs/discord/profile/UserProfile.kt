package org.id125.ccs.discord.profile

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val discordId: Long,
    val email: String,
    val batchId: String,
    val college: College,
    val degreeProgram: DegreeProgram?,
    val campus: Campus,
    val github: String
)