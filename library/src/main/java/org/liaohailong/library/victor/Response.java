package org.liaohailong.library.victor;

import android.text.TextUtils;

import org.liaohailong.library.victor.callback.Callback;
import org.liaohailong.library.victor.engine.CacheInfo;

import java.net.HttpURLConnection;

/**
 * Describe as: 请求返回数据
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class Response<T>  {

    private int code;
    private String result;
    private long networkTimeMs;
    private CacheInfo cacheInfo;
    private String setCookie;
    private Callback<T> callback;

    public Response<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public Response<T> setResult(String result) {
        this.result = result;
        return this;
    }

    public Response<T> setNetworkTimeMs(long networkTimeMs) {
        this.networkTimeMs = networkTimeMs;
        return this;
    }

    public Response<T> setCallback(Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    public Response<T> setCacheInfo(CacheInfo cacheInfo) {
        this.cacheInfo = cacheInfo;
        return this;
    }

    public Response<T> setCookie(String setCookie) {
        this.setCookie = TextUtils.isEmpty(setCookie) ? "" : setCookie;
        return this;
    }

    public boolean isSuccess() {
        return code < HttpURLConnection.HTTP_BAD_REQUEST;
    }

    public String getRawResult() {
        return result;
    }

    Callback<T> getCallback() {
        return callback;
    }

    public CacheInfo getCacheInfo() {
        return cacheInfo;
    }

    public String getCookie() {
        return setCookie;
    }

    int getCode() {
        return code;
    }
}
