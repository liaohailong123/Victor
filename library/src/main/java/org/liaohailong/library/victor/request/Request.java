package org.liaohailong.library.victor.request;

import android.text.TextUtils;

import org.liaohailong.library.victor.HttpConnectSetting;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.HttpInfo;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.callback.Callback;

import java.util.Map;

/**
 * Describe as: 普通的文本请求
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class Request<T> {
    //Http协议相关
    private final String mUrl;//请求服务器的地址
    private final String mHttpMethod;//默认为GET请求
    private final HttpField mHttpHeader;//Http报文请求首部字段
    private final HttpField mHttpParams;//POST方法的请求参数
    private final HttpConnectSetting mHttpConnectSetting;//Http协议请求过程中的自定义设置

    //数据回调
    private Callback<T> mCallback;

    //开关标记位
    public volatile boolean isCanceled = false;

    public Request(String url,
                   String httpMethod,
                   HttpField httpHeader,
                   HttpField httpField,
                   HttpConnectSetting httpConnectSetting,
                   Callback<T> callback) {

        mUrl = url;
        mHttpMethod = httpMethod;
        mHttpHeader = httpHeader;
        mHttpParams = httpField;
        mHttpConnectSetting = httpConnectSetting;
        mCallback = callback;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getHttpMethod() {
        return mHttpMethod;
    }

    public boolean isPost() {
        return TextUtils.equals(HttpInfo.POST, getHttpMethod());
    }

    public HttpField getHttpHeader() {
        return mHttpHeader;
    }

    public HttpField getPostParams() {
        return mHttpParams;
    }

    public HttpConnectSetting getHttpConnectSetting() {
        return mHttpConnectSetting;
    }

    public Response<T> generateResponse() {
        return new Response<>();
    }

    public Callback<T> getCallback() {
        return mCallback;
    }

    public void setCallback(Callback<T> mCallback) {
        this.mCallback = mCallback;
    }

    public String getCacheKey() {
        String url = getUrl();
        String httpMethod = getHttpMethod();
        HttpField postParams = getPostParams();
        Map<String, String> params = postParams.getParams();
        String cacheFileName = Util.getCacheFileName(url, httpMethod, params);
        return Util.hashKeyFromUrl(cacheFileName);
    }

    public synchronized void cancel() {
        isCanceled = true;
    }

    @Override
    public String toString() {
        return "url = " + mUrl
                + "   httpMethod = " + mHttpMethod
                + "   httpHeader = " + mHttpHeader.toString()
                + "  mHttpParams = " + mHttpParams.toString();
    }
}
