/*******************************************************************************
 * Copyright (c) 2018 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.mediation.mopub;

import android.content.Context;
import android.text.TextUtils;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdView;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.Map;

public class AdPieBanner extends CustomEventBanner {

    private static final String SLOT_ID_KEY = "slotId";

    private CustomEventBannerListener mBannerListener;
    private AdView mAdView;

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener,
                              Map<String, Object> localExtras, Map<String, String> serverExtras) {

        mBannerListener = customEventBannerListener;

        if (TextUtils.isEmpty(serverExtras.get(SLOT_ID_KEY))) {
            if (mBannerListener != null) {
                mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
            return;
        }

        String slotId = serverExtras.get(SLOT_ID_KEY);

        mAdView = new AdView(context);
        mAdView.setSlotId(slotId);
        mAdView.setScaleUp(true);
        mAdView.setAdListener(new AdView.AdListener() {

            @Override
            public void onAdLoaded() {
                if (mBannerListener != null) {
                    mBannerListener.onBannerLoaded(mAdView);
                }
                MoPubLog.d("AdPie onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (mBannerListener != null) {
                    switch (errorCode) {
                        case AdPieError.NO_FILL:
                            mBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
                            break;
                        case AdPieError.INVALID_REQUEST:
                            mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                            break;
                        case AdPieError.NETWORK_ERROR:
                            mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
                            break;
                        case AdPieError.NO_CONNECTION:
                            mBannerListener.onBannerFailed(MoPubErrorCode.NO_CONNECTION);
                            break;
                        case AdPieError.INTERNAL_ERROR:
                            mBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                            break;
                        default:
                            mBannerListener.onBannerFailed(MoPubErrorCode.UNSPECIFIED);
                            break;
                    }
                }

                MoPubLog.d("AdPie onAdFailedToLoad : " + AdPieError.getMessage(errorCode));
            }

            @Override
            public void onAdClicked() {
                if (mBannerListener != null) {
                    mBannerListener.onBannerClicked();
                }
                MoPubLog.d("AdPie onAdClicked");
            }
        });

        mAdView.load();

    }

    @Override
    protected void onInvalidate() {
        if (mAdView != null) {
            mAdView.destroy();
            mAdView = null;
        }
    }
}