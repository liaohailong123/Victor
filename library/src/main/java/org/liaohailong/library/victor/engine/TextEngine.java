package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.request.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Describe as: 文本数据请求引擎
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class TextEngine extends AbEngine {

    private ExecutorService mNetWorkWorker;
    private ExecutorService mCacheWorker;

    private Map<Request<?>, Future<?>> mAcceptRequest = new HashMap<>();

    TextEngine(Deliver deliver, int size) {
        super(deliver, size);
        mSize = mSize < 2 ? 2 : mSize;
        mNetWorkWorker = Executors.newFixedThreadPool(mSize - 1);
        mCacheWorker = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() {
        if (mNetWorkWorker == null || mNetWorkWorker.isShutdown() || mNetWorkWorker.isTerminated()) {
            mNetWorkWorker = Executors.newFixedThreadPool(mSize - 1);
        }
        if (mCacheWorker == null || mCacheWorker.isShutdown() || mCacheWorker.isTerminated()) {
            mCacheWorker = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void addRequest(Request<?> request) {
        if (request == null) {
            return;
        }
        if (request.isShouldCache()) {
            Future<?> submit = mCacheWorker.submit(new CacheDispatcher(request, this, mDeliver));
            mAcceptRequest.put(request, submit);
        } else {
            addNetWorkRequest(request);
        }
    }

    void addNetWorkRequest(Request<?> request) {
        if (request == null) {
            return;
        }
        Future<?> submit = mNetWorkWorker.submit(new NetworkDispatcher(request, this, mDeliver));
        mAcceptRequest.put(request, submit);
    }

    @Override
    public void removeRequest(Request<?> request) {
        if (mAcceptRequest.containsKey(request)) {
            request.cancel();
            Future<?> future = mAcceptRequest.get(request);
            future.cancel(true);
            mAcceptRequest.remove(request);
        }
    }

    @Override
    public void clearRequest() {
        for (Request<?> request : mAcceptRequest.keySet()) {
            request.cancel();
        }
        for (Map.Entry<Request<?>, Future<?>> entry : mAcceptRequest.entrySet()) {
            Request<?> request = entry.getKey();
            Future<?> future = entry.getValue();
            if (request != null) {
                request.cancel();
            }
            if (future != null) {
                future.cancel(true);
            }
        }
        mAcceptRequest.clear();
    }

    @Override
    public void release() {
        clearRequest();
        if (mNetWorkWorker != null) {
            mNetWorkWorker.shutdownNow();
            mNetWorkWorker = null;
        }
        if (mCacheWorker != null) {
            mCacheWorker.shutdownNow();
            mCacheWorker = null;
        }
    }
}
