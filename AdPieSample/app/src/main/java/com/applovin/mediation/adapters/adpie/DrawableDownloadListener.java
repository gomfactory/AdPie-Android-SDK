package com.applovin.mediation.adapters.adpie;

import android.graphics.drawable.Drawable;

import java.util.HashMap;

public interface DrawableDownloadListener {
    void onDownloadSuccess(HashMap<String, Drawable> drawableMap);

    void onDownloadFailure();
}