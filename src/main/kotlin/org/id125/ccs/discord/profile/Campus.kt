package org.id125.ccs.discord.profile

import kotlinx.serialization.Serializable

@Serializable
enum class Campus(val id: String, val displayName: String, val emoji: String) {
    MANILA("manila", "Manila", "🏛️"),
    LAGUNA("laguna", "Laguna", "🌴"),
}