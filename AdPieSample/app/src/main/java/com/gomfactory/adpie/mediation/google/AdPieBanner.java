/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.mediation.google;

import android.content.Context;
import android.os.Bundle;

import com.gomfactory.adpie.sdk.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class AdPieBanner implements CustomEventBanner {

    private AdView adView;

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void requestBannerAd(Context context,
                                CustomEventBannerListener listener,
                                String serverParameter,
                                AdSize size,
                                MediationAdRequest mediationAdRequest,
                                Bundle customEventExtras) {

        adView = new AdView(context);
        adView.setSlotId(serverParameter);
        adView.setAdListener(new AdPieBannerEventForwarder(listener, adView));
        adView.load();

    }
}