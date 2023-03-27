package com.google.ads.mediation.adpie;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.RewardedVideoAd;
import com.gomfactory.adpie.sdk.videoads.FinishState;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;

import org.json.JSONObject;

public class AdPieRewardedAdLoader implements MediationRewardedAd, RewardedVideoAd.RewardedVideoAdListener {

    private String TAG = AdPieRewardedAdLoader.class.getSimpleName();

    /**
     * Configuration for requesting the rewarded ad from the third party network.
     */
    private final MediationRewardedAdConfiguration mediationRewardedAdConfiguration;

    /**
     * A {@link MediationAdLoadCallback} that handles any callback when a Sample rewarded ad finishes
     * loading.
     */
    private final MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> mediationAdLoadCallback;

    /**
     * Used to forward rewarded video ad events to the Google Mobile Ads SDK.
     */
    private MediationRewardedAdCallback rewardedAdCallback;

    private static final String APP_ID = "app_id";
    private static final String SLOT_ID = "slot_id";

    private static final String DOMAIN = "com.google.ads.mediation.adpie";

    private RewardedVideoAd mRewardedVideoAd;

    private String mAppId;
    private String mSlotId;

    public AdPieRewardedAdLoader(MediationRewardedAdConfiguration adConfiguration,
                                 MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> adLoadCallback) {
        this.mediationRewardedAdConfiguration = adConfiguration;
        this.mediationAdLoadCallback = adLoadCallback;
    }

    public void loadAd() {
        try {
            Log.d(TAG, "severParameters : " + mediationRewardedAdConfiguration.getServerParameters());

            String serverParameter = mediationRewardedAdConfiguration.getServerParameters().getString("parameter");

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

        final Context context = mediationRewardedAdConfiguration.getContext();

        if (!AdPieSDK.getInstance().isInitialized()) {
            AdPieSDK.getInstance().initialize(context, mAppId, new AdPieSDK.OnInitializedListener() {
                @Override
                public void onCompleted(boolean result) {
                    if (result) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mRewardedVideoAd = new RewardedVideoAd(context, mSlotId);
                                mRewardedVideoAd.setAdListener(AdPieRewardedAdLoader.this);
                                mRewardedVideoAd.load();
                            }
                        });
                    } else {
                        mediationAdLoadCallback.onFailure(new AdError(AdPieError.SDK_NOT_INITIALIZE, "Failed to initialize SDK.", DOMAIN));
                    }
                }
            });
        } else {
            mRewardedVideoAd = new RewardedVideoAd(context, mSlotId);
            mRewardedVideoAd.setAdListener(AdPieRewardedAdLoader.this);
            mRewardedVideoAd.load();
        }
    }

    @Override
    public void showAd(@NonNull Context context) {
        if(mRewardedVideoAd != null && mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            rewardedAdCallback.onAdFailedToShow(new AdError(0, "No ad to show.", DOMAIN));
        }
    }

    @Override
    public void onRewardedVideoLoaded() {
        rewardedAdCallback = mediationAdLoadCallback.onSuccess(this);
    }

    @Override
    public void onRewardedVideoFailedToLoad(int errorCode) {
        mediationAdLoadCallback.onFailure(new AdError(errorCode, AdPieError.getMessage(errorCode), DOMAIN));
    }

    @Override
    public void onRewardedVideoClicked() {
        rewardedAdCallback.reportAdClicked();
    }

    @Override
    public void onRewardedVideoStarted() {
        rewardedAdCallback.onAdOpened();
        rewardedAdCallback.onVideoStart();
        rewardedAdCallback.reportAdImpression();
    }

    @Override
    public void onRewardedVideoFinished(FinishState finishState) {
        if (finishState == FinishState.COMPLETED) {
            rewardedAdCallback.onVideoComplete();
            rewardedAdCallback.onUserEarnedReward(new RewardItem() {
                @Override
                public int getAmount() {
                    return 1;
                }

                @NonNull
                @Override
                public String getType() {
                    return "reward";
                }
            });
        } else if (finishState == FinishState.ERROR || finishState == FinishState.UNKNOWN) {
            rewardedAdCallback.onAdFailedToShow(new AdError(0, "A video error occurred.", DOMAIN));
        }

        rewardedAdCallback.onAdClosed();
    }
}
