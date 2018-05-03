package org.liaohailong.library.victor;

import android.util.Log;

/**
 * Describe as : Log输出
 * Created by LHL on 2018/5/2.
 */

public final class LogMan {
    private static final String TAG = "Victor";

    private LogMan() throws IllegalAccessException {
        throw new IllegalAccessException("no instance");
    }

    public static void i(String msg) {
        if (Victor.getInstance().getConfig().isLogEnable()) {
            Log.i(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (Victor.getInstance().getConfig().isLogEnable()) {
            Log.e(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (Victor.getInstance().getConfig().isLogEnable()) {
            Log.d(TAG, msg);
        }
    }
}
