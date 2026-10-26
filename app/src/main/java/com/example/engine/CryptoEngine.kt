package com.example.engine

import android.util.Base64
import com.example.model.HashIdentification
import com.example.model.HashResult
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object CryptoEngine {

    fun hashAll(input: String): HashResult {
        return HashResult(
            input = input,
            md5 = hash("MD5", input),
            sha1 = hash("SHA-1", input),
            sha256 = hash("SHA-256", input),
            sha512 = hash("SHA-512", input)
        )
    }

    fun hash(algorithm: String, input: String): String {
        return try {
            val md = MessageDigest.getInstance(algorithm)
            val bytes = md.digest(input.toByteArray(StandardCharsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun encodeBase64(input: String): String {
        return Base64.encodeToString(input.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    }

    fun decodeBase64(input: String): String {
        return try {
            val decodedBytes = Base64.decode(input.trim(), Base64.DEFAULT)
            String(decodedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "Error: Invalid Base64 string (${e.message})"
        }
    }

    fun encodeHex(input: String): String {
        return input.toByteArray(StandardCharsets.UTF_8).joinToString("") { "%02X".format(it) }
    }

    fun decodeHex(input: String): String {
        return try {
            val clean = input.replace("\\s".toRegex(), "").replace("0x", "")
            val bytes = ByteArray(clean.length / 2)
            for (i in clean.indices step 2) {
                bytes[i / 2] = clean.substring(i, i + 2).toInt(16).toByte()
            }
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "Error: Invalid Hex string (${e.message})"
        }
    }

    fun encodeUrl(input: String): String {
        return URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
    }

    fun decodeUrl(input: String): String {
        return try {
            URLDecoder.decode(input, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            "Error: Invalid URL encoded string"
        }
    }

    fun rot13(input: String): String {
        val result = StringBuilder()
        for (c in input) {
            when (c) {
                in 'a'..'m' -> result.append(c + 13)
                in 'n'..'z' -> result.append(c - 13)
                in 'A'..'M' -> result.append(c + 13)
                in 'N'..'Z' -> result.append(c - 13)
                else -> result.append(c)
            }
        }
        return result.toString()
    }

    fun identifyHash(hashString: String): HashIdentification {
        val trimmed = hashString.trim()
        val len = trimmed.length
        val isHex = trimmed.matches(Regex("^[a-fA-F0-9]+$"))
        val algorithms = mutableListOf<String>()

        if (trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$")) {
            algorithms.add("Bcrypt (Blowfish-based)")
        } else if (trimmed.startsWith("$6$")) {
            algorithms.add("SHA-512 Crypt (Unix)")
        } else if (trimmed.startsWith("$5$")) {
            algorithms.add("SHA-256 Crypt (Unix)")
        } else if (trimmed.startsWith("$1$")) {
            algorithms.add("MD5 Crypt (Unix)")
        } else if (trimmed.count { it == '.' } == 2) {
            algorithms.add("JSON Web Token (JWT)")
        } else if (isHex) {
            when (len) {
                32 -> {
                    algorithms.add("MD5")
                    algorithms.add("NTLM")
                    algorithms.add("MD4")
                    algorithms.add("LM Hash")
                }
                40 -> {
                    algorithms.add("SHA-1")
                    algorithms.add("RIPEMD-160")
                    algorithms.add("MySQL 4.1+")
                }
                56 -> {
                    algorithms.add("SHA-224")
                    algorithms.add("SHA3-224")
                }
                64 -> {
                    algorithms.add("SHA-256")
                    algorithms.add("Keccak-256")
                    algorithms.add("BLAKE2s")
                }
                96 -> {
                    algorithms.add("SHA-384")
                    algorithms.add("SHA3-384")
                }
                128 -> {
                    algorithms.add("SHA-512")
                    algorithms.add("Whirlpool")
                    algorithms.add("BLAKE2b")
                }
                else -> algorithms.add("Unknown Hex string of length $len")
            }
        } else {
            // Check Base64
            if (trimmed.matches(Regex("^[a-zA-Z0-9+/=]+$"))) {
                algorithms.add("Base64 Encoded Stream / HMAC")
            } else {
                algorithms.add("Unrecognized format")
            }
        }

        return HashIdentification(
            hash = trimmed,
            probableAlgorithms = algorithms,
            bitLength = len * 4
        )
    }

    fun decodeJwt(jwt: String): String {
        return try {
            val parts = jwt.trim().split(".")
            if (parts.size < 2) return "Invalid JWT format. Must contain at least header and payload."
            val header = decodeBase64Url(parts[0])
            val payload = decodeBase64Url(parts[1])
            val signature = if (parts.size > 2) parts[2] else "[none]"
            """
            |--- [JWT HEADER] ---
            |$header
            |
            |--- [JWT PAYLOAD] ---
            |$payload
            |
            |--- [SIGNATURE] ---
            |$signature
            """.trimMargin()
        } catch (e: Exception) {
            "Failed to decode JWT: ${e.message}"
        }
    }

    private fun decodeBase64Url(input: String): String {
        var base64 = input.replace('-', '+').replace('_', '/')
        while (base64.length % 4 != 0) {
            base64 += "="
        }
        val decoded = Base64.decode(base64, Base64.DEFAULT)
        return String(decoded, StandardCharsets.UTF_8)
    }
}
