package org.id125.ccs.discord.profile

import kotlinx.serialization.Serializable

@Serializable
enum class DegreeProgram(
    val id: String,
    val code: String,
    val displayName: String,
    val emoji: String,
    val campus: Campus
) {
    // Computer Science (Manila)
    CS_ST("cs-st", "CS-ST", "Computer Science Major in Software Technology", "💻", Campus.MANILA),
    CS_CSE("cs-cse", "CS-CSE", "Computer Science Major in Computer Systems Engineering", "💻", Campus.MANILA),
    CS_NIS("cs-nis", "CS-NIS", "Computer Science Major in Network and Information Security", "💻", Campus.MANILA),
    BSMS_CS("bsms-cs", "BSMS-CS", "Computer Science and Master of Science in Computer Science", "💻", Campus.MANILA),

    // Information Systems / Technology (Manila)
    IS("is", "IS", "Information Systems", "\uD83C\uDF10", Campus.MANILA),
    IT("it", "IT", "Information Technology", "\uD83C\uDF10", Campus.MANILA),

    // Computer Science (Laguna)
    CSS("css", "CSS", "Computer Science", "💻", Campus.LAGUNA),

    // Information Technology (Laguna)
    ITS("its", "ITS", "Information Technology", "💻", Campus.LAGUNA),

    // Interactive Entertainment (Laguna)
    IET_AD("iet-ad", "IE-AD", "Interactive Entertainment Major in Game Art in Design", "🎮", Campus.LAGUNA),
    IET_GD("iet-gd", "IE-GD", "Interactive Entertainment Major in Game Development", "🎮", Campus.LAGUNA)
}