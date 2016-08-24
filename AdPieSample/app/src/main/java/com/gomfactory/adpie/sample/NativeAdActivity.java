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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_native);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        toolbar.setTitle("Native Ad");

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView tvName = (TextView) findViewById(R.id.tvName);
        tvName.setText(getString(R.string.app_name));

        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvVersion.setText("AdPie SDK Version : " + AdPieSDK.getInstance().getVersion());

        TextView tvMediaId = (TextView) findViewById(R.id.tvMediaId);
        tvMediaId.setText("Media ID : " + getString(R.string.mid));

        TextView tvSlotId = (TextView) findViewById(R.id.tvSlotId);
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

                    ((ListViewAdapter) listview.getAdapter()).addItem(10, nativeAdView);
                    ((ListViewAdapter) listview.getAdapter()).notifyDataSetChanged();
                }else{
                    // nativeAd.setSkipDownload(true)를 호출한 경우로 이미지 다운로드 필요
                    // 이미지 다운로드 및 설정이 필요

                    {
                        // 방법 1 : SDK 통한 이미지 다운로드

                        nativeAdView.downloadResource(new NativeAdView.ResourceLoadEventListener() {
                            @Override
                            public void onSuccess() {
                                ((ListViewAdapter) listview.getAdapter()).addItem(10, nativeAdView);
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
        if(nativeAd != null){
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

        private static final int ITEM_VIEW_TYPE_CONTENT = 0;
        private static final int ITEM_VIEW_TYPE_AD = 1;
        private static final int ITEM_VIEW_TYPE_MAX = 2;

        private ArrayList<Object> listViewItemList = new ArrayList<>();

        public ListViewAdapter() {
        }

        @Override
        public int getItemViewType(int position) {

            if (listViewItemList.get(position) instanceof NativeAdView) {
                return ITEM_VIEW_TYPE_AD;
            }

            return ITEM_VIEW_TYPE_CONTENT;
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_VIEW_TYPE_MAX;
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
            int type = getItemViewType(position);

            if (type == ITEM_VIEW_TYPE_CONTENT) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                }

                String listViewItem = (String) listViewItemList.get(position);

                TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
                textView.setText(listViewItem.toString());
            } else if (type == ITEM_VIEW_TYPE_AD) {

                NativeAdView nativeAdView = (NativeAdView) listViewItemList.get(position);
                return nativeAdView;

            }

            return convertView;
        }

        public void addItem(Object item) {
            listViewItemList.add(item);
        }

        public void addItem(int index, Object item) {
            listViewItemList.add(index, item);
        }
    }
}