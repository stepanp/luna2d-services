//-----------------------------------------------------------------------------
// luna2d ads service for AdMob
// This is part of luna2d engine
// Copyright 2014-2017 Stepan Prokofjev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.
//-----------------------------------------------------------------------------

#import <Foundation/Foundation.h>
#import "lunaiosadsprotocol.h"
#import "lunaiosservicesapi.h"

#ifdef ENABLE_DEBUG_LOG
#define DEBUG_LOG(...) NSLog(__VA_ARGS__);
#else
#define DEBUG_LOG(...)
#endif

@import GoogleMobileAds;

@interface AdMobMediation : NSObject <LUNAIosAdsProtocol, GADInterstitialDelegate, GADRewardBasedVideoAdDelegate>
{
	bool needShowInterstitial;
	bool wasRewardedVideoSuccess;
	bool isRewardedVideoShowing;
	bool isRewardedVideoCaching;
}

@property (copy) void (^setOnInterstitialClosedProp)();
@property (copy) void (^onRewardedVideoSuccessProp)();
@property (copy) void (^onRewardedVideoFailProp)();
@property (copy) void (^onRewardedVideoErrorProp)();

@property(nonatomic, strong) NSString* appId;
@property(nonatomic, strong) NSString* bannerId;
@property(nonatomic, strong) NSString* interstitialId;
@property(nonatomic, strong) NSString* rewardedVideoId;
@property(nonatomic, strong) NSArray* testDeviceIds;

@property(nonatomic, strong) GADInterstitial* interstitial;
@property(nonatomic, strong) GADBannerView *bannerView;

-(id) init;

-(UIViewController*) getViewController;

-(GADRequest*) makeRequest: (NSString*) location;

// Set callback calling when interstitial has been closed
-(void) setOnInterstitialClosed: (void (^)()) callback;

// Set callback calling when video has been succesfully viewed
-(void) setOnRewardedVideoSuccess: (void (^)()) callback;

// Set callback calling when video has been dismissed
-(void) setOnRewardedVideoFail: (void (^)()) callback;

// Set callback calling when video cause error
-(void) setOnRewardedVideoError: (void (^)()) callback;

// Is banner enabled
-(BOOL) isBannerEnabled;

// Get default banner height (in pixels)
-(int) getBannerHeight;

// Check for banner is shown
-(BOOL) isBannerShown;

// Show banner
-(void) showBanner: (NSString*) location;

// Hide banner
-(void) hideBanner;

// Is interstitial enabled
-(BOOL) isInterstitialEnabled;

// Check for interstitial is downloaded ready to showing
-(BOOL) isInterstitialReady;

// Cache interstitial
-(void) cacheInterstitial: (NSString*) location;

// Show interstitial
-(void) showInterstitial: (NSString*) location;

// Is rewarded video enabled
-(BOOL) isRewardedVideoEnabled;

// Check for rewarded video is downloaded ready to showing
-(BOOL) isRewardedVideoReady;

// Cache rewarded video
-(void) cacheRewardedVideo: (NSString*) location;

// Show rewarded video
-(void) showRewardedVideo: (NSString*) location;

@end
