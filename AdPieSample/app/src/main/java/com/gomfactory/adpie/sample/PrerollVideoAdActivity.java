/*******************************************************************************
 * Copyright (c) 2017 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.PrerollVideoAd;
import com.gomfactory.adpie.sdk.videoads.FinishState;
import com.gomfactory.adpie.sdk.videoads.VideoAdPlaybackListener;
import com.gomfactory.adpie.sdk.videoads.VideoAdView;

public class PrerollVideoAdActivity extends AppCompatActivity {

    public static final String TAG = PrerollVideoAdActivity.class.getSimpleName();

    private PrerollVideoAd prerollVideoAd;
    private VideoAdView videoAdView;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preroll_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        toolbar.setTitle("Pre-Roll Video Ad");

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

        TextView tvPrerollSlotId = (TextView) findViewById(R.id.text_slot);
        tvPrerollSlotId.setText("Slot ID : " + getString(R.string.preroll_video_sid));

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

                printMessage(PrerollVideoAdActivity.this, "Preroll onAdFailedToLoad "
                        + AdPieError.getMessage(errorCode));

                playCustomVideo();
            }

            @Override
            public void onAdClicked() {
                printMessage(PrerollVideoAdActivity.this, "Preroll onAdClicked");
            }
        });

        prerollVideoAd.setVideoAdPlaybackListener(new VideoAdPlaybackListener() {
            @Override
            public void onVideoAdStarted() {
                printMessage(PrerollVideoAdActivity.this, "Preroll onVideoAdStarted");
            }

            @Override
            public void onVideoFinished(FinishState finishState) {
                printMessage(PrerollVideoAdActivity.this, "Preroll onVideoFinished : " + finishState);

                switch (finishState) {
                    case ERROR:
                    case SKIPPED:
                    case COMPLETED:
                        playCustomVideo();
                        break;
                }
            }
        });

        Button btnPrerollPlay = (Button) findViewById(R.id.button_preroll_play);
        btnPrerollPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (prerollVideoAd != null) {
                    if (videoView.isPlaying() || prerollVideoAd.isPlaying()) {
                        Toast.makeText(PrerollVideoAdActivity.this, "Wait until video is finished.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    prerollVideoAd.load();
                } else {
                    playCustomVideo();
                }
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

        super.onDestroy();
    }
}
