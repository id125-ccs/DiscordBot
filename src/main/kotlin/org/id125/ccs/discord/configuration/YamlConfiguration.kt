package org.id125.ccs.discord.configuration

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.KSerializer
import java.io.File

class YamlConfiguration<T>(
    private val path: String,
    private val name: String,
    private val defaultValue: T,
    private val serializer: KSerializer<T>
) {
    private val yaml = Yaml.default
    val file: File get() = File(path, "$name.yaml")

    var value: T = loadOrDefault()
        private set

    fun load(): T {
        value = loadOrDefault()

        if (!file.exists()) save()

        return value
    }

    fun save() {
        file.parentFile?.mkdirs()
        file.writeText(yaml.encodeToString(serializer, value))
    }

    private fun loadOrDefault(): T {
        return if (file.exists()) {
            yaml.decodeFromString(serializer, file.readText())
        } else {
            defaultValue
        }
    }
}
