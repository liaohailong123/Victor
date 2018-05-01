package org.liaohailong.library.victor.interceptor;

import org.liaohailong.library.victor.request.Request;

/**
 * Describe as: 网络请求拦截器
 * Created by LiaoHaiLong on 2018/5/1.
 */

public interface Interceptor {

    void process(Request<?> request);
}
