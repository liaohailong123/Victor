package org.liaohailong.library.victor.request;

import org.liaohailong.library.victor.HttpConnectSetting;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.callback.Callback;

import java.io.File;

/**
 * Describe as : 文件类型操作的请求
 * Created by LHL on 2018/5/3.
 */

public class FileRequest<T> extends Request<T> {

    private boolean isDownload = true;
    private String mKey;
    private File mValue;

    public FileRequest(String url,
                       String httpMethod,
                       HttpField httpHeader,
                       HttpField httpField,
                       HttpConnectSetting httpConnectSetting,
                       Callback<T> callback) {
        super(url,
                httpMethod,
                httpHeader,
                httpField,
                httpConnectSetting,
                callback);
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void addFiles(String fileKey, File fileValue) {
        mKey = fileKey;
        mValue = fileValue;
    }

    public String getFileKey() {
        return mKey;
    }

    public File getFile() {
        return mValue;
    }
}
