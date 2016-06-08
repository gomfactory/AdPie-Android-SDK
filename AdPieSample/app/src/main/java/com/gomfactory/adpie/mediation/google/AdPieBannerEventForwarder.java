/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.mediation.google;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class AdPieBannerEventForwarder implements AdView.AdListener {
    private CustomEventBannerListener mBannerListener;
    private AdView mAdView;

    public AdPieBannerEventForwarder(
            CustomEventBannerListener listener, AdView adView) {
        this.mBannerListener = listener;
        this.mAdView = adView;
    }

    @Override
    public void onAdLoaded() {
        mBannerListener.onAdLoaded(mAdView);
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        switch (errorCode) {
            case AdPieError.NO_FILL:
                mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                break;
            case AdPieError.INVALID_REQUEST:
                mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                break;
            case AdPieError.NETWORK_ERROR:
            case AdPieError.NO_CONNECTION:
                mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                break;
            default:
                mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                break;
        }
    }

    @Override
    public void onAdClicked() {
        mBannerListener.onAdClicked();
        mBannerListener.onAdLeftApplication();
    }
}