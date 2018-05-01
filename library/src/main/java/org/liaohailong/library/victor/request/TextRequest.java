package org.liaohailong.library.victor.request;

import org.liaohailong.library.victor.callback.Callback;
import org.liaohailong.library.victor.HttpConnectSetting;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.RequestPriority;
import org.liaohailong.library.victor.engine.IEngine;

/**
 * Describe as: 文本数据请求
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class TextRequest<Type> extends Request<Type> {

    public TextRequest(RequestPriority requestPriority,
                       int order,
                       boolean shouldCache,
                       boolean shouldCookie,
                       String url,
                       String httpMethod,
                       HttpField httpHeader,
                       HttpField httpField,
                       HttpConnectSetting httpConnectSetting,
                       Callback<Type> callback,
                       IEngine engine) {
        super(requestPriority,
                order,
                shouldCache,
                shouldCookie,
                url,
                httpMethod,
                httpHeader,
                httpField,
                httpConnectSetting,
                callback,
                engine);
    }
}
