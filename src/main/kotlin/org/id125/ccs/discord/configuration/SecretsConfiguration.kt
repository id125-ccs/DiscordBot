package org.id125.ccs.discord.configuration

import kotlinx.serialization.Serializable

@Serializable
data class SecretsConfiguration(
    val discordToken: String = "DISCORD_TOKEN",

    val emailAddress: String = "EMAIL_ADDRESS",
    val emailPassword: String = "EMAIL_PASSWORD",

    val mongodbConnectionString: String = "MONGODB_CONNECTION_STRING",

    val hashKey: String = "HASH_KEY"
)