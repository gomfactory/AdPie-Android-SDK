/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.mediation.google;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.InterstitialAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

public class AdPieInterstitialEventForwarder implements InterstitialAd.InterstitialAdListener {
    private CustomEventInterstitialListener mInterstitialListener;

    public AdPieInterstitialEventForwarder(CustomEventInterstitialListener listener) {
        this.mInterstitialListener = listener;
    }

    @Override
    public void onAdLoaded() {
        mInterstitialListener.onAdLoaded();
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        switch (errorCode) {
            case AdPieError.NO_FILL:
                mInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                break;
            case AdPieError.INVALID_REQUEST:
                mInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                break;
            case AdPieError.NETWORK_ERROR:
            case AdPieError.NO_CONNECTION:
                mInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                break;
            default:
                mInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                break;
        }
    }

    @Override
    public void onAdShown() {
        mInterstitialListener.onAdOpened();
    }

    @Override
    public void onAdClicked() {
        mInterstitialListener.onAdClicked();
        mInterstitialListener.onAdLeftApplication();
    }

    @Override
    public void onAdDismissed() {
        mInterstitialListener.onAdClosed();
    }

}