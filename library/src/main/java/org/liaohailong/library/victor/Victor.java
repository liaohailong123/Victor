package org.liaohailong.library.victor;

import android.content.Context;
import android.support.annotation.MainThread;
import android.text.TextUtils;

import org.liaohailong.library.victor.callback.Callback;
import org.liaohailong.library.victor.engine.EngineManager;
import org.liaohailong.library.victor.engine.FileEngine;
import org.liaohailong.library.victor.engine.IEngine;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Describe as: 基于HttpUrlConnection封装的网络请求库
 * 基本功能：
 * 1，绑定Activity or Fragment的生命周期                (未完成)
 * 2，手动移除网络队列中的任务（文件下载/上传除外）
 * 3，上传文件、下载文件（多线程断点下载）             (未完成)
 * 4，仿Volley的万箭齐发式请求（轻量级任务）
 * 5，数据缓存，减少请求网络的频率，从而优化流量费用
 * 6，自由修改全局统一的Http请求首部字段
 * 7，全局统一的拦截器
 * Created by LiaoHaiLong on 2018/4/30.
 */

public class Victor {
    //全局基本配置
    private VictorConfig mVictorConfig;
    //数据跨线程传送门
    private Deliver mDeliver = new Deliver();
    //双引擎控制器
    private EngineManager mEngineManager = new EngineManager(mDeliver);

    private static final class SingletonHolder {
        static final Victor INSTANCE = new Victor();
    }

    public static Victor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @MainThread
    public VictorConfig initConfig(Context context) {
        if (mVictorConfig == null) {
            mVictorConfig = new VictorConfig(context);
        }
        return mVictorConfig;
    }

    public VictorConfig getConfig() {
        return mVictorConfig;
    }

    public EngineManager getEngineManager() {
        return mEngineManager;
    }

    public Victor addInterceptor(Interceptor interceptor) {
        mVictorConfig.addInterceptor(interceptor);
        return this;
    }

    public LinkedList<Interceptor> getInterceptors() {
        return mVictorConfig.getInterceptors();
    }

    public TextRequestBuilder newTextRequest() {
        return new TextRequestBuilder();
    }

    public FileRequestBuilder newFileRequest() {
        return new FileRequestBuilder();
    }

    public abstract class RequestBuilder {
        private String url;
        private String httpMethod = HttpInfo.GET;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> params = new HashMap<>();
        private RequestPriority requestPriority = RequestPriority.MIDDLE;
        private int connectTimeOut = 0;
        private int readTimeOut = 0;
        private IEngine mEngine;
        private boolean useCache = false;
        private boolean useCookie = false;

        public RequestBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public RequestBuilder doGet() {
            httpMethod = HttpInfo.GET;
            return this;
        }

        public RequestBuilder doPost() {
            httpMethod = HttpInfo.POST;
            return this;
        }

        public RequestBuilder addHeader(String header, String value) {
            headers.put(header, value);
            return this;
        }

        public RequestBuilder addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public RequestBuilder addParam(String key, String value) {
            params.put(key, value);
            return this;
        }

        public RequestBuilder addParams(Map<String, String> params) {
            this.params.putAll(params);
            return this;
        }

        public RequestBuilder setRequestPriority(RequestPriority requestPriority) {
            this.requestPriority = requestPriority;
            return this;
        }

        public RequestBuilder setConnectTimeOut(int connectTimeOut) {
            this.connectTimeOut = connectTimeOut;
            return this;
        }

        public RequestBuilder setReadTimeOut(int readTimeOut) {
            this.readTimeOut = readTimeOut;
            return this;
        }

        public RequestBuilder setUseCache(boolean useCache) {
            this.useCache = useCache;
            return this;
        }

        public RequestBuilder setUseCookie(boolean useCookie) {
            this.useCookie = useCookie;
            return this;
        }

        public <T> Request<T> setCallback(Callback<T> callback) {
            if (TextUtils.isEmpty(url)) {
                throw new IllegalArgumentException(" url can not be empty!");
            }
            int order = (int) System.currentTimeMillis();

            Map<String, String> defaultHeaders = mVictorConfig.getDefaultHeaders();
            Map<String, String> defaultParams = mVictorConfig.getDefaultParams();

            HttpField httpHeader = new HttpField()
                    .addParams(defaultHeaders)
                    .addParams(headers);

            HttpField httpParams = new HttpField()
                    .addParams(defaultParams)
                    .addParams(params);

            HttpConnectSetting mDefaultHttpConnectSetting = mVictorConfig.getHttpConnectSetting();
            HttpConnectSetting httpConnectSetting = new HttpConnectSetting()
                    .setConnectTimeout(connectTimeOut > 0 ? connectTimeOut : mDefaultHttpConnectSetting.getConnectTimeout())
                    .setReadTimeout(readTimeOut > 0 ? readTimeOut : mDefaultHttpConnectSetting.getReadTimeout());

            Request<T> request = new Request<>(requestPriority,
                    order,
                    useCache,
                    useCookie,
                    url,
                    httpMethod,
                    httpHeader,
                    httpParams,
                    httpConnectSetting,
                    callback,
                    mEngine);

            if (mEngine == null) {
                mEngine = getEngine();
            }
            mEngine.addRequest(request);
            return request;
        }

        protected abstract IEngine getEngine();
    }


    public final class TextRequestBuilder extends RequestBuilder {

        @Override
        protected IEngine getEngine() {
            return mEngineManager.getTextEngine();
        }
    }

    public final class FileRequestBuilder extends RequestBuilder {

        @Override
        protected IEngine getEngine() {
            FileEngine fileEngine = mEngineManager.getFileEngine();
            fileEngine.start();
            return fileEngine;
        }
    }
}

