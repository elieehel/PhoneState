//
//  PhoneState.m
//  PhoneState
//
//  Created by Fredrik Andersson on 2014-08-28.
//  Modified by Andreas Idehaag on 2014-08-29.
//  Modified by Fredrik Andersson on 2015-11-12.
//  Copyright (c) 2014 Fredrik Andersson. All rights reserved.
//

#import "PhoneState.h"
#import <Cordova/CDV.h>

#import <CoreTelephony/CTCallCenter.h>
#import <CoreTelephony/CTCall.h>

@implementation PhoneState


-(BOOL)isOnPhone
{
    //kollar om telefonen är i samtal
    //returnera Ja om så är och Nej i andra fall
    CTCallCenter *callCenter = [[CTCallCenter alloc] init];
    for(CTCall * call in callCenter.currentCalls)
    {
        if(call.callState == CTCallStateConnected)
        {
            NSLog(@"NU Är jag i samtal");
            return YES;
        }
    }
    NSLog(@"Jag är inte i samtal");
    return NO;
    
}

-(void) thredEntryPoint:(id)paramSender
{
    CDVPluginResult *pluginResult = nil;
    
    BOOL previousResult = NO;
    BOOL onPhone        = NO;
    
    while ([[NSThread currentThread] isCancelled] == NO)
    {
        [NSThread sleepForTimeInterval:1.0f];
        
        if([[NSThread currentThread] isCancelled] == NO)
        {
            //kolla om du är i samtal
            onPhone = self.isOnPhone;
            if(onPhone && !previousResult)
            {
                NSLog(@"Du är i samtal");
                previousResult = YES;
                pluginResult = [ CDVPluginResult
                                      resultWithStatus:CDVCommandStatus_OK
                                       messageAsString:@"busy"
                                    ];
								[pluginResult setKeepCallbackAsBool:YES];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
            }
            else if (!onPhone && previousResult)
            {
                NSLog(@"Du är inte i samtal");
                previousResult = NO;
                pluginResult = [ CDVPluginResult
                                      resultWithStatus:CDVCommandStatus_OK
                                       messageAsString:@"idle"
                                    ];
                [pluginResult setKeepCallbackAsBool:YES];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
                
            }
        }
    }
}

- (void)start:(CDVInvokedUrlCommand*)command {

		//[self.commandDelegate runInBackground:^{
    //    NSString* payload = nil;
        // Some blocking logic...
    //    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:payload];
        // The sendPluginResult method is thread-safe.
    //    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    //}];

		_callbackId = command.callbackId;

		//skapar bakgrundstråden
    [NSThread detachNewThreadSelector:@selector(thredEntryPoint:)
                             toTarget:[self appDelegate]
                           withObject:[self appDelegate]];
    
    
    CDVPluginResult *pluginResult = [ CDVPluginResult
                                      resultWithStatus:CDVCommandStatus_OK
                                       messageAsString:@"Thread start"
                                    ];
    
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
    
}

-(void)endTaskWithIdentifier:(NSNumber *)paramIdentifier
{
    //avsluta tråden
    UIBackgroundTaskIdentifier identifier = [paramIdentifier integerValue];
    [[UIApplication sharedApplication] endBackgroundTask:identifier];
    
}

- (void)dealloc
{
   // [self stop:nil];
}

- (void)stop:(CDVInvokedUrlCommand *)command
{
    // [self stop:nil];
}

- (void)onReset
{
   // [self stop:nil];
}

- (void)echo:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
}


@end
