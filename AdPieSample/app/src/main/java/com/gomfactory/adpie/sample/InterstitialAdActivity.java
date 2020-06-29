/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.InterstitialAd;

public class InterstitialAdActivity extends AppCompatActivity
        implements InterstitialAd.InterstitialAdListener {

    public static final String TAG = InterstitialAdActivity.class.getSimpleName();

    private InterstitialAd interstitialAd;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_interstitial);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        toolbar.setTitle("Interstitial Ad");

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
        tvSlotId.setText("Slot ID : " + getString(R.string.interstitial_sid));

        // Insert your AdPie-Slot-ID
        interstitialAd = new InterstitialAd(this, getString(R.string.interstitial_sid));
        interstitialAd.setAdListener(this);

        button = (Button) findViewById(R.id.button_interstitial_ad);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interstitialAd.load();
            }
        });
    }

    @Override
    protected void onDestroy() {

        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }

        super.onDestroy();
    }

    @Override
    public void onAdLoaded() {
        printMessage(InterstitialAdActivity.this, "Interstitial onAdLoaded");

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        printMessage(InterstitialAdActivity.this, "Interstitial onAdFailedToLoad "
                + AdPieError.getMessage(errorCode));
    }

    @Override
    public void onAdShown() {
        printMessage(InterstitialAdActivity.this, "Interstitial onAdShown");
    }

    @Override
    public void onAdClicked() {
        printMessage(InterstitialAdActivity.this, "Interstitial onAdClicked");
    }

    @Override
    public void onAdDismissed() {
        printMessage(InterstitialAdActivity.this, "Interstitial onAdDismissed");
    }


    public void printMessage(Context context, String message) {
        Log.d(TAG, message);

        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
