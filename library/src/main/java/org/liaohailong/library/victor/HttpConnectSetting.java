package org.liaohailong.library.victor;

/**
 * Describe as: Http请求连接设置
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class HttpConnectSetting {
    private int mConnectTimeout;
    private int mReadTimeout;

    HttpConnectSetting() {
        mConnectTimeout = 2 * 1000;
        mReadTimeout = 2 * 1000;
    }

    HttpConnectSetting setConnectTimeout(int mConnectTimeout) {
        this.mConnectTimeout = mConnectTimeout;
        return this;
    }

    HttpConnectSetting setReadTimeout(int mReadTimeout) {
        this.mReadTimeout = mReadTimeout;
        return this;
    }

    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    public int getReadTimeout() {
        return mReadTimeout;
    }
}
