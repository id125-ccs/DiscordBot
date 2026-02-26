package org.id125.ccs.discord.configuration.dto

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant

@Serializable
data class MessageEmbedDTO(
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val color: Int? = null, // RGB int
    val timestamp: String? = null,

    val author: AuthorDTO? = null,
    val footer: FooterDTO? = null,
    val thumbnail: String? = null,
    val image: String? = null,

    val fields: List<FieldDTO> = emptyList()
) {
    fun toJDA(): MessageEmbed {
        val builder = EmbedBuilder()

        title?.let { builder.setTitle(it, url) }
        description?.let(builder::setDescription)
        color?.let(builder::setColor)

        timestamp?.let {
            builder.setTimestamp(Instant.parse(it))
        }

        author?.let {
            builder.setAuthor(it.name, it.url, it.iconUrl)
        }

        footer?.let {
            builder.setFooter(it.text, it.iconUrl)
        }

        thumbnail?.let(builder::setThumbnail)
        image?.let(builder::setImage)

        fields.forEach {
            builder.addField(it.name, it.value, it.inline)
        }

        return builder.build()
    }
}

@Serializable
data class FieldDTO(
    val name: String,
    val value: String,
    val inline: Boolean = false
)

@Serializable
data class AuthorDTO(
    val name: String,
    val url: String? = null,
    val iconUrl: String? = null
)

@Serializable
data class FooterDTO(
    val text: String,
    val iconUrl: String? = null
)
