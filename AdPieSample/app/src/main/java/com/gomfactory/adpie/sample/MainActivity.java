/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.AdView;
import com.gomfactory.adpie.sdk.InterstitialAd;

public class MainActivity extends AppCompatActivity
        implements InterstitialAd.InterstitialAdListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private AdView adView;
    private InterstitialAd interstitialAd;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Insert your AdPie-Media-ID
        AdPieSDK.getInstance().initialize(getApplicationContext(), "56b050727174ea1199cf8ed1");

        // Insert your AdPie-Slot-ID
        interstitialAd = new InterstitialAd(this, "56b050727174ea1199cf8ed3");
        interstitialAd.setAdListener(this);

        setContentView(R.layout.activity_main);

        adView = (AdView) findViewById(R.id.adView);
        adView.setAdListener(new com.gomfactory.adpie.sdk.AdView.AdListener() {

            @Override
            public void onAdLoaded() {
                printMessage(MainActivity.this, "AdView onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                printMessage(MainActivity.this, "AdView onAdFailedToLoad "
                        + AdPieError.getMessage(errorCode));
            }

            @Override
            public void onAdClicked() {
                printMessage(MainActivity.this, "AdView onAdClicked");
            }
        });

        adView.load();

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interstitialAd.load();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }

        super.onDestroy();
    }

    @Override
    public void onAdLoaded() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        printMessage(MainActivity.this, "Interstitial onAdFailedToLoad "
                + AdPieError.getMessage(errorCode));
    }

    @Override
    public void onAdShown() {
        printMessage(MainActivity.this, "Interstitial onAdShown");
    }

    @Override
    public void onAdClicked() {
        printMessage(MainActivity.this, "Interstitial onAdClicked");
    }

    @Override
    public void onAdDismissed() {
        printMessage(MainActivity.this, "Interstitial onAdDismissed");
    }


    public void printMessage(Context context, String message) {
        Log.d(TAG, message);

        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
