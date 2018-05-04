package org.liaohailong.library.victor;

/**
 * Describe as: Http请求连接设置
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class HttpConnectSetting {
    private static final int MIN_TIME_OUT = 2000;
    private int mConnectTimeout;
    private int mReadTimeout;
    private boolean useCache = false;
    private boolean useCookie = false;

    HttpConnectSetting() {
        mConnectTimeout = 2 * 1000;
        mReadTimeout = 2 * 1000;
    }

    HttpConnectSetting setConnectTimeout(int mConnectTimeout) {
        this.mConnectTimeout = mConnectTimeout;
        return this;
    }

    HttpConnectSetting setReadTimeout(int mReadTimeout) {
        this.mReadTimeout = mReadTimeout;
        return this;
    }

    HttpConnectSetting setUseCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    HttpConnectSetting setUseCookie(boolean useCookie) {
        this.useCookie = useCookie;
        return this;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public boolean isUseCookie() {
        return useCookie;
    }

    public int getConnectTimeout() {
        return mConnectTimeout <= MIN_TIME_OUT ? MIN_TIME_OUT : mConnectTimeout;
    }

    public int getReadTimeout() {
        return mReadTimeout <= MIN_TIME_OUT ? MIN_TIME_OUT : mReadTimeout;
    }
}
