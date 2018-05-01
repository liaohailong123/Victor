package org.liaohailong.library.victor.callback;

/**
 * Describe as: 网络请求回调
 * Created by LiaoHaiLong on 2018/5/1.
 */

public interface Callback<T> {

    void getRawData(int code, String data);

    void onSuccess(T result);

    void onFailure(int code, String error);
}
