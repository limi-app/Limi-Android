package personal.limi.logic.rules.cloud

import org.kotlincrypto.hash.sha2.SHA256

fun contentHash(input: String): String {
    val digest = SHA256()
    return digest.digest(input.encodeToByteArray()).joinToString("") {
        (it.toInt() and 0xFF).toString(16).padStart(2, '0')
    }
}
