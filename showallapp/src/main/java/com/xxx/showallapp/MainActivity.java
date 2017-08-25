package com.xxx.showallapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


//if (goodsList_drawerLayout != null && goodsList_drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
//        goodsList_drawerLayout.closeDrawer(Gravity.RIGHT);
//        return true;
//        }

public class MainActivity extends AppCompatActivity {

    private final static String rootDir = "show_allAPP";
    private GridView gridView;
    private List<ApplicationData> applicationDatas;
    private BaseAdapter adapter;
    private PackageManager mPackageManager;
    private View filterSwitch;
    private String[] filterArr;

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

        init();
    }

    public void init() {
        mPackageManager = getPackageManager();
        applicationDatas = new ArrayList<>();


        String filterArrStr = readFile(getDir(rootDir) + File.separator + "filter.txt");
        if (filterArrStr.length() > 0) {
            try {
                filterArrStr = filterArrStr.substring("--_v_--".length());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //得到包名集合
        filterArr = filterArrStr.split("--_v_--");

//        Arrays.sort(filterArr, new Comparator<String>() {//根据时间排序
//            @Override
//            public int compare(String o1, String o2) {
//                try {
//                    long o1Time = parseLong(o1.split(",_,")[1]);
//                    long o2Time = parseLong(o2.split(",_,")[1]);
//                    if (o2Time == o1Time) return 0;
//                    return o2Time > o1Time ? 1 : -1;
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                }
//                return 0;
//            }
//        });

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

                            //添加
                            writeFile(("--_v_--" + applicationData.getPackageName() + ",_," + /*applicationData.getUpdateTime() + ",_," + */applicationData.getAppName() + "\n").getBytes(), getDir(rootDir) + File.separator + "filter.txt", true);
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
    }

    private void refresh() {
        applicationDatas.clear();
        new Thread() {
            @Override
            public void run() {
                PackageManager mPackageManager = MainActivity.this.getPackageManager();
                List<PackageInfo> packageInfos = mPackageManager
                        .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
                for (PackageInfo packs : packageInfos) {
                    if (true || (packs.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        boolean filterFlag = true;//两次遍历有问题，暂时这样解决  用置空稍微提高下速度，只在第一次打开才更新过滤
                        for (int i = 0; i < filterArr.length; i++) {
                            if (filterArr[i] != null && TextUtils.equals(packs.packageName, filterArr[i].split(",_,")[0])){
                                filterArr[i] = null;
                                filterFlag = false;
                                break;
                            }
                        }
                        if (filterFlag){
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
//                        int i = 0;
//                        for (int j = 0; j < filterArr.length; j++) {
//                            for (; i < applicationDatas.size(); i++) {
//                                if (TextUtils.equals(applicationDatas.get(i).getPackageName() + "", filterArr[j].split(",_,")[0])) {
//                                    applicationDatas.remove(i);
////                                    i--;//不变，直接不更改
//                                    break;//一个一个对应
//                                }
//                            }
//                        }
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

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    /**
     * 读取txt文件的内容
     *
     * @param path 想要读取的文件路径
     * @return 返回文件内容
     */
    public static String readFile(String path) {

        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                result.append(s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * 把字符串数据写入文件
     *
     * @param content 需要写入的字符串
     * @param path    文件路径名称
     * @param append  是否以添加的模式写入
     * @return 是否写入成功
     */
    public static boolean writeFile(byte[] content, String path, boolean append) {
        boolean res = false;
        File f = new File(path);
        RandomAccessFile raf = null;
        try {
            if (f.exists()) {
                if (!append) {
                    f.delete();
                    f.createNewFile();
                }
            } else {
                f.createNewFile();
            }
            if (f.canWrite()) {
                raf = new RandomAccessFile(f, "rw");
                raf.seek(raf.length());
                raf.write(content);
                res = true;
            }
        } catch (Exception e) {
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                }
            }
        }
        return res;
    }


    /**
     * 获取应用目录，当SD卡存在时，获取SD卡上的目录，当SD卡不存在时，获取应用的cache目录
     */
    public static String getDir(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(getExternalStoragePath());
        sb.append(name);
        sb.append(File.separator);
        String path = sb.toString();
        if (createDirs(path)) {
            return path;
        } else {
            return null;
        }
    }

    /**
     * 创建文件夹
     */
    public static boolean createDirs(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 获取SD下的应用目录
     */
    public static String getExternalStoragePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(rootDir);
        sb.append(File.separator);
        createDirs(sb.toString());
        return sb.toString();
    }
}
