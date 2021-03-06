//
//  PhoneState.h
//  PhoneState
//
//  Created by Fredrik Andersson on 2014-08-28.
//  Modified by Andreas Idehaag on 2014-08-29.
//  Modified by Fredrik Andersson on 2015-11-12.
//  Copyright (c) 2014 Fredrik Andersson. All rights reserved.
//

#import <Cordova/CDV.h>

@interface PhoneState : CDVPlugin {
	NSString* _callbackId;
}

	
- (BOOL)isOnPhone;
- (void)start:(CDVInvokedUrlCommand*)command;
- (void)thredEntryPoint:(id)paramSender;
- (void)stop:(CDVInvokedUrlCommand*)command;
- (void)dealloc;
	
@end
