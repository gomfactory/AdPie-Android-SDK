/*******************************************************************************
 * Copyright (c) 2016 GomFactory, Inc. All Rights Reserved.
 ******************************************************************************/

package com.gomfactory.adpie.sample;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private long lastTimeSelected = 0;

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
                }
            }
        });
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
            TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1);
            TextView descTextView = (TextView) convertView.findViewById(R.id.textView2);

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
