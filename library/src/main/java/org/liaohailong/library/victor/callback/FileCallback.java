package org.liaohailong.library.victor.callback;

/**
 * Describe as :
 * Created by LHL on 2018/5/2.
 */

public abstract class FileCallback implements Callback<String> {

    @Override
    public void getRawData(int code, String data) {
        //do nothing...
    }

    @Override
    public void onSuccess(String result) {

    }

    @Override
    public void onFailure(int code, String error) {
        //do nothing...
    }

    @Override
    public void onPreLoading(String url) {

    }

    @Override
    public void onLoading(String url, String tempFilePath, int progress) {

    }

    @Override
    public void onPostLoaded(String url, String filePath) {

    }

    @Override
    public void onLoadingError(String url, String info) {

    }
}
