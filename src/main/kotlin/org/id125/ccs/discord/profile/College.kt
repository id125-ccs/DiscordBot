package org.id125.ccs.discord.profile

import kotlinx.serialization.Serializable

@Serializable
enum class College(val abbreviation: String, val displayName: String, val emoji: String) {
    CCS("CCS", "College of Computer Studies", "💻"),
    CED("CED", "Br. Andrew Gonzalez College of Education", "📚"),
    CLA("CLA", "College of Liberal Arts", "🎭"),
    COB("COB", "Ramon V. del Rosario College of Business", "💼"),
    COE("COE", "Gokongwei College of Engineering", "🛠️"),
    COS("COS", "College of Science", "🔬"),
    SOE("SOE", "School of Economics", "📈");
}