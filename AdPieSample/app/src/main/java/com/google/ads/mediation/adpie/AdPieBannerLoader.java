package com.google.ads.mediation.adpie;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.AdView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;

import org.json.JSONObject;

public class AdPieBannerLoader implements MediationBannerAd, AdView.AdListener {

    private String TAG = AdPieBannerLoader.class.getSimpleName();

    /** Configuration for requesting the banner ad from the third party network. */
    private final MediationBannerAdConfiguration mediationBannerAdConfiguration;

    /** Callback that fires on loading success or failure. */
    private final MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> mediationAdLoadCallback;

    /** Callback for banner ad events. */
    private MediationBannerAdCallback bannerAdCallback;

    private static final String APP_ID = "app_id";
    private static final String SLOT_ID = "slot_id";

    private static final String DOMAIN = "com.google.ads.mediation.adpie";

    private AdView mAdView;

    private String mAppId;
    private String mSlotId;

    public AdPieBannerLoader(@NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration,
                             @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> mediationAdLoadCallback) {
        this.mediationBannerAdConfiguration = mediationBannerAdConfiguration;
        this.mediationAdLoadCallback = mediationAdLoadCallback;
    }

    public void loadAd() {
        try {
            Log.d(TAG, "severParameters : " + mediationBannerAdConfiguration.getServerParameters());

            String serverParameter = mediationBannerAdConfiguration.getServerParameters().getString("parameter");

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

        final Context context = mediationBannerAdConfiguration.getContext();

        if (!AdPieSDK.getInstance().isInitialized()) {
            AdPieSDK.getInstance().initialize(context, mAppId, new AdPieSDK.OnInitializedListener() {
                @Override
                public void onCompleted(boolean result) {
                    if (result) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mAdView = new AdView(context);
                                mAdView.setSlotId(mSlotId);
                                mAdView.setAdListener(AdPieBannerLoader.this);

                                mAdView.load();
                            }
                        });
                    } else {
                        mediationAdLoadCallback.onFailure(new AdError(AdPieError.SDK_NOT_INITIALIZE, "Failed to initialize SDK.", DOMAIN));
                    }
                }
            });
        } else {
            mAdView = new AdView(context);
            mAdView.setSlotId(mSlotId);
            mAdView.setAdListener(AdPieBannerLoader.this);

            mAdView.load();
        }
    }

    @NonNull
    @Override
    public View getView() {
        return mAdView;
    }

    @Override
    public void onAdLoaded() {
        bannerAdCallback = mediationAdLoadCallback.onSuccess(this);
        bannerAdCallback.reportAdImpression();
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        mediationAdLoadCallback.onFailure(new AdError(errorCode, AdPieError.getMessage(errorCode), DOMAIN));
    }

    @Override
    public void onAdClicked() {
        bannerAdCallback.reportAdClicked();
        bannerAdCallback.onAdLeftApplication();
    }
}

