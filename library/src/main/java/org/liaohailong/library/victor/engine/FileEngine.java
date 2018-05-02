package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.LogMan;
import org.liaohailong.library.victor.request.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Describe as : 文件下载/上传引擎
 * Created by LHL on 2018/5/2.
 */

public class FileEngine extends AbEngine {

    //文件下载引擎
    private ExecutorService mExecutorService;

    //缓存任务
    private PriorityBlockingQueue<Request<?>> mCachingQueue = new PriorityBlockingQueue<>();
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
                }
            }
        }
    }

    private void executeNextRequest(Request<?> request) {
        try {
            FileLoader fileLoader = FileLoader.create(request, mDeliver);
            fileLoader.doWork();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (!mCachingQueue.isEmpty()) {
                try {
                    Request<?> nextRequest = mCachingQueue.take();
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
            if (!future.isCancelled() || !future.isDone()) {
                boolean cancel = future.cancel(true);
                LogMan.i("FileEngine removeRequest = " + cancel + " request = " + request.toString());
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
            LogMan.i("FileEngine release() runnables = " + runnableList.toString());
            mExecutorService = null;
        }
    }
}
