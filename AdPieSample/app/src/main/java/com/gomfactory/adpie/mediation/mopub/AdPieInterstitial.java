/*******************************************************************************
 * Copyright (c) 2018 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.mediation.mopub;

import android.content.Context;
import android.text.TextUtils;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.InterstitialAd;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.Map;

public class AdPieInterstitial extends CustomEventInterstitial {

    private static final String SLOT_ID_KEY = "slotId";

    private CustomEventInterstitial.CustomEventInterstitialListener mInterstitialListener;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener,
                                    Map<String, Object> localExtras, Map<String, String> serverExtras) {

        mInterstitialListener = customEventInterstitialListener;

        if (TextUtils.isEmpty(serverExtras.get(SLOT_ID_KEY))) {
            if (mInterstitialListener != null) {
                mInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
            return;
        }

        String slotId = serverExtras.get(SLOT_ID_KEY);

        mInterstitialAd = new InterstitialAd(context, slotId);
        mInterstitialAd.setAdListener(new InterstitialAd.InterstitialAdListener() {
            @Override
            public void onAdLoaded() {
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialLoaded();
                }
                MoPubLog.d("AdPie onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (mInterstitialListener != null) {
                    switch (errorCode) {
                        case AdPieError.NO_FILL:
                            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
                            break;
                        case AdPieError.INVALID_REQUEST:
                            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                            break;
                        case AdPieError.NETWORK_ERROR:
                            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
                            break;
                        case AdPieError.NO_CONNECTION:
                            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_CONNECTION);
                            break;
                        case AdPieError.INTERNAL_ERROR:
                            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                            break;
                        default:
                            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
                            break;
                    }
                }

                MoPubLog.d("AdPie onAdFailedToLoad : " + AdPieError.getMessage(errorCode));
            }

            @Override
            public void onAdShown() {
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialShown();
                }
                MoPubLog.d("AdPie onAdShown");
            }

            @Override
            public void onAdClicked() {
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialClicked();
                }
                MoPubLog.d("AdPie onAdClicked");
            }

            @Override
            public void onAdDismissed() {
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialDismissed();
                }
                MoPubLog.d("AdPie onAdDismissed");
            }
        });
        mInterstitialAd.load();
    }

    @Override
    protected void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    protected void onInvalidate() {
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }
    }
}
