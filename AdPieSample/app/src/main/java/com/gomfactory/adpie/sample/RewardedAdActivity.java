/*******************************************************************************
 * Copyright (c) 2017 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.RewardedAd;

public class RewardedAdActivity extends AppCompatActivity {

    public static final String TAG = RewardedAdActivity.class.getSimpleName();

    private RewardedAd rewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rewarded);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvName = (TextView) findViewById(R.id.text_app_name);
        tvName.setText(getString(R.string.app_name));

        TextView tvVersion = (TextView) findViewById(R.id.text_version);
        tvVersion.setText("AdPie SDK Version : " + AdPieSDK.getInstance().getVersion());

        TextView tvMediaId = (TextView) findViewById(R.id.text_media_id);
        tvMediaId.setText("Media ID : " + getString(R.string.mid));

        TextView tvRewardedVideoSlotId = (TextView) findViewById(R.id.text_slot);
        tvRewardedVideoSlotId.setText("Slot ID : " + getString(R.string.rewarded_video_sid));

        // Insert your AdPie-Slot-ID
        rewardedVideoAd = new RewardedAd(this, getString(R.string.rewarded_video_sid));
        rewardedVideoAd.setAdListener(new RewardedAd.RewardedAdListener() {
            @Override
            public void onAdLoaded() {
                printMessage(RewardedAdActivity.this, "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                printMessage(RewardedAdActivity.this, "onAdFailedToLoad");
            }

            @Override
            public void onAdShown() {
                printMessage(RewardedAdActivity.this, "onAdShown");
            }

            @Override
            public void onAdClicked() {
                printMessage(RewardedAdActivity.this, "onAdClicked");
            }

            @Override
            public void onAdDismissed() {
                printMessage(RewardedAdActivity.this, "onAdDismissed");
            }

            @Override
            public void onAdRewarded() {
                printMessage(RewardedAdActivity.this, "onAdRewarded");
            }

            @Override
            public void onAdFailedToShow() {
                printMessage(RewardedAdActivity.this, "onAdFailedToShow");
            }
        });

        // Insert your SSV User Id (Optional)
        rewardedVideoAd.setUserIdForSSV("");
        // Insert your SSV Custom Data (Optional)
        rewardedVideoAd.setCustomDataForSSV("");

        Button btnRvLoad = (Button) findViewById(R.id.button_rv_load);
        btnRvLoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (rewardedVideoAd != null) {
                    rewardedVideoAd.load();
                }
            }
        });

        Button btnRvShow = (Button) findViewById(R.id.button_rv_show);
        btnRvShow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (rewardedVideoAd.isLoaded()) {
                    rewardedVideoAd.show();
                } else {
                    printMessage(RewardedAdActivity.this, "Not ready!");
                }
            }
        });
    }

    public void printMessage(Context context, String message) {
        Log.d(TAG, message);

        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
            rewardedVideoAd = null;
        }

        super.onDestroy();
    }
}
