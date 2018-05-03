package org.liaohailong.library.victor.request;

import org.liaohailong.library.victor.HttpConnectSetting;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.RequestPriority;
import org.liaohailong.library.victor.callback.Callback;
import org.liaohailong.library.victor.engine.IEngine;

import java.io.File;

/**
 * Describe as : 文件类型操作的请求
 * Created by LHL on 2018/5/3.
 */

public class FileRequest<T> extends Request<T> {

    private boolean isMultiple = false;
    private boolean isDownload = true;
    private String mKey;
    private File mValue;

    public FileRequest(RequestPriority requestPriority, int order, boolean shouldCache, boolean shouldCookie, String url, String httpMethod, HttpField httpHeader, HttpField httpField, HttpConnectSetting httpConnectSetting, Callback<T> callback, IEngine engine) {
        super(requestPriority, order, shouldCache, shouldCookie, url, httpMethod, httpHeader, httpField, httpConnectSetting, callback, engine);
    }

    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public boolean isMultiple() {
        return isMultiple;
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
