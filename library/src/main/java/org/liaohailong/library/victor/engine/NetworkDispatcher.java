package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.HttpInfo;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

import java.lang.ref.WeakReference;

/**
 * Describe as: 网络请求处理
 * Created by LiaoHaiLong on 2018/5/1.
 */

class NetworkDispatcher implements Runnable {

    private WeakReference<TextEngine> mTextEngineWeak;
    private WeakReference<? extends Request<?>> mRequestWeak;
    private WeakReference<Deliver> mDeliverWeak;

    NetworkDispatcher(Request<?> request, TextEngine textEngine, Deliver deliver) {
        mTextEngineWeak = new WeakReference<>(textEngine);
        mRequestWeak = new WeakReference<>(request);
        mDeliverWeak = new WeakReference<>(deliver);
    }

    @Override
    public void run() {
        try {
            Request<?> request = mRequestWeak.get();
            if (request == null || request.isCanceled) {
                return;
            }
            Deliver deliver = mDeliverWeak.get();
            //use cookie if need
            String cacheKey = request.getCacheKey();
            String cookie = Victor.getInstance().getConfig().getCookie(cacheKey);
            if (request.isShouldCookie()) {
                HttpField httpHeader = request.getHttpHeader();
                httpHeader.addParam(HttpInfo.COOKIE, cookie);
            }

            //interceptor
            for (Interceptor interceptor : Victor.getInstance().getInterceptors()) {
                interceptor.process(request);
            }

            HttpWorker httpWorker = HttpWorker.create(request);

            long startTimeMs = System.currentTimeMillis();

            Response<?> response = httpWorker.doWork();

            long endTimeMs = System.currentTimeMillis();

            response.setNetworkTimeMs(endTimeMs - startTimeMs);

            deliver.postResponse(response);

            if (request.isShouldCache() && response.isSuccess()) {
                CacheWorker.saveCache(request, response);
            }

            //save cookie
            cookie = response.getCookie();
            Victor.getInstance().getConfig().saveCookie(cacheKey, cookie);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mTextEngineWeak.get() != null) {
                if (mRequestWeak.get() != null) {
                    mTextEngineWeak.get().removeRequest(mRequestWeak.get());
                }
                mTextEngineWeak.clear();
            }
            mRequestWeak.clear();
            mDeliverWeak.clear();
        }
    }
}
