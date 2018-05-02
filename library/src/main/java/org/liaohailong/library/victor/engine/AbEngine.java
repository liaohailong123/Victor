package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;

/**
 * Describe as : 网络请求引擎适配器
 * Created by LHL on 2018/5/2.
 */

abstract class AbEngine implements IEngine {

    final Deliver mDeliver;
    int mSize = 1;

    AbEngine(Deliver deliver, int size) {
        mDeliver = deliver;
        mSize = size;
    }
}
