package com.android.zxs.errorwatcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.text.DecimalFormat;

/**
 * The MIT License (MIT)
 *
 * 作者：zxs on 2016/5/6 19:43
 * 邮箱：yihaoBeta@163.com
 *
 * 工具类，提供系统相关信息的查询操作
 *
 */
public class Utils {
    private static final String TAG = "Utils";

    public static String getAppVersion(Context con) {
        PackageManager manager = con.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(con.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Name not found Exception");
            return null;
        }
        return info.versionName;
    }

    public static boolean isTablet(Context con) {
        boolean xlarge = ((con.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((con.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return xlarge || large;
    }

    @SuppressWarnings("deprecated")
    public static boolean isInternetConnecting(Context context) {
        boolean isConnecting = false;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getSimState() != TelephonyManager.SIM_STATE_UNKNOWN) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED))
                isConnecting = true;
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED))
                isConnecting = true;
        }
        return isConnecting;
    }

    @SuppressWarnings("deprecation")
    public static String getScreenOrientation(Activity act) {
        Display getOrient = act.getWindowManager().getDefaultDisplay();
        if (getOrient.getWidth() == getOrient.getHeight()) {
            return "Square";
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                return "Portrait";
            } else {
                return "Landscape";
            }
        }
    }

    public static String getScreenSize(Activity act) {
        int screenSize = act.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "Large";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "Normal";
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "Small";
            default:
                return "Unknown";
        }
    }

    public static String ConvertSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getBatteryStatus(Activity act) {
        int status = act.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)).getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == BatteryManager.BATTERY_STATUS_CHARGING)
            return "Charging";
        else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING)
            return "Discharging";
        else if (status == BatteryManager.BATTERY_STATUS_FULL)
            return "Full";
        return "NULL";
    }

    public static String getBatteryChargingMode(Activity act) {
        int plugged = act.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)).getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC)
            return "AC";
        else if (plugged == BatteryManager.BATTERY_PLUGGED_USB)
            return "USB";
        else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS)
            return "WireLess";
        return "NULL";
    }

    public static String getSDCardStatus() {
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (isSDPresent)
            return "Mounted";
        else
            return "Not mounted";
    }


    @SuppressWarnings("deprecation")
    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return ConvertSize(availableBlocks * blockSize);
    }


    @SuppressWarnings("deprecation")
    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return ConvertSize(totalBlocks * blockSize);
    }

    @SuppressWarnings("deprecation")
    public static String getAvailableExternalMemorySize() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return ConvertSize(availableBlocks * blockSize);
        } else {
            return "SDCard not present";
        }
    }

    @SuppressWarnings("deprecation")
    public static String getTotalExternalMemorySize() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return ConvertSize(totalBlocks * blockSize);
        } else {
            return "SDCard not present";
        }
    }

    public static boolean isRooted() {
        boolean found = false;
        if (!found) {
            String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                    "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
            for (String where : places) {
                if (new File(where + "su").exists()) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

}
