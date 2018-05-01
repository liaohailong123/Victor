package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.HttpInfo;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Describe as: 网络请求处理
 * Created by LiaoHaiLong on 2018/5/1.
 */

class NetworkDispatcher extends Thread {

    private PriorityBlockingQueue<Request<?>> mNetworkQueue;
    private Deliver mDeliver;

    private volatile boolean mQuite = false;

    NetworkDispatcher(PriorityBlockingQueue<Request<?>> networkQueue, Deliver deliver) {
        mNetworkQueue = networkQueue;
        mDeliver = deliver;
    }

    synchronized final void quite() {
        mQuite = true;
        interrupt();
    }

    @Override
    public void run() {
        while (true) {
            if (mQuite) {
                return;
            }
            try {
                Request<?> request = mNetworkQueue.take();

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

                mDeliver.postResponse(response);

                if (request.isShouldCache() && response.isSuccess()) {
                    CacheWorker.saveCache(request, response);
                }

                //save cookie
                cookie = response.getCookie();
                Victor.getInstance().getConfig().saveCookie(cacheKey, cookie);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
