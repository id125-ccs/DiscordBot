package org.id125.ccs.discord.utility

fun softThrow(exception: Exception, onCatch: ((Exception) -> Unit)? = null) {
    try {
        throw exception
    } catch (e: Exception) {
        onCatch?.invoke(e)
    }
}

fun softThrow(exception: Exception) = softThrow(exception) { println(it) }