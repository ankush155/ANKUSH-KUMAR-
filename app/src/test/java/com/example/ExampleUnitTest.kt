package com.example

import androidx.compose.ui.graphics.Color
import com.example.engine.CryptoEngine
import com.example.ui.terminal.AnsiColorParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSha256Hashing() {
        val hash = CryptoEngine.hash("SHA-256", "hello")
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", hash)
    }

    @Test
    fun testRot13() {
        val encrypted = CryptoEngine.rot13("Hello World")
        assertEquals("Uryyb Jbeyq", encrypted)
        val decrypted = CryptoEngine.rot13(encrypted)
        assertEquals("Hello World", decrypted)
    }

    @Test
    fun testHashIdentification() {
        val md5Hash = "5d41402abc4b2a76b9719d911017c592"
        val id = CryptoEngine.identifyHash(md5Hash)
        assertTrue(id.probableAlgorithms.contains("MD5"))
    }

    @Test
    fun testAnsiStripping() {
        val raw = "\u001B[1;32m[+] SUCCESS\u001B[0m: Port 80 open"
        val stripped = AnsiColorParser.stripAnsi(raw)
        assertEquals("[+] SUCCESS: Port 80 open", stripped)
    }

    @Test
    fun testAnsiParsingStyles() {
        val raw = "\u001B[1;31mERROR\u001B[0m plain"
        val annotated = AnsiColorParser.parse(raw, Color.White)
        assertEquals("ERROR plain", annotated.text)
        assertTrue(annotated.spanStyles.isNotEmpty())
    }
}
