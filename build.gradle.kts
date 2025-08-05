plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.8"
}

group = "org.id125.ccs.discord"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // DISCORD
    implementation("net.dv8tion:JDA:5.6.1")
    implementation("com.github.andeng07.Promptlin:promptlin-discord-jda:63e0e72ca5")
    implementation("com.github.andeng07:DiscordCommand:d987a7ac22")

    // EMAIL SUPPORT
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // SERIALIZATION
    implementation("com.charleskorn.kaml:kaml:0.85.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // MONGO DB
    implementation(platform("org.mongodb:mongodb-driver-bom:5.5.1"))
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine")
    implementation("org.mongodb:bson-kotlinx")
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.id125.ccs.discord.DiscordBotKt"
    }
}

tasks.shadowJar {
    minimize()
}