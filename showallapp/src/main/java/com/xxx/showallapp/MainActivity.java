package com.xxx.showallapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Long.parseLong;


//if (goodsList_drawerLayout != null && goodsList_drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
//        goodsList_drawerLayout.closeDrawer(Gravity.RIGHT);
//        return true;
//        }

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private List<ApplicationData> applicationDatas;
    private BaseAdapter adapter;
    private PackageManager mPackageManager;
    private SharedPreferences sharedPreferences;
    private View filterSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        filterSwitch = findViewById(R.id.filterSwitch);
        filterSwitch.setSelected(false);

        filterSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterSwitch.setSelected(!filterSwitch.isSelected());
            }
        });

        mPackageManager = getPackageManager();
        applicationDatas = new ArrayList<>();

        sharedPreferences = getSharedPreferences("filter", Context.MODE_PRIVATE);
        String filterArrStr = sharedPreferences.getString("filterArr", "");
        //得到包名集合
        final String[] filterArr = filterArrStr.split("-");
        Arrays.sort(filterArr, new Comparator<String>() {//根据时间排序
            @Override
            public int compare(String o1, String o2) {
                long o1Time = Long.parseLong(o1.split(",")[1]);
                long o2Time = parseLong(o2.split(",")[1]);
                if (o2Time == o1Time) return 0;
                return o2Time > o1Time ? 1 : -1;
            }
        });

        gridView = (GridView) findViewById(R.id.allAppList);
        gridView.setNumColumns(4);

        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return applicationDatas == null ? 0 : applicationDatas.size();
            }

            @Override
            public Object getItem(int position) {
                return applicationDatas.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final ApplicationData applicationData = applicationDatas.get(position);

                if (convertView == null) {
                    convertView = View.inflate(MainActivity.this, R.layout.item_app, null);
                }
                ImageView appImg = (ImageView) convertView.findViewById(R.id.appImg);
                TextView appName = (TextView) convertView.findViewById(R.id.appName);

                appImg.setImageDrawable(applicationData.getIcon());
                appName.setText(applicationData.getAppName());

                //单击开启
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!filterSwitch.isSelected()) {//选中就去过滤
                            String packageName = applicationData.getPackageName();
                            Intent i = mPackageManager.getLaunchIntentForPackage(packageName);
                            if (i != null) startActivity(i);
                        } else {
                            String spFA = sharedPreferences.getString("filterArr", "");
                            String itemStr = (TextUtils.isEmpty(spFA) ? "" : spFA + "-") + applicationData.getPackageName() + "," + applicationData.getUpdateTime() + "," + applicationData.getAppName();
                            sharedPreferences.edit().putString("filterArr", itemStr).apply();
                            applicationDatas.remove(position);
                            notifyDataSetChanged();
                        }
                    }
                });

                //长按打开应用信息
                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        startApplicationInfo(applicationData.getPackageName());
                        return true;
                    }
                });

//                //长按过滤
//                convertView.set(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//
//                        return true;
//                    }
//                });

                return convertView;
            }
        };
        gridView.setAdapter(adapter);

        new Thread() {
            @Override
            public void run() {
                PackageManager mPackageManager = MainActivity.this.getPackageManager();
                List<PackageInfo> packageInfos = mPackageManager
                        .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
                for (PackageInfo packs : packageInfos) {
                    if ((packs.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) ;
                    {
                        ApplicationData mApplicationData = new ApplicationData();
                        mApplicationData.setIcon(packs.applicationInfo
                                .loadIcon(mPackageManager));
                        mApplicationData.setAppName(packs.applicationInfo.loadLabel(
                                mPackageManager).toString());
                        mApplicationData.setPackageName(packs.packageName);
                        mApplicationData.setUpdateTime(packs.lastUpdateTime);
                        applicationDatas.add(mApplicationData);

                    }
                }

                Collections.sort(applicationDatas, new Comparator<ApplicationData>() {
                    @Override
                    public int compare(ApplicationData o1, ApplicationData o2) {
                        if (o2.getUpdateTime() == o1.getUpdateTime()) return 0;
                        return o2.getUpdateTime() > o1.getUpdateTime() ? 1 : -1;
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        for (int j = 0; j < filterArr.length; j++) {
                            for (; i < applicationDatas.size(); i++) {
                                if (TextUtils.equals(applicationDatas.get(i).getPackageName() + "", filterArr[j].split(",")[0])) {
                                    applicationDatas.remove(i);
//                                    i--;//不变，直接不更改
                                    break;//一个一个对应
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    private void startApplicationInfo(String pageName) {
        Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");

        String pkg = "com.android.settings";
        String cls = "com.android.settings.applications.InstalledAppDetails";

        i.setComponent(new ComponentName(pkg, cls));
        i.setData(Uri.parse("package:" + pageName));
        startActivity(i);
    }
}
