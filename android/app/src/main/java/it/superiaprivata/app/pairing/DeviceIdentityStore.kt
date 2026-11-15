package it.superiaprivata.app.pairing

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory

/**
 * Creates an installation-scoped signing identity backed by Android Keystore.
 * The private key is non-exportable; only the public key is shared during pairing.
 */
class DeviceIdentityStore(
    private val alias: String = DEFAULT_ALIAS
) {
    fun publicKeyBase64(): String {
        val keyStore = loadKeyStore()
        ensureKeyPair(keyStore)
        val certificate = keyStore.getCertificate(alias)
            ?: error("Certificato della chiave dispositivo non disponibile")
        return Base64.encodeToString(certificate.publicKey.encoded, Base64.NO_WRAP)
    }

    fun signChallenge(challenge: ByteArray): ByteArray {
        require(challenge.isNotEmpty()) { "La challenge non può essere vuota" }
        val keyStore = loadKeyStore()
        ensureKeyPair(keyStore)
        val privateKey = keyStore.getKey(alias, null) as? java.security.PrivateKey
            ?: error("Chiave privata del dispositivo non disponibile")
        return Signature.getInstance(SIGNATURE_ALGORITHM).run {
            initSign(privateKey)
            update(challenge)
            sign()
        }
    }

    private fun ensureKeyPair(keyStore: KeyStore) {
        if (keyStore.containsAlias(alias)) return
        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )
        generator.initialize(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()
        )
        generator.generateKeyPair()
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    companion object {
        const val DEFAULT_ALIAS = "superia_device_identity_v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"

        fun verifyChallenge(
            publicKeyBase64: String,
            challenge: ByteArray,
            signatureBytes: ByteArray
        ): Boolean {
            return runCatching {
                val encodedKey = Base64.decode(publicKeyBase64, Base64.NO_WRAP)
                val publicKey = KeyFactory.getInstance("EC")
                    .generatePublic(X509EncodedKeySpec(encodedKey))
                Signature.getInstance(SIGNATURE_ALGORITHM).run {
                    initVerify(publicKey)
                    update(challenge)
                    verify(signatureBytes)
                }
            }.getOrDefault(false)
        }
    }
}
