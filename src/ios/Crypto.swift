//
//  Crypto.swift
//  Moodle
//
//  Created by Alex Morris on 13/04/23.
//

import Foundation
import CryptoKit
import Security
import Sodium

@objc class Crypto: NSObject {
    static var PRIVATE_TAG = (Bundle.main.infoDictionary?[kCFBundleNameKey as String] as! String) + ".privatekey"
    static var PUBLIC_TAG = (Bundle.main.infoDictionary?[kCFBundleNameKey as String] as! String) + ".publickey"
    
    @objc static func generateKeypair() -> Bool {
        if #available(iOS 13, *) {
            let sodium = Sodium()
            let keypair = sodium.box.keyPair()

            
            let privateQuery: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrAccount as String: PRIVATE_TAG,
                kSecValueData as String: Data.init(bytes: keypair!.secretKey),
                kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            ]
            let privateStatus = SecItemAdd(privateQuery as CFDictionary, nil)
            
            let publicQuery: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrAccount as String: PUBLIC_TAG,
                kSecValueData as String: Data.init(bytes: keypair!.publicKey),
                kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            ]
            let publicStatus = SecItemAdd(publicQuery as CFDictionary, nil)
            
            if privateStatus == errSecSuccess && publicStatus == errSecSuccess {
                return true
            }

            return false
        }
        return false
    }
    
    @objc static func keyExists() -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: PRIVATE_TAG,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        
        if status == errSecSuccess {
            return true
        }
        return false
    }
    
    @objc static func getPublicKey() -> String? {
        if #available(iOS 13, *) {
            let query: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrAccount as String: PUBLIC_TAG,
                kSecReturnData as String: true,
                kSecMatchLimit as String: kSecMatchLimitOne
            ]
            
            var item: CFTypeRef?
            let status = SecItemCopyMatching(query as CFDictionary, &item)
            
            if status == errSecSuccess {
                guard let publicKeyData = item as? Data else {
                    return nil
                }
                let sodium = Sodium()
                
                var values = [UInt8](repeating:0, count:publicKeyData.count)
                publicKeyData.copyBytes(to: &values, count: publicKeyData.count)
                
                return sodium.utils.bin2base64(values, variant: Utils.Base64Variant.ORIGINAL)
            }
            return nil
        }
        return nil
    }
    
    @objc static func decrypt(base64CipherText: String) -> String? {
        if #available(iOS 13, *) {
            let privateQuery: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrAccount as String: PRIVATE_TAG,
                kSecReturnData as String: true,
                kSecMatchLimit as String: kSecMatchLimitOne
            ]
            let publicQuery: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrAccount as String: PUBLIC_TAG,
                kSecReturnData as String: true,
                kSecMatchLimit as String: kSecMatchLimitOne
            ]
            
            var privateItem: CFTypeRef?
            var publicItem: CFTypeRef?
            let privateStatus = SecItemCopyMatching(privateQuery as CFDictionary, &privateItem)
            let publicStatus = SecItemCopyMatching(publicQuery as CFDictionary, &publicItem)
            
            if privateStatus == errSecSuccess && publicStatus == errSecSuccess {
                guard let privateKeyData = privateItem as? Data else {
                    return nil
                }
                guard let publicKeyData = publicItem as? Data else {
                    return nil
                }

                let sodium = Sodium()
                let ciphertext = sodium.utils.base642bin(base64CipherText, variant: Utils.Base64Variant.ORIGINAL)

                if (ciphertext == nil) {
                    return base64CipherText
                }

                var publicKeyBytes = [UInt8](repeating:0, count:publicKeyData.count)
                publicKeyData.copyBytes(to: &publicKeyBytes, count: publicKeyData.count)
                var privateKeyBytes = [UInt8](repeating:0, count:privateKeyData.count)
                privateKeyData.copyBytes(to: &privateKeyBytes, count: privateKeyData.count)

                let data = sodium.box.open(anonymousCipherText: ciphertext!, recipientPublicKey: publicKeyBytes, recipientSecretKey: privateKeyBytes)

                if (data == nil) {
                    return base64CipherText
                }

                return String(bytes: data!, encoding: .utf8)
            }
        }
        return nil
    }
}
