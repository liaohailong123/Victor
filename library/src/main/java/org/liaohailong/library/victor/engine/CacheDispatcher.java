package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.VictorConfig;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Describe as: 缓存数据处理
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class CacheDispatcher extends Thread {

    private PriorityBlockingQueue<Request<?>> mNetworkQueue;
    private PriorityBlockingQueue<Request<?>> mCacheQueue;
    private final Deliver mDeliver;


    private volatile boolean mQuite = false;

    synchronized final void quite() {
        mQuite = true;
        interrupt();
    }

    CacheDispatcher(PriorityBlockingQueue<Request<?>> networkQueue, PriorityBlockingQueue<Request<?>> cacheQueue, Deliver deliver) {
        mNetworkQueue = networkQueue;
        mCacheQueue = cacheQueue;
        mDeliver = deliver;
    }

    @Override
    public void run() {
        while (true) {
            if (mQuite) {
                return;
            }

            try {
                final Request<?> request = mCacheQueue.take();

                //interceptor
                for (Interceptor interceptor : Victor.getInstance().getInterceptors()) {
                    interceptor.process(request);
                }

                //如果资源过期了，分发回网络请求队列
                if (isExpire(request)) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            mNetworkQueue.add(request);
                        }
                    };
                    mDeliver.postResponse(runnable);
                    continue;
                }
                //如果有效期过期了，分发回网络请求队列
                if (refreshNeeded(request)) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            mNetworkQueue.add(request);
                        }
                    };
                    mDeliver.postResponse(runnable);
                    continue;
                }

                Response<?> response = CacheWorker.getCache(request);
                mDeliver.postResponse(response);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isExpire(Request<?> request) {
        String cacheKey = request.getCacheKey();
        VictorConfig config = Victor.getInstance().getConfig();
        CacheInfo cacheInfo = config.getCacheInfo(cacheKey);
        return cacheInfo == null || cacheInfo.isExpired();
    }

    private boolean refreshNeeded(Request<?> request) {
        String cacheKey = request.getCacheKey();
        VictorConfig config = Victor.getInstance().getConfig();
        CacheInfo cacheInfo = config.getCacheInfo(cacheKey);
        return cacheInfo == null || cacheInfo.refreshNeeded();
    }
}
