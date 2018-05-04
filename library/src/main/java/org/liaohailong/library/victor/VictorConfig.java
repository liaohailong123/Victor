package org.liaohailong.library.victor;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.liaohailong.library.victor.engine.CacheInfo;
import org.liaohailong.library.victor.interceptor.Interceptor;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Describe as: 网络请求相关默认配置
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class VictorConfig {
    private static final String DEFAULT_CACHE_DIR = "victor";
    private int mMaxDiskCacheBytes = 10 * 1024 * 1024;

    private Context mApplicationContext;
    private Map<String, String> mDefaultHeaders = new HashMap<>();
    private Map<String, String> mDefaultParams = new HashMap<>();
    private HttpConnectSetting mHttpConnectSetting = new HttpConnectSetting();
    private File mRootDirectory;
    private SharedPreferences sp;
    private boolean isLogEnable = false;

    //拦截器
    private LinkedList<Interceptor> mInterceptors = new LinkedList<>();

    VictorConfig(Context context) {
        mApplicationContext = context.getApplicationContext();
        sp = mApplicationContext.getSharedPreferences(DEFAULT_CACHE_DIR, Context.MODE_PRIVATE);
        mDefaultHeaders.put("Host", "");
        mDefaultHeaders.put("Cache-Control", "private");
        mDefaultHeaders.put("Connection", "keep-alive");
        mDefaultHeaders.put("Charset", "UTF-8");
        mDefaultHeaders.put("Accept-Encoding", "gzip, deflate");
    }

    Context getApplicationContext() {
        return mApplicationContext;
    }

    public VictorConfig addDefaultHeader(String header, String value) {
        mDefaultHeaders.put(header, value);
        return this;
    }

    public VictorConfig addDefaultHeaders(Map<String, String> header) {
        mDefaultHeaders.putAll(header);
        return this;
    }

    public VictorConfig addDefaultParam(String key, String value) {
        mDefaultParams.put(key, value);
        return this;
    }

    public VictorConfig addDefaultParams(Map<String, String> params) {
        mDefaultParams.putAll(params);
        return this;
    }

    public VictorConfig createCacheDirectory(String cacheDir, int maxDiskCacheBytes) {
        mRootDirectory = new File(TextUtils.isEmpty(cacheDir) ?
                mApplicationContext.getCacheDir().getAbsolutePath() : cacheDir, DEFAULT_CACHE_DIR);
        if (!mRootDirectory.exists()) {
            boolean mkdirs = mRootDirectory.mkdirs();
            if (!mkdirs) {
                LogMan.e("mRootDirectory mkdirs = false");
            }
        } else if (!mRootDirectory.isDirectory()) {
            boolean delete = mRootDirectory.delete();
            if (delete) {
                boolean mkdirs = mRootDirectory.mkdirs();
                if (!mkdirs) {
                    LogMan.e("mRootDirectory mkdirs = false");
                }
            }
        }

        mMaxDiskCacheBytes = maxDiskCacheBytes;
        return this;
    }

    public VictorConfig setConnectTimeout(int mConnectTimeout) {
        mHttpConnectSetting.setConnectTimeout(mConnectTimeout);
        return this;
    }

    public VictorConfig setReadTimeout(int mReadTimeout) {
        mHttpConnectSetting.setReadTimeout(mReadTimeout);
        return this;
    }

    public VictorConfig addInterceptor(Interceptor interceptor) {
        if (interceptor != null) {
            mInterceptors.add(interceptor);
        }
        return this;
    }

    public VictorConfig setLogEnable(boolean logEnable) {
        isLogEnable = logEnable;
        return this;
    }

    boolean isLogEnable() {
        return isLogEnable;
    }

    LinkedList<Interceptor> getInterceptors() {
        return mInterceptors;
    }

    public void addCacheInfo(String key, CacheInfo cacheInfo) {
        if (sp != null) {
            String jsonString = Util.objectToJsonString(cacheInfo);
            sp.edit().putString(key, jsonString).apply();
        }
    }

    public CacheInfo getCacheInfo(String key) {
        String jsonString = sp.getString(key, "");
        if (TextUtils.isEmpty(jsonString)) {
            return new CacheInfo();
        }
        return Util.jsonStringToObject(jsonString, CacheInfo.class);
    }

    public void saveCookie(String key, String cookie) {
        if (sp != null) {
            sp.edit().putString(key, cookie).apply();
        }
    }

    public String getCookie(String key) {
        return sp.getString(key, "");
    }

    public void saveFileLoadingLength(String key, long length) {
        if (sp != null) {
            sp.edit().putLong(key, length).apply();
        }
    }

    public long getFileLoadingLength(String key) {
        return sp.getLong(key, 0);
    }

    HttpConnectSetting getHttpConnectSetting() {
        return mHttpConnectSetting;
    }

    Map<String, String> getDefaultHeaders() {
        return mDefaultHeaders;
    }

    Map<String, String> getDefaultParams() {
        return mDefaultParams;
    }

    public int getMaxDiskCacheBytes() {
        return mMaxDiskCacheBytes;
    }

    public File getRootDirectory() {
        return mRootDirectory;
    }
}
