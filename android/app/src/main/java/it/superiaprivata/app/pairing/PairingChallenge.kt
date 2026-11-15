package it.superiaprivata.app.pairing

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/**
 * Helpers for the signed challenge stage of device pairing.
 *
 * A valid signature proves possession of the private key corresponding to a
 * supplied public key. The app must still obtain explicit user approval and
 * authenticate the public-key fingerprint out of band before trusting a peer.
 */
object PairingChallenge {
    const val CHALLENGE_BYTES = 32
    private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"

    fun encode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    fun decode(value: String): ByteArray {
        require(value.length <= 4096) { "Dato crittografico troppo grande" }
        return Base64.getUrlDecoder().decode(value)
    }

    fun fingerprint(publicKeyBase64: String): String {
        val encodedKey = Base64.getDecoder().decode(publicKeyBase64)
        val digest = MessageDigest.getInstance("SHA-256").digest(encodedKey)
        return digest.joinToString(":") { "%02X".format(it) }
    }

    fun verifySignature(
        publicKeyBase64: String,
        challenge: ByteArray,
        signatureBase64: String
    ): Boolean = runCatching {
        require(challenge.size == CHALLENGE_BYTES) { "Challenge non valida" }
        val keyBytes = Base64.getDecoder().decode(publicKeyBase64)
        val publicKey = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(keyBytes))
        val signatureBytes = decode(signatureBase64)
        Signature.getInstance(SIGNATURE_ALGORITHM).run {
            initVerify(publicKey)
            update(challenge)
            verify(signatureBytes)
        }
    }.getOrDefault(false)
}
