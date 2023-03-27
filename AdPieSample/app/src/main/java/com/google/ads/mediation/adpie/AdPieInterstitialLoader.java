package com.google.ads.mediation.adpie;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.InterstitialAd;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;

import org.json.JSONObject;

public class AdPieInterstitialLoader implements MediationInterstitialAd, InterstitialAd.InterstitialAdListener {

    private String TAG = AdPieInterstitialLoader.class.getSimpleName();

    /**
     * Configuration for requesting the interstitial ad from the third party network.
     */
    private final MediationInterstitialAdConfiguration mediationInterstitialAdConfiguration;

    /**
     * Callback that fires on loading success or failure.
     */
    private final MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> mediationAdLoadCallback;

    /**
     * Callback for interstitial ad events.
     */
    private MediationInterstitialAdCallback interstitialAdCallback;

    private static final String APP_ID = "app_id";
    private static final String SLOT_ID = "slot_id";

    private static final String DOMAIN = "com.google.ads.mediation.adpie";

    private InterstitialAd mInterstitialAd;

    private String mAppId;
    private String mSlotId;

    public AdPieInterstitialLoader(MediationInterstitialAdConfiguration mediationInterstitialAdConfiguration,
                                   MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> mediationAdLoadCallback) {
        this.mediationInterstitialAdConfiguration = mediationInterstitialAdConfiguration;
        this.mediationAdLoadCallback = mediationAdLoadCallback;
    }

    public void loadAd() {
        try {
            Log.d(TAG, "severParameters : " + mediationInterstitialAdConfiguration.getServerParameters());

            String serverParameter = mediationInterstitialAdConfiguration.getServerParameters().getString("parameter");

            JSONObject jsonObject = new JSONObject(serverParameter);
            mAppId = jsonObject.getString(APP_ID);
            mSlotId = jsonObject.getString(SLOT_ID);

            Log.d(TAG, "AppId : " + mAppId + ", SlotId : " + mSlotId);
        } catch (Exception e) {
            e.printStackTrace();
            mediationAdLoadCallback.onFailure(new AdError(AdPieError.SERVER_DATA_ERROR, "Parameters are invalid.", DOMAIN));
            return;
        }

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mSlotId)) {
            mediationAdLoadCallback.onFailure(new AdError(AdPieError.SERVER_DATA_ERROR, "Parameters are invalid.", DOMAIN));
            return;
        }

        final Context context = mediationInterstitialAdConfiguration.getContext();

        if (!AdPieSDK.getInstance().isInitialized()) {
            AdPieSDK.getInstance().initialize(context, mAppId, new AdPieSDK.OnInitializedListener() {
                @Override
                public void onCompleted(boolean result) {
                    if (result) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mInterstitialAd = new InterstitialAd(context, mSlotId);
                                mInterstitialAd.setAdListener(AdPieInterstitialLoader.this);
                                mInterstitialAd.load();
                            }
                        });
                    } else {
                        mediationAdLoadCallback.onFailure(new AdError(AdPieError.SDK_NOT_INITIALIZE, "Failed to initialize SDK.", DOMAIN));
                    }
                }
            });
        } else {
            mInterstitialAd = new InterstitialAd(context, mSlotId);
            mInterstitialAd.setAdListener(AdPieInterstitialLoader.this);
            mInterstitialAd.load();
        }
    }

    @Override
    public void showAd(@NonNull Context context) {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d(TAG, "Interstitial ad not ready");
        }
    }

    @Override
    public void onAdLoaded() {
        interstitialAdCallback = mediationAdLoadCallback.onSuccess(AdPieInterstitialLoader.this);
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        mediationAdLoadCallback.onFailure(new AdError(errorCode, AdPieError.getMessage(errorCode), DOMAIN));
    }

    @Override
    public void onAdShown() {
        interstitialAdCallback.reportAdImpression();
        interstitialAdCallback.onAdOpened();
    }

    @Override
    public void onAdClicked() {
        interstitialAdCallback.reportAdClicked();
        interstitialAdCallback.onAdLeftApplication();
    }

    @Override
    public void onAdDismissed() {
        interstitialAdCallback.onAdClosed();
    }
}
