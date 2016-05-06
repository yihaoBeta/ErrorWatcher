package com.android.zxs.errorwatcher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * The MIT License (MIT)
 *
 * 作者：zxs on 2016/5/6 19:43
 * 邮箱：yihaoBeta@163.com
 *
 * ErrorWatcher主类，开放接口供调用
 *
 */
public class ErrorWatcher {


    private static final String DEFAULT_FILE_PREFIX = "error_watcher";
    private static final String TAG = "ErrorWatcher";
    private Context mContext;
    private Class<?> mClass;
    private ErrorMsgHandleMode mErrorMsgHandleMode = ErrorMsgHandleMode.LOG;
    //上传服务器的url
    private String mUrl;
    //打印log的tag
    private String mLogTag;
    //保存log文件的文件名前缀
    private String mLogFilePrefix;

    private OnCrashCatchListener mOnCrashCatchListener;

    //post key
    private String mPostValueKey;

    /**
     * 构造函数---
     * 鉴于crash发生的不确定性，建议在app刚开始启动时配置ErrorWatcher，
     * 越早启动，就越能及时捕获异常信息
     *
     * @param context
     */
    public ErrorWatcher(Context context) {
        this.mContext = context;
    }

    /**
     * 完成相关配置后调用，开始监听crash信息
     */
    public void startWatcher() {
        CrashWatcherHandler crashWatcherHandler = new CrashWatcherHandler((Activity) mContext);
        crashWatcherHandler.setCallback(new CrashWatcherHandler.OnErrorOccurredListener() {
            @Override
            public void errorOccurred(JSONObject jsonObject) {

                switch (mErrorMsgHandleMode) {
                    case REPORT_TO_SERVER:
                        if (mUrl != null && !TextUtils.isEmpty(mUrl) && mPostValueKey != null && !TextUtils.isEmpty(mPostValueKey)) {
                            reportLog2Server(mUrl, jsonObject);
                        } else {
                            Log.e(TAG, "url or postParams error");
                        }
                        break;
                    case LOG:
                        if (mLogTag != null && !TextUtils.isEmpty(mLogTag)) {
                            Log.e(mLogTag, "errorOccurred: " + jsonObject.toString());
                        } else {
                            Log.e(TAG, "errorOccurred: " + jsonObject.toString());
                        }
                        break;
                    case USER:
                        if (mOnCrashCatchListener != null) {
                            mOnCrashCatchListener.crashOccurred(jsonObject);
                        }
                        break;

                    case FILE:
                        if (mLogFilePrefix != null && !TextUtils.isEmpty(mLogFilePrefix)) {
                            saveLog2File(mLogFilePrefix, jsonObject);
                        } else {
                            saveLog2File(DEFAULT_FILE_PREFIX, jsonObject);
                        }
                        break;

                    default:
                        break;
                }
                if (mClass != null) {
                    Intent intent = new Intent(mContext, mClass);
                    mContext.startActivity(intent);
                }

                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(crashWatcherHandler);
    }


    /**
     * 发送log文件至server
     *
     * @param serverUrl
     * @param result
     */
    private void reportLog2Server(final String serverUrl, final JSONObject result) {
        if (mContext.getPackageManager().checkPermission(Manifest.permission.INTERNET, mContext.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            if (mContext.getPackageManager().checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, mContext.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                if (Utils.isInternetConnecting(mContext)) {

                    if (result != null && result.length() > 0) {
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... arg0) {
                                try {
                                    URL url = null;
                                    try {
                                        url = new URL(serverUrl);
                                    } catch (MalformedURLException Urlexception) {
                                        Log.e(TAG, "MalformedURLExcpetion", Urlexception);
                                    }
                                    HttpURLConnection conn = null;
                                    try {
                                        conn = (HttpURLConnection) url.openConnection();
                                    } catch (IOException ioe) {
                                        Log.e(TAG, "IOException", ioe);
                                    }
                                    try {
                                        conn.setRequestMethod("POST");
                                    } catch (ProtocolException protocele) {
                                        Log.e(TAG, "ProtocolException", protocele);
                                    }
                                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                    conn.setDoInput(true);
                                    conn.setDoOutput(true);

                                    String params = createPostParas(mPostValueKey, result.toString());
                                    if (params == null) {
                                        return null;
                                    }
                                    try {
                                        OutputStream os = conn.getOutputStream();
                                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                        writer.write(params);
                                        writer.flush();
                                        writer.close();
                                        os.close();
                                    } catch (Exception ee) {
                                        Log.e(TAG, "Buffer Write Exception", ee);
                                    }
                                    try {
                                        conn.connect();
                                    } catch (IOException e1) {
                                        Log.e(TAG, "IOException", e1);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception Occurred", e);
                                }
                                return null;
                            }
                        }.execute();
                    } else
                        Log.e(TAG, "Capture the log error!");
                } else
                    Log.e(TAG, "Network Error");
            } else
                Log.e(TAG, "Permission denied ,need the permission:ACCESS_NETWORK_STATE");
        } else
            Log.e(TAG, "Permission denied ,need the permission:INTERNET");
    }


    /**
     * 创建post参数
     *
     * @param key
     * @param value
     * @return
     */
    private String createPostParas(String key, String value) {
        StringBuilder result = new StringBuilder();

        result.append("&");
        try {
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }


        return result.toString();
    }


    /**
     * 保存log信息至文件
     *
     * @param prefix
     * @param result
     */
    private void saveLog2File(String prefix, JSONObject result) {

        if (prefix == null || TextUtils.isEmpty(prefix)) {
            prefix = DEFAULT_FILE_PREFIX;
        }
        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
        String fileName = prefix + "_" + sdf.format(new Date()) + ".log";
        FileOutputStream fos = null;
        try {
            fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(result.toString().getBytes());
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 指定异常发生时的处理模式
     * <p/>
     * REPORT_TO_SERVER模式：上传log信息至server，需调用setUrlAndParas设置URL信息
     * <p/>
     * LOG模式：只是将log信息打印在终端，适合debug时使用，可选调用setLogTag自己指定tag
     * <p/>
     * USER模式：将log信息反馈给开发者自行处理，需要设置接口回掉获取log信息
     * <p/>
     * FILE模式：将log信息写入文件，可以调用setLogFileName指定log文件名称
     *
     * @param mode
     * @see #setUrlAndParas(String, String)
     * @see #setfileNamePrefix(String)
     * @see #setLogTag(String)
     * @see #setOnCrashCatchedListener(OnCrashCatchListener)
     */
    public ErrorWatcher setErrorMsgHandleMode(ErrorMsgHandleMode mode) {
        this.mErrorMsgHandleMode = mode;
        return this;
    }

    /**
     * 指定当异常发生时要跳转到的activity；
     * 注意：若不指定，则直接退出程序，不会有任何提示
     *
     * @param classname
     * @return
     * @see Context
     */
    public ErrorWatcher setActivity(Class<?> classname) {
        this.mClass = classname;
        return this;
    }

    /**
     * 指定上传log信息的服务器地址及post参数
     * 参数形式为：url&poatValueKey=logmessage
     *
     * @param url
     * @param postValueKey
     * @return
     */
    public ErrorWatcher setUrlAndParas(String url, String postValueKey) {
        this.mUrl = url;
        this.mPostValueKey = postValueKey;
        return this;
    }


    /**
     * 设置crash发生时的监听器，用于接收crash的log信息，以便开发者自行决定处理方式
     *
     * @param listener
     * @return
     */
    public ErrorWatcher setOnCrashCatchedListener(OnCrashCatchListener listener) {
        this.mOnCrashCatchListener = listener;
        return this;
    }

    /**
     * 可选配置，设置终端log的tag信息
     * 仅在设置mode为LOG时起作用
     * 不配置时默认为ErrorWatcher
     *
     * @param tag
     * @return
     */
    public ErrorWatcher setLogTag(String tag) {
        this.mLogTag = tag;
        return this;
    }


    /**
     * 可选配置
     * 设置log信息保存的文件名前缀
     * 默认前缀为error_watcher_
     *
     * 如果不设置，log文件名称为默认前缀加上每次crash发生的时间，例如 error_watcher_2016年5月6日 16:26:17.log
     *
     * @param prefix
     * @return
     */
    public ErrorWatcher setfileNamePrefix(String prefix) {
        this.mLogFilePrefix = prefix;
        return this;
    }


    /**
     * 获取log文件的存储路径
     *
     * @return String log文件的存储路径
     */
    public String getLogFilePath() {
        return mContext.getFilesDir().getAbsolutePath();
    }


    /**
     * 异常信息处理模式枚举
     * <p/>
     * REPORT_TO_SERVER模式：上传log信息至server，需调用setUrlAndParas设置URL信息
     * <p/>
     * LOG模式：只是将log信息打印在终端，适合debug时使用，可选调用setLogTag自己指定tag
     * <p/>
     * USER模式：将log信息反馈给开发者自行处理，需要设置接口回掉获取log信息
     * <p/>
     * FILE模式：将log信息写入文件，可以调用setLogFileName指定log文件名称
     */
    public enum ErrorMsgHandleMode {
        REPORT_TO_SERVER,
        LOG,
        USER,
        FILE,
    }

    /**
     * 回调接口
     */
    public interface OnCrashCatchListener {
        void crashOccurred(JSONObject crashInfo);
    }
}
