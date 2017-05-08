/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

        setContentView(R.layout.activity_banner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        toolbar.setTitle("Banner Ad");

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

        TextView tvSlotId = (TextView) findViewById(R.id.text_slot);
        tvSlotId.setText("Slot ID : " + getString(R.string.banner_sid));

        adView = (AdView) findViewById(R.id.ad_view);
        // Insert your AdPie-Slot-ID
        adView.setSlotId(getString(R.string.banner_sid));
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
