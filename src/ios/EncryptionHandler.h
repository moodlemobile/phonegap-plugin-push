//
//  EncryptionHandler.h
//  Moodle
//
//  Created by Alex Morris on 18/01/23.
//

#import <Foundation/Foundation.h>

@interface EncryptionHandler : NSObject

+ (NSString*) decrypt: (NSString*) ciphertext;
+ (NSString*) getPublicKey;

@end
