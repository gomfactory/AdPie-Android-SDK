/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.AdView;

public class BannerAdActivity extends AppCompatActivity {

    public static final String TAG = BannerAdActivity.class.getSimpleName();

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_banner);
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

        TextView tvSlotId = (TextView) findViewById(R.id.text_slot);
        tvSlotId.setText("Slot ID : " + getString(R.string.banner_sid));

        adView = (AdView) findViewById(R.id.ad_view);
        // Insert your AdPie-Slot-ID
        adView.setSlotId(getString(R.string.banner_sid));
        adView.setScaleUp(true);
        adView.setAdListener(new AdView.AdListener() {

            @Override
            public void onAdLoaded() {
                printMessage(BannerAdActivity.this, "AdView onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                printMessage(BannerAdActivity.this, "AdView onAdFailedToLoad "
                        + AdPieError.getMessage(errorCode));
            }

            @Override
            public void onAdClicked() {
                printMessage(BannerAdActivity.this, "AdView onAdClicked");
            }
        });

        adView.load();

    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }

        super.onDestroy();
    }

    public void printMessage(Context context, String message) {
        Log.d(TAG, message);

        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
