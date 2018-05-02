package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.request.Request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Describe as : 文件下载/上传引擎
 * Created by LHL on 2018/5/2.
 */

public class FileEngine extends AbEngine {

    //文件下载引擎
    private final ExecutorService mExecutorService;

    FileEngine(Deliver deliver, int size) {
        super(deliver, size);
        mExecutorService = Executors.newFixedThreadPool(size);
    }

    @Override
    public void start() {

    }

    @Override
    public void addRequest(Request<?> request) {

    }

    @Override
    public void removeRequest(Request<?> request) {

    }

    @Override
    public void clearRequest() {

    }

    @Override
    public void release() {

    }
}
