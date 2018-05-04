package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.VictorConfig;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

import java.lang.ref.WeakReference;

/**
 * Describe as: 缓存数据处理
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class CacheRunnable implements Runnable {

    private WeakReference<TextEngine> mTextEngineWeak;
    private WeakReference<Deliver> mDeliverWeak;
    private Request<?> mRequest;

    CacheRunnable(Request<?> request, TextEngine textEngine, Deliver deliver) {
        mRequest = request;
        mTextEngineWeak = new WeakReference<>(textEngine);
        mDeliverWeak = new WeakReference<>(deliver);
    }

    @Override
    public void run() {
        try {
            Request<?> request = mRequest;
            if (request == null || request.isCanceled) {
                return;
            }
            Deliver deliver = mDeliverWeak.get();

            //如果资源过期了，分发回网络请求队列
            if (isExpire(request)) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mTextEngineWeak.get() != null) {
                            mTextEngineWeak.get().addNetWorkRequest(mRequest);
                        }
                        mRequest = null;
                    }
                };
                deliver.postResponse(runnable);
                return;
            }
            //如果有效期过期了，分发回网络请求队列
            if (refreshNeeded(request)) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mTextEngineWeak.get() != null) {
                            mTextEngineWeak.get().addNetWorkRequest(mRequest);
                        }
                        mRequest = null;
                    }
                };
                deliver.postResponse(runnable);
                return;
            }

            Response<?> response = CacheWorker.getCache(request);
            deliver.postResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
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
