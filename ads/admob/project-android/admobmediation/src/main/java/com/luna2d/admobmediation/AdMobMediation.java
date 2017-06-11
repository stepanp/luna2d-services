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

package com.luna2d.admobmediation;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.stepanp.luna2d.services.api.LunaActivityListener;
import com.stepanp.luna2d.services.api.LunaAdsService;
import com.stepanp.luna2d.services.api.LunaServicesApi;

public class AdMobMediation extends LunaAdsService implements LunaActivityListener, RewardedVideoAdListener
{
	private static boolean ENABLE_DEBUG_LOG = false;

	public AdMobMediation()
	{
		appId = LunaServicesApi.getConfigString("adMobAppId");
		bannerId = LunaServicesApi.getConfigString("adMobBannerId");
		interstitialId = LunaServicesApi.getConfigString("adMobInterstitialId");
		rewardedVideoId = LunaServicesApi.getConfigString("adMobRewardedVideoId");
		testDeviceIds = LunaServicesApi.getConfigStringArray("adMobTestDeviceIds");

		LunaServicesApi.runInUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Activity activity = LunaServicesApi.getSharedActivity();
				MobileAds.initialize(activity, appId);

				rewardedVideo = MobileAds.getRewardedVideoAdInstance(activity);
				rewardedVideo.setRewardedVideoAdListener(AdMobMediation.this);

				debugLog("AdMobMediation init");
			}
		});
	}

	private String appId;
	private String bannerId;
	private String interstitialId;
	private String rewardedVideoId;
	private String[] testDeviceIds;

	private InterstitialAd interstitial;
	private RewardedVideoAd rewardedVideo;
	private AdView bannerView;

	private boolean needShowInterstitial = false;
	private boolean isInterstitialLoaded = false;
	private boolean wasRewardedVideoSuccess = false;
	private boolean isRewardedVideoShowing = false;
	private boolean isRewardedVideoCaching = false;
	private boolean isRewardedVideoLoaded = false;
	private boolean isBannerShownFlag = false;

	private void debugLog(String text)
	{
		if(!ENABLE_DEBUG_LOG) return;

		Log.e(LunaServicesApi.getLogTag(), text);
	}

	private AdRequest makeRequest()
	{
		AdRequest.Builder builder = new AdRequest.Builder();

		for(String testDeviceId : testDeviceIds)
		{
			builder.addTestDevice(testDeviceId);
		}

		return builder.build();
	}

	@Override
	public int getBannerHeight()
	{
		Activity activity = LunaServicesApi.getSharedActivity();
		return AdSize.SMART_BANNER.getHeightInPixels(activity);
	}

	public boolean isBannerEnabled()
	{
		return !bannerId.isEmpty();
	}

	@Override
	public boolean isBannerShown()
	{
		return isBannerShownFlag;
	}

	@Override
	public void showBanner(String location)
	{
		if(!isBannerEnabled() || bannerView != null) return;

		debugLog("Show banner");

		isBannerShownFlag = true;

		LunaServicesApi.runInUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Activity activity = LunaServicesApi.getSharedActivity();

				bannerView = new AdView(activity);
				bannerView.setAdSize(AdSize.SMART_BANNER);
				bannerView.setAdUnitId(bannerId);
				bannerView.loadAd(makeRequest());
				bannerView.setAdListener(bannerListener);

				LinearLayout layout = new LinearLayout(activity);
				layout.setGravity(Gravity.BOTTOM);
				layout.addView(bannerView);

				ViewGroup content = (ViewGroup)activity.findViewById(android.R.id.content);
				content.addView(layout);
			}
		});
	}

	@Override
	public void hideBanner()
	{
		if(bannerView == null) return;

		debugLog("Hide banner");
		isBannerShownFlag = false;

		LunaServicesApi.runInUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				ViewGroup layout = (ViewGroup)bannerView.getParent();
				ViewGroup parent = (ViewGroup)layout.getParent();

				parent.removeView(layout);
				parent.invalidate();

				bannerView = null;
			}
		});
	}

	AdListener bannerListener = new AdListener()
	{
		@Override
		public void onAdLoaded()
		{
			debugLog("onAdLoaded banner");

			LunaServicesApi.runInUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					ViewGroup layout = (ViewGroup)bannerView.getParent();
					layout.requestLayout();
				}
			});
		}
	};

	public boolean isInterstitialEnabled()
	{
		return !interstitialId.isEmpty();
	}

	@Override
	public boolean isInterstitialReady()
	{
		if(!isInterstitialEnabled() || interstitial == null) return false;

		return isInterstitialLoaded;
	}

	@Override
	public void cacheInterstitial(String location)
	{
		if(!isInterstitialEnabled() || interstitial != null) return;

		debugLog("cacheInterstitial");

		LunaServicesApi.runInUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Activity activity = LunaServicesApi.getSharedActivity();
				interstitial = new InterstitialAd(activity);
				interstitial.setAdUnitId(interstitialId);
				interstitial.setAdListener(interstitialListener);
				interstitial.loadAd(makeRequest());
			}
		});
	}

	@Override
	public void showInterstitial(String location)
	{
		if(interstitial == null)
		{
			needShowInterstitial = true;
			cacheInterstitial(location);
			return;
		}

		debugLog("AdMobMediation showInterstitial");

		if(!isInterstitialLoaded)
		{
			needShowInterstitial = true;
			return;
		}

		debugLog("AdMobMediation showInterstitial isReady");

		LunaServicesApi.runInUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				interstitial.show();
			}
		});
	}

	private AdListener interstitialListener = new AdListener()
	{
		@Override
		public void onAdLoaded()
		{
			debugLog("onAdLoaded");

			isInterstitialLoaded = true;
			if(needShowInterstitial) interstitial.show();
			needShowInterstitial = false;
		}

		@Override
		public void onAdClosed()
		{
			debugLog("onAdClosed");

			interstitial = null;
			isInterstitialLoaded = false;

			LunaServicesApi.runInRenderThread(new Runnable()
			{
				@Override
				public void run()
				{
					onInterstitialClosed();
				}
			});
		}

		@Override
		public void onAdFailedToLoad(int errorCode)
		{
			Log.e(LunaServicesApi.getLogTag(), "Intersisital failted to load with error " + errorCode);

			isInterstitialLoaded = false;
			interstitial = null;
			needShowInterstitial = false;
		}
	};

	public boolean isRewardedVideoEnabled()
	{
		return !rewardedVideoId.isEmpty();
	}

	@Override
	public boolean isRewardedVideoReady()
	{
		return isRewardedVideoEnabled() && isRewardedVideoLoaded;
	}

	@Override
	public void cacheRewardedVideo(String location)
	{
		if(!isRewardedVideoEnabled() || isRewardedVideoLoaded || isRewardedVideoCaching) return;

		debugLog("cacheRewardedVideo");

		isRewardedVideoCaching = true;
		LunaServicesApi.runInUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				rewardedVideo.loadAd(rewardedVideoId, makeRequest());
			}
		});
	}

	@Override
	public void showRewardedVideo(String location)
	{
		if (!isRewardedVideoLoaded) return;

		debugLog("showRewardedVideo");

		LunaServicesApi.runInUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				rewardedVideo.show();
			}
		});
	}

	@Override
	public void onRewarded(RewardItem rewardItem)
	{
		debugLog("onRewarded");

		wasRewardedVideoSuccess = true;
	}

	@Override
	public void onRewardedVideoAdLoaded()
	{
		debugLog("onRewardedVideoAdLoaded");

		isRewardedVideoLoaded = true;
		isRewardedVideoCaching = false;
	}

	@Override
	public void onRewardedVideoAdOpened()
	{
		debugLog("onRewardedVideoAdOpened");

		isRewardedVideoShowing = true;
	}

	@Override
	public void onRewardedVideoAdClosed()
	{
		debugLog("onRewardedVideoAdClosed");

		isRewardedVideoLoaded = false;
		isRewardedVideoShowing = false;

		LunaServicesApi.runInRenderThread(new Runnable()
		{
			@Override
			public void run()
			{
				if(wasRewardedVideoSuccess) onRewardedVideoSuccess();
				else onRewardedVideoFail();

				wasRewardedVideoSuccess = false;
			}
		});
	}

	@Override
	public void onRewardedVideoAdFailedToLoad(int errorCode)
	{
		Log.e(LunaServicesApi.getLogTag(), "Reward based video ad failed to load");

		isRewardedVideoLoaded = false;
		isRewardedVideoCaching = false;

		LunaServicesApi.runInRenderThread(new Runnable()
		{
			@Override
			public void run()
			{
				if(isRewardedVideoShowing) onRewardedVideoError();
			}
		});
	}

	@Override
	public void onRewardedVideoStarted() {}

	@Override
	public void onRewardedVideoAdLeftApplication() {}

	@Override
	public void onStart(Activity activity)
	{

	}

	@Override
	public void onResume(Activity activity)
	{
		rewardedVideo.resume(activity);
		if(bannerView != null)
		{
			bannerView.resume();
			bannerView.setEnabled(true);
		}
	}

	@Override
	public void onPause(Activity activity)
	{
		rewardedVideo.pause(activity);
		if(bannerView != null)
		{
			bannerView.pause();
			bannerView.setEnabled(false);
		}
	}

	@Override
	public void onStop(Activity activity)
	{

	}

	@Override
	public void onDestroy(Activity activity)
	{
		rewardedVideo.destroy(activity);
		if(bannerView != null) bannerView.destroy();
	}

	@Override
	public boolean onBackPressed(Activity activity)
	{
		return false;
	}

	@Override
	public void onNetworkStateChanged(Activity activity, boolean b) {}

	@Override
	public boolean onActivityResult(int i, int i1, Intent intent) { return false; }
}
