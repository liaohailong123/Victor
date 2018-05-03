package org.liaohailong.library.victor.callback;

/**
 * Describe as : 文件上传/下载状态回调
 * Created by LHL on 2018/5/2.
 */

public abstract class FileCallback extends TextCallback<String> {

    @Override
    public final void onSuccess(String result) {
        //do nothing...
    }

    @Override
    public final void onFailure(int code, String error) {
        //do nothing...
    }

    @Override
    public void onPreLoading(String url) {
        //事前，请子类实现
    }

    @Override
    public void onLoading(String url, String tempFilePath, int progress) {
        //事中，请子类实现
    }

    @Override
    public void onPostLoaded(String url, String resultInfo) {
        //事后，请子类实现
    }

    @Override
    public void onLoadingError(String url, String info) {
        //翻车，请子类实现
    }
}
