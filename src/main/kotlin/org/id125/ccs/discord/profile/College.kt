package org.id125.ccs.discord.profile

import kotlinx.serialization.Serializable

@Serializable
enum class College(val abbreviation: String, val displayName: String, val emoji: String) {
    CCS("CCS", "College of Computer Studies", "💻"),
    COB("COB", "Ramon V. del Rosario College of Business", "💼"),
    COE("COE", "Gokongwei College of Engineering", "🛠️"),
    CLA("CLA", "College of Liberal Arts", "🎭"),
    COS("COS", "College of Science", "🔬"),
    CED("CED", "Br. Andrew Gonzalez College of Education", "📚"),
    SOE("SOE", "School of Economics", "📈");
}