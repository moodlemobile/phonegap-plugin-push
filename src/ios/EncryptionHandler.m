//
//  EncryptionHandler.m
//  Moodle
//
//  Created by Alex Morris on 18/01/23.
//

#import "EncryptionHandler.h"
#import "Moodle-Swift.h"

@implementation EncryptionHandler

+ (NSString*) decrypt:(NSString *)ciphertext {
    return [Crypto decryptWithBase64CipherText:ciphertext];
}

+ (NSString*) getPublicKey {
    if (![Crypto keyExists]) {
        [Crypto generateKeypair];
    }

    return [Crypto getPublicKey];
}

@end
