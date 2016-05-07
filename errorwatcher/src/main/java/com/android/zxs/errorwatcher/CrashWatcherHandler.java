package com.android.zxs.errorwatcher;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

/**
 * The MIT License (MIT)
 *
 * 作者：zxs on 2016/4/6 19:43
 * 邮箱：yihaoBeta@163.com
 *
 * 未捕获异常Handler
 *
 */
public class CrashWatcherHandler implements
        java.lang.Thread.UncaughtExceptionHandler {
    private final Activity activity;
    JSONObject jObjectLogMsg;
    String ActivityName;
    private OnErrorOccurredListener mListener;
    private static final String TAG = "CrashWatcherHandler";

    public CrashWatcherHandler(Activity activity) {
        this.activity = activity;
        ActivityName = activity.getClass().getSimpleName();
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        jObjectLogMsg = new JSONObject();
        try {
            jObjectLogMsg.put("PackageName", activity.getPackageName());
            jObjectLogMsg.put("Class", ActivityName);
            jObjectLogMsg.put("Brand", Build.BRAND);
            jObjectLogMsg.put("Device", Build.DEVICE);
            jObjectLogMsg.put("Model", Build.MODEL);
            jObjectLogMsg.put("Product", Build.PRODUCT);
            jObjectLogMsg.put("SDKVersion", Build.VERSION.SDK);
            jObjectLogMsg.put("ReleaseVersion", Build.VERSION.RELEASE);
            jObjectLogMsg.put("IncrementalVersion", Build.VERSION.INCREMENTAL);
            jObjectLogMsg.put("ScreenHeight", activity.getResources().getDisplayMetrics().heightPixels);
            jObjectLogMsg.put("ScreenWidth", activity.getResources().getDisplayMetrics().widthPixels);
            jObjectLogMsg.put("AppVersion", Utils.getAppVersion(activity));
            jObjectLogMsg.put("isTablet", Utils.isTablet(activity));
            jObjectLogMsg.put("Orientation", Utils.getScreenOrientation(activity));
            jObjectLogMsg.put("ScreenSize", Utils.getScreenSize(activity));
            jObjectLogMsg.put("VMHeapSize", Utils.ConvertSize(Runtime.getRuntime().totalMemory()));
            jObjectLogMsg.put("AllocatedVMSize", Utils.ConvertSize(Runtime.getRuntime().freeMemory()));
            jObjectLogMsg.put("MaxVMHeapSize", Utils.ConvertSize((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));
            jObjectLogMsg.put("VMFreeHeapSize", Utils.ConvertSize(Runtime.getRuntime().maxMemory()));
            jObjectLogMsg.put("NativeAllocatedSize", Utils.ConvertSize(Debug.getNativeHeapAllocatedSize()));
            jObjectLogMsg.put("BatteryPercentage", activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)).getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
            jObjectLogMsg.put("BatteryChargingStatus", Utils.getBatteryStatus(activity));
            jObjectLogMsg.put("BatteryChargingType", Utils.getBatteryChargingMode(activity));
            jObjectLogMsg.put("SDCardStatus", Utils.getSDCardStatus());
            jObjectLogMsg.put("InternalMemorySize", Utils.getTotalInternalMemorySize());
            jObjectLogMsg.put("ExternalMemorySize", Utils.getTotalExternalMemorySize());
            jObjectLogMsg.put("InternalFreeSpace", Utils.getAvailableInternalMemorySize());
            jObjectLogMsg.put("ExternalFreeSpace", Utils.getAvailableExternalMemorySize());
            jObjectLogMsg.put("isRooted", Utils.isRooted());
            jObjectLogMsg.put("Local", new Locale("", activity.getResources().getConfiguration().locale.getCountry()).getDisplayCountry());
            jObjectLogMsg.put("ErrorMessage", exception.getMessage());
            jObjectLogMsg.put("ErrorCause", exception.getCause());
            jObjectLogMsg.put("StackTrace", stackTrace.toString());
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception",e);
        }

        if (mListener != null) {
            mListener.errorOccurred(jObjectLogMsg);
        }

    }

    protected void setCallback(OnErrorOccurredListener listener) {
        this.mListener = listener;
    }

    protected interface OnErrorOccurredListener {
        void errorOccurred(JSONObject jsonObject);
    }
}
