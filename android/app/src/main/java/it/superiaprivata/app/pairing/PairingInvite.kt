package it.superiaprivata.app.pairing

import android.util.Base64
import org.json.JSONObject
import java.security.SecureRandom

/**
 * Short-lived QR payload. It advertises identity only; it does not grant trust.
 * The receiver must complete a signed challenge/confirmation exchange separately.
 */
data class PairingInvite(
    val protocolVersion: Int,
    val deviceLabel: String,
    val publicKey: String,
    val nonce: String,
    val expiresAtEpochMs: Long
) {
    fun isExpired(nowEpochMs: Long = System.currentTimeMillis()): Boolean =
        nowEpochMs >= expiresAtEpochMs

    fun toQrPayload(): String = JSONObject()
        .put("protocol", protocolVersion)
        .put("label", deviceLabel)
        .put("publicKey", publicKey)
        .put("nonce", nonce)
        .put("expiresAt", expiresAtEpochMs)
        .toString()

    companion object {
        const val CURRENT_PROTOCOL_VERSION = 1
        const val VALIDITY_WINDOW_MS = 5 * 60 * 1000L
        private const val NONCE_BYTES = 32

        fun create(
            deviceLabel: String,
            identityStore: DeviceIdentityStore,
            nowEpochMs: Long = System.currentTimeMillis()
        ): PairingInvite {
            require(deviceLabel.isNotBlank()) { "Il nome del dispositivo è obbligatorio" }
            val nonceBytes = ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }
            return PairingInvite(
                protocolVersion = CURRENT_PROTOCOL_VERSION,
                deviceLabel = deviceLabel.take(80),
                publicKey = identityStore.publicKeyBase64(),
                nonce = Base64.encodeToString(nonceBytes, Base64.NO_WRAP),
                expiresAtEpochMs = nowEpochMs + VALIDITY_WINDOW_MS
            )
        }

        fun parseQrPayload(payload: String): PairingInvite {
            require(payload.length <= 4096) { "Invito troppo grande" }
            val json = JSONObject(payload)
            val invite = PairingInvite(
                protocolVersion = json.getInt("protocol"),
                deviceLabel = json.getString("label"),
                publicKey = json.getString("publicKey"),
                nonce = json.getString("nonce"),
                expiresAtEpochMs = json.getLong("expiresAt")
            )
            require(invite.protocolVersion == CURRENT_PROTOCOL_VERSION) {
                "Versione del protocollo di collegamento non supportata"
            }
            require(invite.deviceLabel.isNotBlank() && invite.deviceLabel.length <= 80)
            require(invite.publicKey.length <= 2048 && invite.nonce.length <= 128)
            require(!invite.isExpired()) { "Questo invito è scaduto" }
            return invite
        }
    }
}
