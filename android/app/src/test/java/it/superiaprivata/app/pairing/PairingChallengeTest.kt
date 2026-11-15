package it.superiaprivata.app.pairing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64

class PairingChallengeTest {
    @Test
    fun verifiesValidSignatureAndRejectsTamperedChallenge() {
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()
        val challenge = ByteArray(PairingChallenge.CHALLENGE_BYTES) { it.toByte() }
        val signer = Signature.getInstance("SHA256withECDSA").apply {
            initSign(keyPair.private)
            update(challenge)
        }
        val signature = signer.sign()
        val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val signatureBase64 = PairingChallenge.encode(signature)

        assertTrue(PairingChallenge.verifySignature(publicKey, challenge, signatureBase64))
        val changedChallenge = challenge.copyOf().also { it[0] = (it[0] + 1).toByte() }
        assertFalse(PairingChallenge.verifySignature(publicKey, changedChallenge, signatureBase64))
    }

    @Test
    fun fingerprintIsStableForSamePublicKey() {
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(256)
        }.generateKeyPair()
        val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)

        assertEquals(
            PairingChallenge.fingerprint(publicKey),
            PairingChallenge.fingerprint(publicKey)
        )
        assertEquals(95, PairingChallenge.fingerprint(publicKey).length)
    }
}
