package com.xxx.closep;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(){
            @Override
            public void run() {
                try {
                    finish();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startApplicationInfo(getForegroundApp());
            }
        }.start();

    }

    private void startApplicationInfo(String pageName){
        Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");

        String pkg = "com.android.settings";
        String cls = "com.android.settings.applications.InstalledAppDetails";

        i.setComponent(new ComponentName(pkg, cls));
        i.setData(Uri.parse("package:" + pageName));
        startActivity(i);
    }


    @SuppressLint("NewApi")
    private String getForegroundApp() {
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(new Date());
        long endt = calendar.getTimeInMillis();//结束时间
        calendar.add(Calendar.DAY_OF_MONTH, -1);//时间间隔为一个月
        long statt = calendar.getTimeInMillis();//开始时间
        UsageStatsManager usageStatsManager=(UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        //获取一个月内的信息
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY,statt,endt);

        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return null;
        }

        UsageStats recentStats = null;
        for (UsageStats usageStats : queryUsageStats) {

            if(recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()){
                recentStats = usageStats;
            }
        }

        return recentStats.getPackageName();
    }
}
