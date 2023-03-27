package com.google.ads.mediation.adpie;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gomfactory.adpie.sdk.AdPieSDK;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;
import com.google.android.gms.ads.mediation.MediationNativeAdCallback;
import com.google.android.gms.ads.mediation.MediationNativeAdConfiguration;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;
import com.google.android.gms.ads.mediation.VersionInfo;

import java.util.List;

public class AdPieMediationAdapter extends Adapter {

    private String TAG = AdPieMediationAdapter.class.getSimpleName();

    private AdPieBannerLoader mAdPieBannerLoader;
    private AdPieInterstitialLoader mAdPieInterstitialLoader;
    private AdPieRewardedAdLoader mAdPieRewardedAdLoader;

    @Override
    public VersionInfo getVersionInfo() {
        String versionString = AdPieSDK.getInstance().getVersion() + ".0";
        String[] splits = versionString.split("\\.");

        if (splits.length >= 4) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]) * 100 + Integer.parseInt(splits[3]);
            return new VersionInfo(major, minor, micro);
        }

        return new VersionInfo(0, 0, 0);
    }

    @Override
    public VersionInfo getSDKVersionInfo() {
        String versionString = AdPieSDK.getInstance().getVersion();
        String[] splits = versionString.split("\\.");

        if (splits.length >= 3) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]);
            return new VersionInfo(major, minor, micro);
        }

        return new VersionInfo(0, 0, 0);
    }

    @Override
    public void initialize(@NonNull Context context,
                           @NonNull InitializationCompleteCallback initializationCompleteCallback,
                           @NonNull List<MediationConfiguration> list) {
    }

    @Override
    public void loadBannerAd(@NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration, @NonNull final MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {
        Log.d(TAG, "loadBannerAd : " + mediationBannerAdConfiguration);
        mAdPieBannerLoader = new AdPieBannerLoader(mediationBannerAdConfiguration, callback);
        mAdPieBannerLoader.loadAd();

    }

    @Override
    public void loadInterstitialAd(@NonNull MediationInterstitialAdConfiguration mediationInterstitialAdConfiguration, @NonNull MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> callback) {
        Log.d(TAG, "loadInterstitialAd : " + mediationInterstitialAdConfiguration);
        mAdPieInterstitialLoader = new AdPieInterstitialLoader(mediationInterstitialAdConfiguration, callback);
        mAdPieInterstitialLoader.loadAd();
    }

    @Override
    public void loadNativeAd(@NonNull MediationNativeAdConfiguration mediationNativeAdConfiguration, @NonNull MediationAdLoadCallback<UnifiedNativeAdMapper, MediationNativeAdCallback> callback) {
    }

    @Override
    public void loadRewardedAd(@NonNull MediationRewardedAdConfiguration mediationRewardedAdConfiguration, @NonNull MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> callback) {
        Log.d(TAG, "loadRewardedAd : " + mediationRewardedAdConfiguration);
        mAdPieRewardedAdLoader = new AdPieRewardedAdLoader(mediationRewardedAdConfiguration, callback);
        mAdPieRewardedAdLoader.loadAd();
    }
}