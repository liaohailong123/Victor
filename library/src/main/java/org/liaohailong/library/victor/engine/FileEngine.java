package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.LogMan;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.callback.Callback;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Describe as : 文件下载/上传引擎
 * Created by LHL on 2018/5/2.
 */

public class FileEngine extends AbEngine {

    //文件下载引擎
    private ExecutorService mExecutorService;
    //缓存任务
    private LinkedList<Request<?>> mCachingQueue = new LinkedList<>();
    //记录任务
    private Map<Request<?>, Future<?>> mFutures = new HashMap<>();

    FileEngine(Deliver deliver, int size) {
        super(deliver, size);
    }

    @Override
    public void start() {
        if (mExecutorService == null || mExecutorService.isShutdown() || mExecutorService.isTerminated()) {
            mExecutorService = Executors.newFixedThreadPool(mSize);
        }
    }

    @Override
    public void addRequest(final Request<?> request) {
        //本地有缓存文件
        if (checkCache(request)) {
            return;
        }
        //避免重复请求
        if (mFutures.containsKey(request) || mCachingQueue.contains(request)) {
            return;
        }
        //把控任务数量
        int currentSize = mFutures.size();
        if (currentSize >= mSize) {
            mCachingQueue.add(request);
            return;
        }

        Runnable runnable = new FileRequestRunnable(request);
        Future<?> submit = mExecutorService.submit(runnable);
        mFutures.put(request, submit);
    }

    private boolean checkCache(Request<?> request) {
        String url = request.getUrl();
        String path = FileDownLoader.getPath(url);
        boolean loaded = FileDownLoader.isLoaded(path);
        if (loaded) {
            Callback<?> callback = request.getCallback();
            callback.onPostLoaded(url, path);
        }
        return loaded;
    }

    private final class FileRequestRunnable implements Runnable {
        private Request<?> mRequest;

        private FileRequestRunnable(Request<?> request) {
            mRequest = request;
        }

        @Override
        public void run() {
            try {
                executeNextRequest(mRequest);
            } finally {
                synchronized (this) {
                    mFutures.remove(mRequest);
                    mRequest = null;
                }
            }
        }
    }

    private void executeNextRequest(Request<?> request) {
        try {
            FileLoader fileLoader = FileLoader.create(request, mDeliver);
            //interceptor
            for (Interceptor interceptor : Victor.getInstance().getInterceptors()) {
                interceptor.process(request);
            }
            fileLoader.doWork();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (!mCachingQueue.isEmpty()) {
                try {
                    Request<?> nextRequest = mCachingQueue.removeLast();
                    executeNextRequest(nextRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void removeRequest(Request<?> request) {
        if (mFutures.containsKey(request)) {
            request.cancel();
            Future<?> future = mFutures.get(request);
            if (!future.isCancelled() || !future.isDone()) {
                boolean cancel = future.cancel(true);
                LogMan.i("FileEngine removeRequest = " + cancel + " request = " + request.toString());
            }
            mFutures.remove(request);
            return;
        }
        if (mCachingQueue.contains(request)) {
            mCachingQueue.remove(request);
        }
    }

    @Override
    public void clearRequest() {
        for (Map.Entry<Request<?>, Future<?>> entry : mFutures.entrySet()) {
            Request<?> request = entry.getKey();
            Future<?> future = entry.getValue();
            if (request != null) {
                request.cancel();
            }
            if (future != null) {
                future.cancel(true);
            }
        }
        mFutures.clear();
        mCachingQueue.clear();
    }

    @Override
    public void release() {
        clearRequest();
        if (mExecutorService != null) {
            List<Runnable> runnableList = mExecutorService.shutdownNow();
            LogMan.i("FileEngine release() runnableList = " + runnableList.toString());
            mExecutorService = null;
        }
    }
}
