package org.id125.ccs.discord.utility.callback

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

private fun <K, E: Event> callback(keyRetriever: E.() -> K?): Callback<K, E> = Callback(keyRetriever)

class Callback<K, E : Event>(
    private val keyRetriever: E.() -> K?
) {
    companion object {
        val MODAL = callback<String, ModalInteractionEvent> {
            member?.let {
                "callback:verification:name:g_${it.guild.idLong}:u_${it.idLong}"
            }
        }
    }

    private val callbacks = mutableMapOf<K, E.() -> Unit>()

    fun register(key: K, callback: E.() -> Unit) {
        callbacks[key] = callback
    }

    fun handle(event: E) {
        val key = event.keyRetriever() ?: return
        val callback = callbacks.remove(key) ?: return
        callback(event)
    }
}