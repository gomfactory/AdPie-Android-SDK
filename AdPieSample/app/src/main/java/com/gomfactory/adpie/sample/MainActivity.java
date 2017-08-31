/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gomfactory.adpie.sdk.AdPieSDK;
import com.gomfactory.adpie.sdk.DialogAd;
import com.gomfactory.adpie.sdk.dialog.DialogStyle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private long lastTimeSelected = 0;

    private DialogAd dialogAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Insert your AdPie-Media-ID
        AdPieSDK.getInstance().initialize(getApplicationContext(), getString(R.string.mid));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListViewAdapter adapter = new ListViewAdapter();
        ListView listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);

        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.adpie_logo),
                "Banner Ad", "배너 광고");
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.adpie_logo),
                "Interstitial Ad", "전면 광고");
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.adpie_logo),
                "Native Ad", "네이티브 광고");
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.adpie_logo),
                "Video Ad", "비디오 광고 (Pre-Roll / FullScreen)");

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Long now = System.currentTimeMillis();
                if ((now - lastTimeSelected) > 500) {
                    lastTimeSelected = now;
                } else {
                    return;
                }

                Intent intent = null;
                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, BannerAdActivity.class);
                        MainActivity.this.startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, InterstitialAdActivity.class);
                        MainActivity.this.startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, NativeAdActivity.class);
                        MainActivity.this.startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(MainActivity.this, VideoAdActivity.class);
                        MainActivity.this.startActivity(intent);
                        break;
                }
            }
        });

        DialogStyle dialogStyle = new DialogStyle.Builder()
                .setAdSize(DialogStyle.DIALOG_SIZE_250x250) // 필수
                .setDefaultImageResId(R.drawable.coffee_500x500) // 필수
                .build();

        dialogAd = new DialogAd(MainActivity.this, dialogStyle, getString(R.string.dialog_sid_250x250));
        dialogAd.setDialogAdListenr(new DialogAd.DialogAdListener() {
            @Override
            public void onFirstButtonClicked() {
            }

            @Override
            public void onSecondButtonClicked() {
                // 앱 종료
                finish();
            }

            @Override
            public void onThirdButtonClicked() {
            }

            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

            }

            @Override
            public void onAdClicked() {

            }
        });
        dialogAd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                // 다이얼로그 취소 이벤트 발생
            }
        });
        dialogAd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // 다이얼로그 종료 이벤트 발생
            }
        });
        dialogAd.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // 다이얼로그 표출 이벤트 발생
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (dialogAd != null) {
            // 다이얼로그 종료
            dialogAd.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (dialogAd != null) {
            // 다이얼로그 표출
            dialogAd.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 회전 앱의 경우 필수 구현
        if (dialogAd != null) {
            dialogAd.dismiss();
        }
    }

    private class ListViewItem {
        private Drawable iconDrawable;
        private String titleStr;
        private String descStr;

        public void setIcon(Drawable icon) {
            iconDrawable = icon;
        }

        public void setTitle(String title) {
            titleStr = title;
        }

        public void setDesc(String desc) {
            descStr = desc;
        }

        public Drawable getIcon() {
            return this.iconDrawable;
        }

        public String getTitle() {
            return this.titleStr;
        }

        public String getDesc() {
            return this.descStr;
        }
    }

    private class ListViewAdapter extends BaseAdapter {
        private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();

        public ListViewAdapter() {
        }

        @Override
        public int getCount() {
            return listViewItemList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.productlist_item, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.text_first);
            TextView descTextView = (TextView) convertView.findViewById(R.id.text_second);

            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            ListViewItem listViewItem = listViewItemList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            iconImageView.setImageDrawable(listViewItem.getIcon());
            titleTextView.setText(listViewItem.getTitle());
            descTextView.setText(listViewItem.getDesc());

            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position);
        }

        public void addItem(Drawable icon, String title, String desc) {
            ListViewItem item = new ListViewItem();

            item.setIcon(icon);
            item.setTitle(title);
            item.setDesc(desc);

            listViewItemList.add(item);
        }
    }
}
