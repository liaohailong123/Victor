package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.request.Request;

/**
 * Describe as: 请求引擎规范
 * Created by LiaoHaiLong on 2018/5/1.
 */

public interface IEngine {
    void start();

    void addRequest(Request<?> request);

    void removeRequest(Request<?> request);

    void clearRequest();

    void release();
}
