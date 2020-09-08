package com.gomfactory.adpie.mediation.mopub;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.AdView;
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
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_SUCCESS;

public class AdPieBanner extends BaseAd implements AdView.AdListener {

    private static final String ADAPTER_NAME = AdPieBanner.class.getSimpleName();

    private static final String APP_ID_KEY = "app_id";
    private static final String SLOT_ID_KEY = "slot_id";

    private AdView mAdView;
    private String mAppID;
    private String mSlotID;

    public AdPieBanner() {
    }

    @Override
    protected void load(@NonNull Context context, @NonNull AdData adData) throws Exception {
        if (!extrasAreValid(context, adData)) {
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if (mAdView != null) {
            mAdView.destroy();
            mAdView = null;
        }

        mAdView = new AdView(context);
        mAdView.setSlotId(mSlotID);
        mAdView.setScaleUp(true);
        mAdView.setAdListener(this);

        MoPubLog.log(getAdNetworkId(), LOAD_ATTEMPTED, ADAPTER_NAME);

        mAdView.load();
    }

    @Override
    protected void onInvalidate() {
        if (mAdView != null) {
            mAdView.destroy();
            mAdView = null;
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

    @Nullable
    @Override
    protected View getAdView() {
        return mAdView;
    }

    @Override
    public void onAdLoaded() {
        MoPubLog.log(getAdNetworkId(), LOAD_SUCCESS, ADAPTER_NAME);
        MoPubLog.log(getAdNetworkId(), SHOW_ATTEMPTED, ADAPTER_NAME);
        MoPubLog.log(getAdNetworkId(), SHOW_SUCCESS, ADAPTER_NAME);

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
    public void onAdClicked() {
        MoPubLog.log(getAdNetworkId(), CLICKED, ADAPTER_NAME);

        if (mInteractionListener != null) {
            mInteractionListener.onAdClicked();
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
