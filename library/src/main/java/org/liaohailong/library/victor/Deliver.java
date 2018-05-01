package org.liaohailong.library.victor;


import android.os.Handler;
import android.os.Looper;

import org.liaohailong.library.victor.callback.Callback;

/**
 * Describe as: 将数据从工作线程传递到主线程的传送门
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class Deliver {
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public <T> void postResponse(final Response<T> response) {
        if (response == null) {
            return;
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Callback<T> callback = response.getCallback();
                int code = response.getCode();
                String rawResult = response.getRawResult();
                callback.getRawData(code, rawResult);
                response.setCallback(null);
            }
        };
        mHandler.post(runnable);
    }

    public void postResponse(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mHandler.post(runnable);
    }
}
