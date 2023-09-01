package com.adobe.phonegap.push

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Base64MessageEncoder
import com.goterl.lazysodium.utils.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec

class EncryptionHandler {
    companion object {
        private const val TAG: String = "${PushPlugin.PREFIX_TAG} (EncryptionHandler)"
        private const val PRIVATE_KEY_PREF = "SODIUM_KEY_PRIVATE"
        private const val PUBLIC_KEY_PREF = "SODIUM_KEY_PUBLIC"
        private const val KEYSTORE_ALIAS: String = "moodlemobile"
        private const val KEYSTORE_NAME = "AndroidKeyStore"
        private const val CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        private const val DIGEST = "SHA-256"
        private val ALGO_SPEC = OAEPParameterSpec(
            DIGEST,
            "MGF1",
            MGF1ParameterSpec.SHA1,
            OAEPParameterSpec.DEFAULT.pSource
        )

        fun encryptionSupported(): Boolean {
            // Libsodium doesn't work in Android older than 8 because lazysodium-android uses java.util.Base64.
            // It should be fixed in lazysodium-android 5.1.1 version, then this should be changed to VERSION_CODES.M.
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
        }

        /**
         * Decrypt base64 encoded ciphertext using sodium private key.
         */
        fun decrypt(context: Context, base64EncodedText: String): String {
            if (!encryptionSupported()) {
                return base64EncodedText
            }

            try {
                // Try decode text now to check if it is encoded.
                // If it fails the string was not encoded and likely replaced with a language string
                // for being too long.
                Base64.decode(base64EncodedText, Base64.NO_WRAP)
            } catch (ex: IllegalArgumentException) {
                return base64EncodedText
            }
            // Retrieve keys from storage, reassemble keypair.
            val lazySodium = LazySodiumAndroid(SodiumAndroid(), Charsets.UTF_8, Base64MessageEncoder())
            val pref = context.getSharedPreferences(PushConstants.COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE)
            val b64PrivateKey = pref.getString(PRIVATE_KEY_PREF, null) ?: throw Exception()
            val publicKey = Key.fromBase64String(pref.getString(PUBLIC_KEY_PREF, null))
            val privateKey = decryptKey(b64PrivateKey)
            val keyPair = com.goterl.lazysodium.utils.KeyPair(publicKey, privateKey)

            return lazySodium.cryptoBoxSealOpenEasy(base64EncodedText, keyPair);
        }

        /**
         * Encrypt a text using sodium public key.
         */
        fun encrypt(context: Context, text: String): String {
            if (!encryptionSupported()) {
                return text
            }

            val lazySodium = LazySodiumAndroid(SodiumAndroid(), Charsets.UTF_8, Base64MessageEncoder())
            val publicKey = getPublicKey(context)

            return lazySodium.cryptoBoxSealEasy(text, Key.fromBase64String(publicKey));
        }

        /**
         * Decrypt ciphertext using the RSA key from the keystore and transform into sodium key.
         */
        private fun decryptKey(cipherText: String): Key {
            val keyStore = KeyStore.getInstance(KEYSTORE_NAME)
            keyStore.load(null)

            val entry: KeyStore.PrivateKeyEntry =
                keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.PrivateKeyEntry

            val cipher = Cipher.getInstance(CIPHER)
            cipher.init(Cipher.DECRYPT_MODE, entry.privateKey, ALGO_SPEC)

            val plaintext = cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP))

            return Key.fromBytes(plaintext)
        }

        /**
         * Encrypt sodium key using RSA key from keystore.
         */
        private fun encryptKey(key: Key): String? {
            if (!encryptionSupported()) {
                return null
            }

            val keyStore = KeyStore.getInstance(KEYSTORE_NAME)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                // Generate key if none exists.
                val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA", KEYSTORE_NAME)
                keyPairGenerator
                    .apply {
                        initialize(
                            KeyGenParameterSpec.Builder(
                                KEYSTORE_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                            ).run {
                                setKeySize(2048)
                                setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                                setDigests(
                                    KeyProperties.DIGEST_SHA256
                                )
                                build()
                            }
                        )
                    }
                keyPairGenerator.generateKeyPair()

                keyStore.load(null)
            }

            val keypair: KeyStore.PrivateKeyEntry =
                keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.PrivateKeyEntry

            val cipher = Cipher.getInstance(CIPHER)
            cipher.init(Cipher.ENCRYPT_MODE, keypair.certificate.publicKey, ALGO_SPEC)
            val ciphertext = cipher.doFinal(key.asBytes)
            return String(Base64.encode(ciphertext, Base64.NO_WRAP), Charsets.UTF_8)
        }

        /**
         * Return sodium public key as a hex string.
         * Creates the key pair if it doesn't exist.
         */
        fun getPublicKey(context: Context): String? {
            if (!encryptionSupported()) {
                return null
            }

            val lazySodium = LazySodiumAndroid(SodiumAndroid())
            val pref = context.getSharedPreferences(PushConstants.COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE)
            val base64PubKey = pref.getString(PUBLIC_KEY_PREF, null)
            val keypair: com.goterl.lazysodium.utils.KeyPair
            return if (base64PubKey == null) {
                keypair = lazySodium.cryptoBoxKeypair()
                pref.edit().putString(PRIVATE_KEY_PREF, encryptKey(keypair.secretKey)).apply()
                val base64Key = String(Base64.encode(keypair.publicKey.asBytes, Base64.NO_WRAP))
                pref.edit().putString(PUBLIC_KEY_PREF, base64Key).apply()
                base64Key
            } else {
                base64PubKey
            }
        }
    }

}
