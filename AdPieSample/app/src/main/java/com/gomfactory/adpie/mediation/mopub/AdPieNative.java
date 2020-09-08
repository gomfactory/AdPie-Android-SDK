/*******************************************************************************
 * Copyright (c) 2018 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.mediation.mopub;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.NativeAd;
import com.gomfactory.adpie.sdk.nativeads.NativeAdData;
import com.gomfactory.adpie.sdk.nativeads.NativeAdView;
import com.mopub.common.logging.MoPubLog;
import com.mopub.nativeads.CustomEventNative;
import com.mopub.nativeads.ImpressionTracker;
import com.mopub.nativeads.NativeClickHandler;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeImageHelper;
import com.mopub.nativeads.StaticNativeAd;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;

public class AdPieNative extends CustomEventNative {

    private static final String ADAPTER_NAME = AdPieNative.class.getSimpleName();

    private static final String APP_ID_KEY = "app_id";
    private static final String SLOT_ID_KEY = "slotId";

    private CustomEventNativeListener mCustomEventNativeListener;

    private String mSlotId;

    @Override
    protected void loadNativeAd(@NonNull Context context, @NonNull CustomEventNativeListener customEventNativeListener,
                                @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) {

        mCustomEventNativeListener = customEventNativeListener;

        if (TextUtils.isEmpty(serverExtras.get(APP_ID_KEY)) || TextUtils.isEmpty(serverExtras.get(SLOT_ID_KEY))) {
            if (customEventNativeListener != null) {
                mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            }
            return;
        }

        String appId = serverExtras.get(APP_ID_KEY);
        if (!AdPieSDK.getInstance().isInitialized()) {
            AdPieSDK.getInstance().initialize(context, appId);
        }

        mSlotId = serverExtras.get(SLOT_ID_KEY);

        NativeAd nativeAd = new NativeAd(context, null);
        nativeAd.setSlotId(mSlotId);
        nativeAd.setSkipDownload(true);

        AdPieNativeAd adPieNativeAd =
                new AdPieNativeAd(context, nativeAd, customEventNativeListener);
        adPieNativeAd.loadAd();
    }

    class AdPieNativeAd extends StaticNativeAd implements NativeAd.AdListener {

        private Context mContext;
        private NativeAd mNativeAd;
        private ImpressionTracker mImpressionTracker;
        private NativeClickHandler mNativeClickHandler;

        public AdPieNativeAd(Context context, NativeAd nativeAd,
                             CustomEventNativeListener customEventNativeListener) {

            mContext = context.getApplicationContext();
            mNativeAd = nativeAd;
            mNativeAd.setAdListener(this);

            mImpressionTracker = new ImpressionTracker(mContext);
            mNativeClickHandler = new NativeClickHandler(mContext);
        }

        public void loadAd() {
            if (mNativeAd != null) {
                mNativeAd.loadAd();
            }
        }

        @Override
        public void recordImpression(@NonNull View view) {
            notifyAdImpressed();
        }

        @Override
        public void prepare(@NonNull View view) {
            if (mImpressionTracker != null) {
                mImpressionTracker.addView(view, this);
            }
            if (mNativeClickHandler != null) {
                mNativeClickHandler.setOnClickListener(view, this);
            }
        }

        @Override
        public void clear(@NonNull View view) {
            if (mImpressionTracker != null) {
                mImpressionTracker.removeView(view);
            }
            if (mNativeClickHandler != null) {
                mNativeClickHandler.clearOnClickListener(view);
            }
        }

        @Override
        public void handleClick(@NonNull View view) {
            notifyAdClicked();
            if (mCustomEventNativeListener != null) {
                mNativeClickHandler.openClickDestinationUrl(getClickDestinationUrl(), view);
            }
        }

        @Override
        public void destroy() {
            if (mNativeAd != null) {
                mNativeAd.destroy();
                mNativeAd = null;
            }
        }

        @Override
        public void onAdLoaded(NativeAdView nativeAdView) {

            MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, "AdPie onAdLoaded");

            NativeAdData nativeAdData = nativeAdView.getNativeAdData();

            setTitle(nativeAdData.getTitle());
            setText(nativeAdData.getDescription());
            setIconImageUrl(nativeAdData.getIconImageUrl());

            setMainImageUrl(nativeAdData.getMainImageUrl());
            if (!TextUtils.isEmpty(nativeAdData.getCallToAction())) {
                setCallToAction(nativeAdData.getCallToAction());
            } else {
                setCallToAction("Click Here");
            }

            setClickDestinationUrl(nativeAdData.getLink());

            if (!TextUtils.isEmpty(nativeAdData.getOptoutLink())) {
                setPrivacyInformationIconClickThroughUrl(nativeAdData.getOptoutLink());
            }
            if (!TextUtils.isEmpty(nativeAdData.getOptoutImageUrl())) {
                setPrivacyInformationIconImageUrl(nativeAdData.getOptoutImageUrl());
            }

            addImpressionTrackers(new JSONArray(nativeAdData.getTrackingImpUrls()));
            addClickTrackers(new JSONArray(nativeAdData.getTrackingClkUrls()));

            final List<String> imageUrls = new ArrayList<>();
            String iconUrl = getIconImageUrl();
            if (!TextUtils.isEmpty(iconUrl)) {
                imageUrls.add(getIconImageUrl());
            }
            String mainImageUrl = getMainImageUrl();
            if (!TextUtils.isEmpty(mainImageUrl)) {
                imageUrls.add(getMainImageUrl());
            }

            NativeImageHelper.preCacheImages(mContext, imageUrls, new NativeImageHelper.ImageListener() {
                @Override
                public void onImagesCached() {
                    MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, "AdPie onImagesCached");
                    if (mCustomEventNativeListener != null) {
                        mCustomEventNativeListener.onNativeAdLoaded(AdPieNativeAd.this);
                    }
                }

                @Override
                public void onImagesFailedToCache(NativeErrorCode errorCode) {
                    if (mCustomEventNativeListener != null) {
                        mCustomEventNativeListener.onNativeAdFailed(errorCode);
                    }
                }
            });

            nativeAdView.destroy();
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            if (mCustomEventNativeListener != null) {
                switch (errorCode) {
                    case AdPieError.NO_FILL:
                        mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_NO_FILL);
                        break;
                    case AdPieError.INVALID_REQUEST:
                        mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
                        break;
                    case AdPieError.NETWORK_ERROR:
                        mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_REQUEST);
                        break;
                    case AdPieError.NO_CONNECTION:
                        mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.CONNECTION_ERROR);
                        break;
                    case AdPieError.INTERNAL_ERROR:
                        mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
                        break;
                    default:
                        mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
                        break;
                }
            }

            MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, "AdPie onAdFailedToLoad : " + AdPieError.getMessage(errorCode));
        }

        @Override
        public void onAdShown() {
            MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, "AdPie onAdShown");
        }

        @Override
        public void onAdClicked() {
            MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, "AdPie onAdClicked");
        }

        private String getAdNetworkId() {
            return mSlotId;
        }
    }
}