package com.applovin.mediation.adapters;

import static com.applovin.sdk.AppLovinSdkUtils.runOnUiThread;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.MaxInterstitialAdapter;
import com.applovin.mediation.adapter.MaxNativeAdAdapter;
import com.applovin.mediation.adapter.MaxRewardedAdapter;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxNativeAdAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.sdk.AppLovinSdk;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.AdView;
import com.gomfactory.adpie.sdk.InterstitialAd;
import com.gomfactory.adpie.sdk.NativeAd;
import com.gomfactory.adpie.sdk.RewardedVideoAd;
import com.gomfactory.adpie.sdk.nativeads.NativeAdData;
import com.gomfactory.adpie.sdk.nativeads.NativeAdView;
import com.gomfactory.adpie.sdk.util.ClickThroughUtil;
import com.gomfactory.adpie.sdk.util.DisplayUtil;
import com.gomfactory.adpie.sdk.util.ReportUtil;
import com.gomfactory.adpie.sdk.videoads.FinishState;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AdPieMediationAdapter extends MediationAdapterBase
        implements MaxAdViewAdapter, MaxInterstitialAdapter, MaxRewardedAdapter, MaxNativeAdAdapter {

    private static final String TAG = AdPieMediationAdapter.class.getSimpleName();

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    private NativeAd mNativeAd;

    public interface DrawableDownloadListener {
        void onDownloadSuccess(HashMap<String, Drawable> drawableMap);

        void onDownloadFailure();
    }

    public AdPieMediationAdapter(AppLovinSdk appLovinSdk) {
        super(appLovinSdk);
    }

    @Override
    public void initialize(MaxAdapterInitializationParameters maxAdapterInitializationParameters,
                           Activity activity, OnCompletionListener onCompletionListener) {
        onCompletionListener.onCompletion(InitializationStatus.DOES_NOT_APPLY, null);
    }

    @Override
    public String getSdkVersion() {
        return AdPieSDK.getInstance().getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return AdPieSDK.getInstance().getVersion() + ".0";
    }

    @Override
    public void onDestroy() {
        log("Destroy called for adapter " + this);

        if (mAdView != null) {
            mAdView.destroy();
            mAdView = null;
        }

        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }

        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.destroy();
            mRewardedVideoAd = null;
        }

        if (mNativeAd != null) {
            mNativeAd.destroy();
            mNativeAd = null;
        }
    }

    @Override
    public void loadAdViewAd(MaxAdapterResponseParameters maxAdapterResponseParameters, MaxAdFormat maxAdFormat,
                             Activity activity, MaxAdViewAdapterListener maxAdViewAdapterListener) {
        final String adId = maxAdapterResponseParameters.getThirdPartyAdPlacementId();
        d("Loading " + maxAdFormat.getLabel() + " AdView ad for placement: " + adId + "...");

        final String appId = maxAdapterResponseParameters.getServerParameters().getString("app_id", null);
        Log.d(TAG, "AppId : " + appId + ", SlotId : " + adId);

        if (!TextUtils.isEmpty(appId)) {
            if (!AdPieSDK.getInstance().isInitialized()) {
                AdPieSDK.getInstance().initialize(activity, appId);
            }
        } else {
            if (maxAdViewAdapterListener != null) {
                maxAdViewAdapterListener.onAdViewAdLoadFailed(MaxAdapterError.BAD_REQUEST);
            }
            return;
        }

        final MaxAdViewAdapterListener listener = maxAdViewAdapterListener;

        final Activity finalActivity = activity;

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdView = new AdView(finalActivity);
                    mAdView.setSlotId(adId);
                    mAdView.setAdListener(new AdView.AdListener() {
                        @Override
                        public void onAdLoaded() {
                            if (listener != null) {
                                listener.onAdViewAdLoaded(mAdView);
                                listener.onAdViewAdDisplayed();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            if (listener != null) {
                                listener.onAdViewAdLoadFailed(MaxAdapterError.NO_FILL);
                            }
                        }

                        @Override
                        public void onAdClicked() {
                            if (listener != null) {
                                listener.onAdViewAdClicked();
                            }
                        }
                    });

                    mAdView.load();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            if (listener != null) {
                listener.onAdViewAdLoadFailed(MaxAdapterError.INTERNAL_ERROR);
            }
        }
    }

    @Override
    public void loadInterstitialAd(MaxAdapterResponseParameters maxAdapterResponseParameters, Activity activity,
                                   MaxInterstitialAdapterListener maxInterstitialAdapterListener) {
        final String adId = maxAdapterResponseParameters.getThirdPartyAdPlacementId();
        d("Loading interstitial ad for ad id: " + adId + "...");

        final String appId = maxAdapterResponseParameters.getServerParameters().getString("app_id", null);
        Log.d(TAG, "AppId : " + appId + ", SlotId : " + adId);

        if (!TextUtils.isEmpty(appId)) {
            if (!AdPieSDK.getInstance().isInitialized()) {
                AdPieSDK.getInstance().initialize(activity, appId);
            }
        } else {
            if (maxInterstitialAdapterListener != null) {
                maxInterstitialAdapterListener.onInterstitialAdLoadFailed(MaxAdapterError.BAD_REQUEST);
            }
            return;
        }

        final MaxInterstitialAdapterListener listener = maxInterstitialAdapterListener;

        final Activity finalActivity = activity;

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mInterstitialAd = new InterstitialAd(finalActivity, adId);
                    mInterstitialAd.setAdListener(new InterstitialAd.InterstitialAdListener() {
                        @Override
                        public void onAdLoaded() {
                            if (listener != null) {
                                listener.onInterstitialAdLoaded();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            if (listener != null) {
                                listener.onInterstitialAdLoadFailed(MaxAdapterError.NO_FILL);
                            }
                        }

                        @Override
                        public void onAdShown() {
                            if (listener != null) {
                                listener.onInterstitialAdDisplayed();
                            }
                        }

                        @Override
                        public void onAdClicked() {
                            if (listener != null) {
                                listener.onInterstitialAdClicked();
                            }
                        }

                        @Override
                        public void onAdDismissed() {
                            if (listener != null) {
                                listener.onInterstitialAdHidden();
                            }
                        }

                        @Override
                        public void onAdFailedToShow() {
                            listener.onInterstitialAdDisplayFailed(MaxAdapterError.AD_DISPLAY_FAILED);
                        }
                    });

                    mInterstitialAd.load();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            if (listener != null) {
                listener.onInterstitialAdLoadFailed(MaxAdapterError.INTERNAL_ERROR);
            }
        }
    }

    @Override
    public void showInterstitialAd(MaxAdapterResponseParameters maxAdapterResponseParameters, Activity activity,
                                   MaxInterstitialAdapterListener maxInterstitialAdapterListener) {
        final MaxInterstitialAdapterListener listener = maxInterstitialAdapterListener;

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        log("Interstitial ad not ready");

                        if (listener != null) {
                            listener.onInterstitialAdDisplayFailed(MaxAdapterError.AD_NOT_READY);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void loadRewardedAd(MaxAdapterResponseParameters maxAdapterResponseParameters, Activity activity,
                               MaxRewardedAdapterListener maxRewardedAdapterListener) {
        final String adId = maxAdapterResponseParameters.getThirdPartyAdPlacementId();
        d("Loading rewarded ad for ad id: " + adId + "...");

        final String appId = maxAdapterResponseParameters.getServerParameters().getString("app_id", null);
        Log.d(TAG, "AppId : " + appId + ", SlotId : " + adId);

        if (!TextUtils.isEmpty(appId)) {
            if (!AdPieSDK.getInstance().isInitialized()) {
                AdPieSDK.getInstance().initialize(activity, appId);
            }
        } else {
            if (maxRewardedAdapterListener != null) {
                maxRewardedAdapterListener.onRewardedAdLoadFailed(MaxAdapterError.BAD_REQUEST);
            }
            return;
        }

        final MaxRewardedAdapterListener listener = maxRewardedAdapterListener;

        final Activity finalActivity = activity;

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRewardedVideoAd = new RewardedVideoAd(finalActivity, adId);
                    mRewardedVideoAd.setAdListener(new RewardedVideoAd.RewardedVideoAdListener() {
                        @Override
                        public void onRewardedVideoLoaded() {
                            if (listener != null) {
                                listener.onRewardedAdLoaded();
                            }
                        }

                        @Override
                        public void onRewardedVideoFailedToLoad(int errorCode) {
                            if (listener != null) {
                                listener.onRewardedAdLoadFailed(MaxAdapterError.NO_FILL);
                            }
                        }

                        @Override
                        public void onRewardedVideoClicked() {
                            if (listener != null) {
                                listener.onRewardedAdClicked();
                            }
                        }

                        @Override
                        public void onRewardedVideoStarted() {
                            if (listener != null) {
                                listener.onRewardedAdDisplayed();
                            }
                        }

                        @Override
                        public void onRewardedVideoFinished(FinishState finishState) {
                            if (finishState == FinishState.COMPLETED) {
                                if (listener != null) {
                                    MaxReward reward = getReward();
                                    listener.onUserRewarded(reward);
                                }
                            } else if (finishState == FinishState.ERROR || finishState == FinishState.UNKNOWN) {
                                if (listener != null) {
                                    listener.onRewardedAdDisplayFailed(MaxAdapterError.AD_DISPLAY_FAILED);
                                }
                            }

                            if (listener != null) {
                                listener.onRewardedAdHidden();
                            }
                        }
                    });

                    mRewardedVideoAd.load();

                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            if (listener != null) {
                listener.onRewardedAdLoadFailed(MaxAdapterError.INTERNAL_ERROR);
            }
        }
    }

    @Override
    public void showRewardedAd(MaxAdapterResponseParameters maxAdapterResponseParameters, Activity activity,
                               MaxRewardedAdapterListener maxRewardedAdapterListener) {
        final MaxRewardedAdapterListener listener = maxRewardedAdapterListener;

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRewardedVideoAd != null && mRewardedVideoAd.isLoaded()) {
                        mRewardedVideoAd.show();
                    } else {
                        log("Rewarded ad not ready");

                        listener.onRewardedAdDisplayFailed(MaxAdapterError.AD_NOT_READY);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void loadNativeAd(MaxAdapterResponseParameters maxAdapterResponseParameters, Activity activity,
                             MaxNativeAdAdapterListener maxNativeAdAdapterListener) {
        final String adId = maxAdapterResponseParameters.getThirdPartyAdPlacementId();
        d("Loading native ad for ad id: " + adId + "...");

        final String appId = maxAdapterResponseParameters.getServerParameters().getString("app_id", null);
        Log.d(TAG, "AppId : " + appId + ", SlotId : " + adId);

        if (!TextUtils.isEmpty(appId)) {
            if (!AdPieSDK.getInstance().isInitialized()) {
                AdPieSDK.getInstance().initialize(activity, appId);
            }
        } else {
            if (maxNativeAdAdapterListener != null) {
                maxNativeAdAdapterListener.onNativeAdLoadFailed(MaxAdapterError.BAD_REQUEST);
            }
            return;
        }

        final MaxNativeAdAdapterListener listener = maxNativeAdAdapterListener;

        final Activity finalActivity = activity;

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mNativeAd = new NativeAd(finalActivity, null);
                    mNativeAd.setSlotId(adId);
                    mNativeAd.setAdListener(new NativeAd.AdListener() {
                        @Override
                        public void onAdLoaded(NativeAdView nativeAdView) {
                            try {

                                final NativeAdData nativeAdData = nativeAdView.getNativeAdData();

                                HashMap<String, URL> map = new HashMap<>();
                                if (!TextUtils.isEmpty(nativeAdData.getIconImageUrl())) {
                                    map.put(DrawableDownloadUtil.KEY_ICON, new URL(nativeAdData.getIconImageUrl()));
                                }
                                if (!TextUtils.isEmpty(nativeAdData.getMainImageUrl())) {
                                    map.put(DrawableDownloadUtil.KEY_MAIN_IMAGE, new URL(nativeAdData.getMainImageUrl()));
                                }
                                if (!TextUtils.isEmpty(nativeAdData.getOptoutImageUrl())) {
                                    map.put(DrawableDownloadUtil.KEY_PRIVACY_ICON, new URL(nativeAdData.getOptoutImageUrl()));
                                }

                                new DrawableDownloadUtil(new DrawableDownloadListener() {
                                    @Override
                                    public void onDownloadSuccess(HashMap<String, Drawable> drawableMap) {
                                        Drawable iconDrawable = drawableMap.get(DrawableDownloadUtil.KEY_ICON);
                                        Drawable mainDrawable = drawableMap.get(DrawableDownloadUtil.KEY_MAIN_IMAGE);
                                        Drawable privacyIconDrawable = drawableMap.get(DrawableDownloadUtil.KEY_PRIVACY_ICON);

                                        ImageView mainImageView = new ImageView(finalActivity);
                                        mainImageView.setImageDrawable(mainDrawable);

                                        ImageView privacyImageView = new ImageView(finalActivity);
                                        if (privacyIconDrawable != null) {
                                            privacyImageView.setVisibility(View.VISIBLE);
                                            privacyImageView.setLayoutParams(new ViewGroup.LayoutParams(
                                                    DisplayUtil.dpToPx(finalActivity, 20), DisplayUtil.dpToPx(finalActivity, 20)));
                                            privacyImageView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    try {
                                                        ClickThroughUtil.goToBrowser(finalActivity, nativeAdData.getOptoutLink());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            privacyImageView.setImageDrawable(privacyIconDrawable);
                                        }

                                        MaxNativeAd.Builder builder = new MaxNativeAd.Builder()
                                                .setAdFormat(MaxAdFormat.NATIVE)
                                                .setTitle(nativeAdData.getTitle())
                                                .setBody(nativeAdData.getDescription())
                                                .setCallToAction(nativeAdData.getCallToAction())
                                                .setIcon(new MaxNativeAd.MaxNativeAdImage(iconDrawable))
                                                .setMediaView(mainImageView)
                                                .setOptionsView(privacyImageView);
                                        MaxNativeAd maxNativeAd = new MaxAdPieNativeAd(builder, listener, nativeAdData);

                                        if (listener != null) {
                                            listener.onNativeAdLoaded(maxNativeAd, null);
                                        }
                                    }

                                    @Override
                                    public void onDownloadFailure() {
                                        if (listener != null) {
                                            listener.onNativeAdLoadFailed(MaxAdapterError.NO_FILL);
                                        }
                                    }
                                }).execute(map);

                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());

                                if (listener != null) {
                                    listener.onNativeAdLoadFailed(MaxAdapterError.INTERNAL_ERROR);
                                }
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            if (listener != null) {
                                listener.onNativeAdLoadFailed(MaxAdapterError.NO_FILL);
                            }
                        }

                        @Override
                        public void onAdShown() {

                        }

                        @Override
                        public void onAdClicked() {

                        }
                    });

                    mNativeAd.loadAd();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private class MaxAdPieNativeAd
            extends MaxNativeAd {

        private MaxNativeAdAdapterListener listener;
        private View observerView;
        private NativeAdData nativeAdData;

        private boolean mIsClicked;

        public MaxAdPieNativeAd(final Builder builder) {
            super(builder);
        }

        public MaxAdPieNativeAd(Builder builder, MaxNativeAdAdapterListener listener, NativeAdData nativeAdData) {
            super(builder);
            this.listener = listener;
            this.nativeAdData = nativeAdData;
        }

        @Override
        public void prepareViewForInteraction(final MaxNativeAdView maxNativeAdView) {
            try {
                ViewGroup optionViewGroup = maxNativeAdView.getOptionsContentViewGroup();
                if (optionViewGroup != null
                        && optionViewGroup.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) optionViewGroup.getLayoutParams();
                    layoutParams.width = DisplayUtil.dpToPx(maxNativeAdView.getContext(), 20);
                    layoutParams.height = DisplayUtil.dpToPx(maxNativeAdView.getContext(), 20);
                    optionViewGroup.setLayoutParams(layoutParams);
                }

                if (maxNativeAdView.getTitleTextView() != null) {
                    maxNativeAdView.getTitleTextView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClick(v);
                        }
                    });
                }

                if (maxNativeAdView.getBodyTextView() != null) {
                    maxNativeAdView.getBodyTextView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClick(v);
                        }
                    });
                }

                if (maxNativeAdView.getIconImageView() != null) {
                    maxNativeAdView.getIconImageView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClick(v);
                        }
                    });
                }

                if (maxNativeAdView.getMainView() != null) {
                    maxNativeAdView.getMainView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClick(v);
                        }
                    });
                }

                if (maxNativeAdView.getCallToActionButton() != null) {
                    maxNativeAdView.getCallToActionButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClick(v);
                        }
                    });
                }

                observerView = new View(maxNativeAdView.getContext());
                observerView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                maxNativeAdView.addView(observerView, 0);

                observerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                        if (observerView != null) {
                            observerView.removeOnAttachStateChangeListener(this);
                        }

                        ReportUtil.sendReport(ReportUtil.NATIVE_IMPRESSION_TAG, nativeAdData.getTrackingImpUrls()); // 임프레션 트래킹

                        if (listener != null) {
                            listener.onNativeAdDisplayed(null);
                        }
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {

                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        private void handleClick(View view) {
            try {
                ClickThroughUtil.goToBrowser(view.getContext(), nativeAdData.getLink());

                if (!mIsClicked) {
                    mIsClicked = true;

                    ReportUtil.sendReport(ReportUtil.NATIVE_CLICK_TAG, nativeAdData.getTrackingClkUrls()); //클릭 트래킹
                }

                if (listener != null) {
                    listener.onNativeAdClicked();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DrawableDownloadUtil extends AsyncTask<Object, Void, HashMap<String, Drawable>> {

        public static final String KEY_MAIN_IMAGE = "main_image_key";
        public static final String KEY_ICON = "icon_key";
        public static final String KEY_PRIVACY_ICON = "privacy_icon_key";
        private static final long DRAWABLE_FUTURE_TIMEOUT_MILLISECONDS = 3000;
        private final DrawableDownloadListener mListener;

        public DrawableDownloadUtil(DrawableDownloadListener listener) {
            mListener = listener;
        }

        @Override
        protected HashMap<String, Drawable> doInBackground(Object... params) {

            HashMap<String, URL> urlsMap = (HashMap<String, URL>) params[0];
            ExecutorService executorService = Executors.newCachedThreadPool();

            try {
                HashMap<String, Drawable> drawablesMap = new HashMap<>();

                if(urlsMap.containsKey(KEY_MAIN_IMAGE)) {
                    Future<Drawable> imageDrawableFuture =
                            getDrawableFuture(urlsMap.get(KEY_MAIN_IMAGE), executorService);

                    Drawable imageDrawable =
                            imageDrawableFuture.get(DRAWABLE_FUTURE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);

                    drawablesMap.put(KEY_MAIN_IMAGE, imageDrawable);
                }
                if(urlsMap.containsKey(KEY_ICON)) {
                    Future<Drawable> iconDrawableFuture =
                            getDrawableFuture(urlsMap.get(KEY_ICON), executorService);

                    Drawable iconDrawable =
                            iconDrawableFuture.get(DRAWABLE_FUTURE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);

                    drawablesMap.put(KEY_ICON, iconDrawable);
                }

                if(urlsMap.containsKey(KEY_PRIVACY_ICON)) {
                    Future<Drawable> privacyIconDrawableFuture =
                            getDrawableFuture(urlsMap.get(KEY_PRIVACY_ICON), executorService);

                    Drawable privacyIconDrawable =
                            privacyIconDrawableFuture.get(DRAWABLE_FUTURE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);

                    drawablesMap.put(KEY_PRIVACY_ICON, privacyIconDrawable);
                }

                return drawablesMap;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Log.d(TAG, "Native ad images failed to download. " + e.toString());
                return null;
            } catch (Exception e) {
                Log.d(TAG, "Native ad images failed to download. " + e.toString());
                return null;
            }
        }

        private Future<Drawable> getDrawableFuture(final URL url, ExecutorService executorService) {
            return executorService.submit(
                    new Callable<Drawable>() {
                        @Override
                        public Drawable call() throws Exception {
                            InputStream in = url.openStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(in);

                            bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
                            return new BitmapDrawable(Resources.getSystem(), bitmap);
                        }
                    });
        }

        @Override
        protected void onPostExecute(HashMap<String, Drawable> drawablesMap) {
            super.onPostExecute(drawablesMap);
            if (drawablesMap != null) {
                mListener.onDownloadSuccess(drawablesMap);
            } else {
                mListener.onDownloadFailure();
            }
        }
    }
}