package org.id125.ccs.discord.utility

import org.id125.ccs.discord.AppContext
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Hashing {
    private val SECRET_KEY: ByteArray = Base64.getDecoder().decode(AppContext.secrets.hashKey)

    private val macThreadLocal = ThreadLocal.withInitial {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(SECRET_KEY, "HmacSHA256")
        mac.init(keySpec)
        mac
    }

    fun hash(input: String): String {
        val normalized = input.trim().lowercase()
        val mac = macThreadLocal.get()
        synchronized(mac) {
            mac.reset()
            val digest = mac.doFinal(normalized.toByteArray(StandardCharsets.UTF_8))
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
        }
    }
}