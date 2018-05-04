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

#import "AdMobMediation.h"

@implementation AdMobMediation

-(id) init
{
	self = [super init];
	
	needShowInterstitial = false;
	wasRewardedVideoSuccess = false;
	isRewardedVideoShowing = false;
	isRewardedVideoCaching = false;
	isBannerLoading = false;
	isBannerLoaded = false;
	
	self.appId = [LUNAIosServicesApi getConfigString:@"adMobAppId"];
	self.bannerId = [LUNAIosServicesApi getConfigString:@"adMobBannerId"];
	self.interstitialId = [LUNAIosServicesApi getConfigString:@"adMobInterstitialId"];
	self.rewardedVideoId = [LUNAIosServicesApi getConfigString:@"adMobRewardedVideoId"];
	self.testDeviceIds = [LUNAIosServicesApi getConfigArray:@"adMobTestDeviceIds"];
	
	// Initialize Google Mobile Ads SDK
	[GADMobileAds configureWithApplicationID: self.appId];
	
	DEBUG_LOG(@"AdMobMediation init");
	
	return self;
}

-(UIViewController*) getViewController
{
	return [[[[UIApplication sharedApplication] delegate] window] rootViewController];
}

-(GADRequest*) makeRequest: (NSString*) location
{
	GADRequest* request = [GADRequest request];
	
	if(self.testDeviceIds.count != 0) request.testDevices = self.testDeviceIds;	
	return request;
}

// Set callback calling when interstitial has been closed
-(void) setOnInterstitialClosed: (void (^)()) callback
{
	self.setOnInterstitialClosedProp = callback;
}

// Set callback calling when video has been succesfully viewed
-(void) setOnRewardedVideoSuccess: (void (^)()) callback
{
	self.onRewardedVideoSuccessProp = callback;
}

// Set callback calling when video has been dismissed or in case of error
-(void) setOnRewardedVideoFail: (void (^)()) callback
{
	self.onRewardedVideoFailProp = callback;
}

// Set callback calling when video cause error
-(void) setOnRewardedVideoError: (void (^)()) callback
{
	self.onRewardedVideoErrorProp = callback;
}


//-------
// Banner
//-------

// Is banner enabled
-(BOOL) isBannerEnabled
{
	return [self.bannerId length] > 0;
}

// Get default banner height (in pixels)
-(int) getBannerHeight
{
	float screenScale = [[[UIDevice currentDevice] systemVersion] floatValue] < 8.0 ? [[UIScreen mainScreen] scale] : [[UIScreen mainScreen] nativeScale];
	
	return CGSizeFromGADAdSize(kGADAdSizeSmartBannerPortrait).height * screenScale;
}

// Check for banner is shown
-(BOOL) isBannerShown
{
	return isBannerLoaded;
}

// Show banner
-(void) showBanner: (NSString*) location
{
	if(![self isBannerEnabled] || isBannerLoading || isBannerLoaded) return;
	
	CGFloat offsetY = 0.0;
	
	if(@available(iOS 11.0, *))
	{
		UIWindow *mainWindow = [[[UIApplication sharedApplication] delegate] window];
		offsetY = mainWindow.safeAreaInsets.bottom;
	}
	
	CGPoint origin = CGPointMake(0.0, [self getViewController].view.frame.size.height - offsetY - CGSizeFromGADAdSize(kGADAdSizeSmartBannerPortrait).height);

	self.bannerView = [[GADBannerView alloc] initWithAdSize:kGADAdSizeSmartBannerPortrait origin:origin];
	self.bannerView.adUnitID = self.bannerId;
	self.bannerView.rootViewController = [self getViewController];
	self.bannerView.delegate = self;
	
	[[self getViewController].view addSubview: self.bannerView];
	
	isBannerLoaded = false;
	isBannerLoading = true;
	[self.bannerView loadRequest: [self makeRequest: location]];
}

// Hide banner
-(void) hideBanner
{
	if(!self.bannerView) return;
	
	[self.bannerView removeFromSuperview];
	self.bannerView = nil;
}

-(void) adViewDidReceiveAd:(GADBannerView*)adView
{
	DEBUG_LOG(@"adViewDidReceiveAd");
	
	isBannerLoading = false;
	isBannerLoaded = true;
}

-(void) adView:(GADBannerView*)adView didFailToReceiveAdWithError:(GADRequestError*)error
{
	NSLog(@"Banner failed to load with error %@", [error localizedDescription]);
	
	isBannerLoading = false;
	isBannerLoaded = false;
}


//-------------
// Interstitial
//-------------

// Is interstitial enabled
-(BOOL) isInterstitialEnabled
{
	return [self.interstitialId length] > 0;
}

// Check for interstitial is downloaded ready to showing
-(BOOL) isInterstitialReady
{
	if(![self isInterstitialEnabled] || !self.interstitial) return FALSE;
	
	return self.interstitial.isReady;
}

-(void) cacheInterstitial: (NSString*) location
{
	if(![self isInterstitialEnabled] || self.interstitial) return;
	
	DEBUG_LOG(@"cacheInterstitial");
	
	self.interstitial = [[GADInterstitial alloc] initWithAdUnitID: self.interstitialId];
	self.interstitial.delegate = self;
	[self.interstitial loadRequest:[self makeRequest: location]];
}

// Show interstitial
-(void) showInterstitial: (NSString*) location
{	
	if(!self.interstitial)
	{
		needShowInterstitial = true;
		[self cacheInterstitial: location];
		return;
	}
	
	DEBUG_LOG(@"AdMobMediation showInterstitial");
	
	if(!self.interstitial.isReady)
	{
		needShowInterstitial = true;
		return;
	}
	
	DEBUG_LOG(@"AdMobMediation showInterstitial isReady");
	
	[self.interstitial presentFromRootViewController:[self getViewController]];
}

- (void)interstitialDidReceiveAd:(GADInterstitial *)ad
{
	DEBUG_LOG(@"interstitialDidReceiveAd");
	
	if(needShowInterstitial) [self.interstitial presentFromRootViewController:[self getViewController]];
	needShowInterstitial = false;
}

-(void) interstitialDidDismissScreen:(GADInterstitial *)interstitial
{
	DEBUG_LOG(@"interstitialDidDismissScreen");
	
	self.interstitial = nil;
	self.setOnInterstitialClosedProp();
}

-(void) interstitial:(GADInterstitial*)interstitial didFailToReceiveAdWithError:(GADRequestError *)error
{
	NSLog(@"Intersisital failed to load with error %@", error.localizedDescription);
	
	self.interstitial = nil;
	needShowInterstitial = false;
}

//---------------
// Rewarded video
//---------------

// Is rewarded video enabled
-(BOOL) isRewardedVideoEnabled
{
	return [self.rewardedVideoId length] > 0;
}

// Check for rewarded video is downloaded ready to showing
-(BOOL) isRewardedVideoReady
{
	return [self isRewardedVideoEnabled] && [[GADRewardBasedVideoAd sharedInstance] isReady];
}

// Cache rewarded video
-(void) cacheRewardedVideo: (NSString*) location
{
	if(![self isRewardedVideoEnabled] || [[GADRewardBasedVideoAd sharedInstance] isReady] || isRewardedVideoCaching) return;
	
	DEBUG_LOG(@"cacheRewardedVideo");
	
	[GADRewardBasedVideoAd sharedInstance].delegate = self;
	[[GADRewardBasedVideoAd sharedInstance] loadRequest:[self makeRequest:location] withAdUnitID:self.rewardedVideoId];
	
	isRewardedVideoCaching = true;
}

// Show rewarded video
-(void) showRewardedVideo: (NSString*) location
{
	if (![[GADRewardBasedVideoAd sharedInstance] isReady]) return;
	
	DEBUG_LOG(@"showRewardedVideo");
	
	[[GADRewardBasedVideoAd sharedInstance] presentFromRootViewController:[self getViewController]];
}

- (void)rewardBasedVideoAd:(GADRewardBasedVideoAd *)rewardBasedVideoAd didRewardUserWithReward:(GADAdReward *)reward
{
	DEBUG_LOG(@"didRewardUserWithReward");
	
	wasRewardedVideoSuccess = true;
}

- (void)rewardBasedVideoAdDidReceiveAd:(GADRewardBasedVideoAd *)rewardBasedVideoAd
{
	DEBUG_LOG(@"Reward based video ad is received.");
	
	isRewardedVideoCaching = false;
}

- (void)rewardBasedVideoAdDidOpen:(GADRewardBasedVideoAd *)rewardBasedVideoAd
{
	DEBUG_LOG(@"Opened reward based video ad.");
	
	isRewardedVideoShowing = true;
}

- (void)rewardBasedVideoAdDidClose:(GADRewardBasedVideoAd *)rewardBasedVideoAd
{
	DEBUG_LOG(@"Reward based video ad is closed.");
	
	if(wasRewardedVideoSuccess) self.onRewardedVideoSuccessProp();
	else self.onRewardedVideoFailProp();
	
	wasRewardedVideoSuccess = false;
	isRewardedVideoShowing = false;
}

- (void)rewardBasedVideoAd:(GADRewardBasedVideoAd *)rewardBasedVideoAd didFailToLoadWithError:(NSError *)error
{
	NSLog(@"Reward based video ad failed to load.");
	
	if(isRewardedVideoShowing) self.onRewardedVideoErrorProp();
	isRewardedVideoCaching = false;
}

@end
