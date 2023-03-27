package com.applovin.mediation.adapters.adpie;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

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

public class DrawableDownloadUtil extends AsyncTask<Object, Void, HashMap<String, Drawable>> {

    public static final String KEY_MAIN_IMAGE = "main_image_key";
    public static final String KEY_ICON = "icon_key";
    public static final String KEY_PRIVACY_ICON = "privacy_icon_key";
    private static final long DRAWABLE_FUTURE_TIMEOUT_MILLISECONDS = 3000;
    private static final String TAG = DrawableDownloadUtil.class.getSimpleName();
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
