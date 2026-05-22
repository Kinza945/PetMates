package com.kynzai.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import java.util.Base64

class OAuthPkceTest {
    @Test
    fun challenge_isSha256OfVerifier() {
        val verifier = OAuthPkce.generateVerifier()
        val expected = Base64.getUrlEncoder().withoutPadding().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)),
        )
        assertEquals(expected, OAuthPkce.challenge(verifier))
    }

    @Test
    fun generateVerifier_producesUniqueValues() {
        val a = OAuthPkce.generateVerifier()
        val b = OAuthPkce.generateVerifier()
        assertNotEquals(a, b)
        assertTrue(a.length >= 43)
    }
}
