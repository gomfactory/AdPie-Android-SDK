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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gomfactory.adpie.sdk.AdPieError;
import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.NativeAd;
import com.gomfactory.adpie.sdk.nativeads.NativeAdData;
import com.gomfactory.adpie.sdk.nativeads.NativeAdView;
import com.gomfactory.adpie.sdk.nativeads.NativeAdViewBinder;

import java.util.ArrayList;

public class NativeAdActivity extends AppCompatActivity {

    public static final String TAG = NativeAdActivity.class.getSimpleName();

    private ListView listview = null;

    private NativeAd nativeAd = null;

    private int adIdx = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_native);
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
        tvSlotId.setText("Slot ID : " + getString(R.string.native_sid));

        ListViewAdapter adapter = new ListViewAdapter();
        listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);

        for (int i = 0; i < 20; i++) {
            adapter.addItem("Item " + (i + 1) + "");
        }

        NativeAdViewBinder viewBinder = new NativeAdViewBinder.Builder(R.layout.adpie_native_ad_template)
                .setTitleId(R.id.native_ad_title)
                .setDescriptionId(R.id.native_ad_description)
                .setMainId(R.id.native_ad_main)
                .setIconImageId(R.id.native_ad_icon)
                .setCallToActionId(R.id.native_ad_cta)
                .setOptOutId(R.id.native_optout)
                .build();

        nativeAd = new NativeAd(this, viewBinder);
        nativeAd.setSlotId(getString(R.string.native_sid));
        nativeAd.setAdListener(new NativeAd.AdListener() {
            @Override
            public void onAdLoaded(final NativeAdView nativeAdView) {
                printMessage(NativeAdActivity.this, "NativeAdView onAdLoaded");

                if (nativeAdView.isResourceLoaded()) {
                    // 기본 값으로 이미지 관련 리소스 로딩 완료
                    // 이미지 관련 리소스 로딩 완료

                    ((ListViewAdapter) listview.getAdapter()).addItem(adIdx, nativeAdView);
                    ((ListViewAdapter) listview.getAdapter()).notifyDataSetChanged();
                } else {
                    // nativeAd.setSkipDownload(true)를 호출한 경우로 이미지 다운로드 필요
                    // 이미지 다운로드 및 설정이 필요

                    {
                        // 방법 1 : SDK 통한 이미지 다운로드

                        nativeAdView.downloadResource(new NativeAdView.ResourceLoadEventListener() {
                            @Override
                            public void onSuccess() {
                                ((ListViewAdapter) listview.getAdapter()).addItem(adIdx, nativeAdView);
                                ((ListViewAdapter) listview.getAdapter()).notifyDataSetChanged();
                            }

                            @Override
                            public void onError() {
                                printMessage(NativeAdActivity.this, "NativeAdView resource download failed.");
                            }
                        });
                    }


                    {
                        // 방법 2 : SDK 를 통하지 않고 매체에서 별도로 이미지 로딩을 하는 경우 별도로 설정가능

                        NativeAdData nativeAdData = nativeAdView.getNativeAdData();
                        String iconImageUrl = nativeAdData.getIconImageUrl();
                        ImageView iconImageView = nativeAdView.getIconImageView();
                        String mainImageUrl = nativeAdData.getMainImageUrl();
                        ImageView mainImageView = nativeAdView.getMainImageView();
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                printMessage(NativeAdActivity.this, "NativeAdView onAdFailedToLoad "
                        + AdPieError.getMessage(errorCode));
            }

            @Override
            public void onAdShown() {
                printMessage(NativeAdActivity.this, "NativeAdView onAdShown");
            }

            @Override
            public void onAdClicked() {
                printMessage(NativeAdActivity.this, "NativeAdView onAdClicked");
            }
        });

        nativeAd.loadAd();
    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }

        super.onDestroy();
    }

    public void printMessage(Context context, String message) {
        Log.d(TAG, message);

        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    private class ListViewAdapter extends BaseAdapter {

        private ArrayList<Object> listViewItemList = new ArrayList<>();

        public ListViewAdapter() {
        }

        @Override
        public int getCount() {
            return listViewItemList.size();
        }

        @Override
        public Object getItem(int location) {
            return listViewItemList.get(location);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item, parent, false);
            }

            ViewGroup adContainer = convertView.findViewById(R.id.ad_container);
            TextView textView = convertView.findViewById(R.id.text_view);
            adContainer.removeAllViews();

            if (listViewItemList.get(position) != null) {
                if (listViewItemList.get(position) instanceof NativeAdView) {
                    adContainer.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);

                    try {
                        NativeAdView nativeAdView = (NativeAdView) listViewItemList.get(position);
                        if (nativeAdView.getParent() != null) {
                            adContainer.addView(nativeAdView.copy());
                        } else {
                            adContainer.addView(nativeAdView);
                        }
                    } catch (Exception e) {
                    }
                } else {
                    adContainer.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);

                    String listViewItem = (String) listViewItemList.get(position);
                    textView.setText(listViewItem);
                }
            }

            return convertView;
        }

        public void addItem(Object item) {
            listViewItemList.add(item);
        }

        public void addItem(int index, Object item) {
            if (listViewItemList.get(index) instanceof NativeAdView) {
                listViewItemList.remove(index);
            }
            listViewItemList.add(index, item);
        }
    }
}