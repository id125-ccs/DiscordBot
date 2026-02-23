package org.id125.ccs.discord.verification

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import me.centauri07.promptlin.core.form.FormSession

@OptIn(ExperimentalTime::class)
val startTime = mutableMapOf<FormSession<*>, Instant>()

@OptIn(ExperimentalTime::class)
var FormSession<*>.timeStarted: Instant
    get() = startTime[this] ?: Clock.System.now()
    set(value) {
        startTime[this] = value
    }