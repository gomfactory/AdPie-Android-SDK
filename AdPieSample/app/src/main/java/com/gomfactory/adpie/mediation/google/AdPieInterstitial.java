/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.mediation.google;

import android.content.Context;
import android.os.Bundle;

import com.gomfactory.adpie.sdk.InterstitialAd;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

public class AdPieInterstitial implements CustomEventInterstitial {

    private InterstitialAd interstitial;

    @Override
    public void requestInterstitialAd(Context context,
                                      CustomEventInterstitialListener listener,
                                      String serverParameter,
                                      MediationAdRequest mediationAdRequest,
                                      Bundle customEventExtras) {

        interstitial = new InterstitialAd(context, serverParameter);
        interstitial.setAdListener(new AdPieInterstitialEventForwarder(listener));
        interstitial.load();
    }


    @Override
    public void showInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    @Override
    public void onDestroy() {
        if (interstitial != null) {
            interstitial = null;
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }
}