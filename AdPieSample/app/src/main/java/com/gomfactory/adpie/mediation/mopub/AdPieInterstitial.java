package com.gomfactory.adpie.mediation.mopub;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.InterstitialAd;
import com.mopub.common.LifecycleListener;
import com.mopub.common.Preconditions;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.AdData;
import com.mopub.mobileads.BaseAd;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.Map;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CLICKED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_FAILED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_FAILED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_SUCCESS;

public class AdPieInterstitial extends BaseAd implements InterstitialAd.InterstitialAdListener {

    private static final String ADAPTER_NAME = AdPieInterstitial.class.getSimpleName();

    private static final String APP_ID_KEY = "app_id";
    private static final String SLOT_ID_KEY = "slot_id";

    private InterstitialAd mInterstitial;
    private String mAppID;
    private String mSlotID;

    public AdPieInterstitial() {
    }

    @Override
    protected void load(@NonNull Context context, @NonNull AdData adData) throws Exception {

        if (!extrasAreValid(context, adData)) {
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if (mInterstitial != null) {
            mInterstitial.destroy();
            mInterstitial = null;
        }

        mInterstitial = new InterstitialAd(context, mSlotID);
        mInterstitial.setAdListener(this);

        MoPubLog.log(getAdNetworkId(), LOAD_ATTEMPTED, ADAPTER_NAME);

        mInterstitial.load();
    }

    @Override
    protected void show() {
        MoPubLog.log(getAdNetworkId(), SHOW_ATTEMPTED, ADAPTER_NAME);

        if (mInterstitial.isLoaded()) {
            mInterstitial.show();
        } else {
            MoPubLog.log(getAdNetworkId(), SHOW_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.getIntCode(), MoPubErrorCode.NETWORK_NO_FILL);

            if (mInteractionListener != null) {
                mInteractionListener.onAdFailed(MoPubErrorCode.NETWORK_NO_FILL);
            }
        }
    }

    @Override
    protected void onInvalidate() {
        if (mInterstitial != null) {
            mInterstitial.destroy();
            mInterstitial = null;
        }
    }

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return mSlotID == null ? "" : mSlotID;
    }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity,
                                            @NonNull AdData adData) throws Exception {
        if (extrasAreValid(launcherActivity, adData)) {
            if (!AdPieSDK.getInstance().isInitialized()) {
                AdPieSDK.getInstance().initialize(launcherActivity, mAppID);
            }
        }

        return true;
    }


    @Override
    public void onAdLoaded() {
        MoPubLog.log(getAdNetworkId(), LOAD_SUCCESS, ADAPTER_NAME);

        if (mLoadListener != null) {
            mLoadListener.onAdLoaded();
        }
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        MoPubLog.log(getAdNetworkId(), LOAD_FAILED, ADAPTER_NAME,
                MoPubErrorCode.NETWORK_NO_FILL.getIntCode(), MoPubErrorCode.NETWORK_NO_FILL);
        MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, "Failed to load ads with errorCode: " + errorCode);

        if (mLoadListener != null) {
            mLoadListener.onAdLoadFailed(MoPubErrorCode.NETWORK_NO_FILL);
        }
    }

    @Override
    public void onAdShown() {
        MoPubLog.log(getAdNetworkId(), SHOW_SUCCESS, ADAPTER_NAME);

        if (mInteractionListener != null) {
            mInteractionListener.onAdShown();
        }
    }

    @Override
    public void onAdClicked() {
        MoPubLog.log(getAdNetworkId(), CLICKED, ADAPTER_NAME);

        if (mInteractionListener != null) {
            mInteractionListener.onAdClicked();
        }
    }

    @Override
    public void onAdDismissed() {
        if (mInteractionListener != null) {
            mInteractionListener.onAdDismissed();
        }
    }

    private boolean extrasAreValid(@NonNull Context context,
                                   @NonNull AdData adData) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(adData);

        final Map<String, String> extras = adData.getExtras();
        if (extras.isEmpty()) {
            MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, "Server data is null or empty.");

            if (mLoadListener != null) {
                mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                return false;
            }
        }

        if (extras.containsKey(APP_ID_KEY) && extras.containsKey(SLOT_ID_KEY)) {
            mAppID = extras.get(APP_ID_KEY);
            mSlotID = extras.get(SLOT_ID_KEY);
            return true;
        }

        return false;
    }
}
