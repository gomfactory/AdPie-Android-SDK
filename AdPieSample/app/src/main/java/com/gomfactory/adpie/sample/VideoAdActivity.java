/*******************************************************************************
 * Copyright (c) 2017 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.InterstitialAd;
import com.gomfactory.adpie.sdk.PrerollVideoAd;
import com.gomfactory.adpie.sdk.videoads.VideoAdPlaybackListener;
import com.gomfactory.adpie.sdk.videoads.VideoAdView;

public class VideoAdActivity  extends AppCompatActivity {

    public static final String TAG = VideoAdActivity.class.getSimpleName();

    private InterstitialAd interstitialAd;

    private PrerollVideoAd prerollVideoAd;
    private VideoAdView videoAdView;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        toolbar.setTitle("Video Ad");

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView tvName = (TextView) findViewById(R.id.text_app_name);
        tvName.setText(getString(R.string.app_name));

        TextView tvVersion = (TextView) findViewById(R.id.text_version);
        tvVersion.setText("AdPie SDK Version : " + AdPieSDK.getInstance().getVersion());

        TextView tvMediaId = (TextView) findViewById(R.id.text_media_id);
        tvMediaId.setText("Media ID : " + getString(R.string.mid));

        TextView tvPrerollSlotId = (TextView) findViewById(R.id.text_preroll_slot);
        tvPrerollSlotId.setText("Pre-Roll Slot ID : " + getString(R.string.preroll_video_sid));

        TextView tvInterstitialSlotId = (TextView) findViewById(R.id.text_interstitial_slot);
        tvInterstitialSlotId.setText("Interstitial Slot ID : " + getString(R.string.interstitial_video_sid));

        videoView = (VideoView) findViewById(R.id.video_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            videoView.animate().alpha(1);
        }

        videoAdView = (VideoAdView) findViewById(R.id.video_adview);

        prerollVideoAd = new PrerollVideoAd(this, getString(R.string.preroll_video_sid), videoAdView);
        prerollVideoAd.setAdListener(new PrerollVideoAd.AdListener() {
            @Override
            public void onAdLoaded() {
                if (prerollVideoAd.isLoaded()) {
                    videoView.setVisibility(View.GONE);
                    prerollVideoAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

                printMessage(VideoAdActivity.this, "Preroll onAdFailedToLoad "
                        + AdPieError.getMessage(errorCode));

                playCustomVideo();
            }

            @Override
            public void onAdClicked() {
                printMessage(VideoAdActivity.this, "Preroll onAdClicked");
            }
        });

        prerollVideoAd.setVideoAdPlaybackListener(new VideoAdPlaybackListener() {
            @Override
            public void onVideoAdStarted() {
                printMessage(VideoAdActivity.this, "Preroll onVideoAdStarted");
            }

            @Override
            public void onVideoAdPaused() {
                printMessage(VideoAdActivity.this, "Preroll onVideoAdPaused");
            }

            @Override
            public void onVideoAdStopped() {
                printMessage(VideoAdActivity.this, "Preroll onVideoAdStopped");
                playCustomVideo();
            }

            @Override
            public void onVideoAdSkipped() {
                printMessage(VideoAdActivity.this, "Preroll onVideoAdSkipped");
                playCustomVideo();
            }

            @Override
            public void onVideoAdError() {
                printMessage(VideoAdActivity.this, "Preroll onVideoAdError");
                playCustomVideo();
            }

            @Override
            public void onVideoAdCompleted() {
                printMessage(VideoAdActivity.this, "Preroll onVideoAdCompleted");
                playCustomVideo();
            }
        });

        Button btnPrerollPlay = (Button) findViewById(R.id.button_preroll_play);
        btnPrerollPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (videoView.isPlaying() || prerollVideoAd.isPlaying()) {
                    Toast.makeText(VideoAdActivity.this, "Wait until video is finished.", Toast.LENGTH_SHORT).show();
                    return;
                }

                prerollVideoAd.load();
            }
        });

        // Insert your AdPie-Slot-ID
        interstitialAd = new InterstitialAd(this, getString(R.string.interstitial_video_sid));
        interstitialAd.setAdListener(new InterstitialAd.InterstitialAdListener() {

            @Override
            public void onAdLoaded() {
                printMessage(VideoAdActivity.this, "Interstitial Video onAdLoaded");

                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                printMessage(VideoAdActivity.this, "Interstitial Video onAdFailedToLoad "
                        + AdPieError.getMessage(errorCode));
            }

            @Override
            public void onAdShown() {
                printMessage(VideoAdActivity.this, "Interstitial Video onAdShown");
            }

            @Override
            public void onAdClicked() {
                printMessage(VideoAdActivity.this, "Interstitial Video onAdClicked");
            }

            @Override
            public void onAdDismissed() {
                printMessage(VideoAdActivity.this, "Interstitial Video onAdDismissed");
            }
        });

        interstitialAd.setVideoAdPlaybackListener(new VideoAdPlaybackListener() {
            @Override
            public void onVideoAdStarted() {
                printMessage(VideoAdActivity.this, "Interstitial onVideoAdStarted");
            }

            @Override
            public void onVideoAdPaused() {
                printMessage(VideoAdActivity.this, "Interstitial onVideoAdPaused");
            }

            @Override
            public void onVideoAdStopped() {
                printMessage(VideoAdActivity.this, "Interstitial onVideoAdStopped");
            }

            @Override
            public void onVideoAdSkipped() {
                printMessage(VideoAdActivity.this, "Interstitial onVideoAdSkipped");
            }

            @Override
            public void onVideoAdError() {
                printMessage(VideoAdActivity.this, "Interstitial onVideoAdError");
            }

            @Override
            public void onVideoAdCompleted() {
                printMessage(VideoAdActivity.this, "Interstitial onVideoAdCompleted");
            }
        });

        Button btnInterstitialPlay = (Button) findViewById(R.id.button_inters_play);
        btnInterstitialPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                interstitialAd.load();
            }
        });
    }

    public void playCustomVideo() {
        videoView.setVisibility(View.VISIBLE);

        String path = "android.resource://" + getPackageName() + "/" + R.raw.sample;
        Uri uri = Uri.parse(path);
        videoView.setVideoURI(uri);

        videoView.start();
    }

    public void printMessage(Context context, String message) {
        Log.d(TAG, message);

        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (prerollVideoAd != null) {
            prerollVideoAd.destroy();
            prerollVideoAd = null;
        }

        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }

        super.onDestroy();
    }
}
