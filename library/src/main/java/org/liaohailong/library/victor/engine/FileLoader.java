package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.callback.Callback;
import org.liaohailong.library.victor.request.FileRequest;
import org.liaohailong.library.victor.request.Request;

/**
 * Describe as : 文件请求
 * Created by LHL on 2018/5/3.
 */

class FileLoader<Type> extends HttpWorker<Type> {

    static <T> FileLoader create(Request<T> request, Deliver deliver) {
        FileLoader<T> tFileLoader;
        FileRequest<T> tFileRequest = (FileRequest<T>) request;
        if (tFileRequest.isDownload()) {
            tFileLoader = new FileDownLoader<>(request, deliver);
        } else {
            tFileLoader = new FileUpLoader<>(request, deliver);
        }
        return tFileLoader;
    }

    private Deliver mDeliver;

    FileLoader(Request<Type> request, Deliver deliver) {
        super(request);
        mDeliver = deliver;
    }

    void onPreLoading() {
        if (mDeliver != null && mRequest != null) {
            final Callback<Type> callback = mRequest.getCallback();
            mDeliver.postResponse(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onPreLoading(mRequest.getUrl());
                    }
                }
            });
        }
    }

    void onLoading(final String tempPath, final int progress) {
        if (mDeliver != null && mRequest != null) {
            final String url = mRequest.getUrl();
            final Callback<Type> callback = mRequest.getCallback();
            mDeliver.postResponse(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onLoading(url, tempPath, progress);
                    }
                }
            });
        }
    }

    void onPostLoaded(final String filePath) {
        if (mDeliver != null && mRequest != null) {
            final Callback<Type> callback = mRequest.getCallback();
            final String url = mRequest.getUrl();
            mDeliver.postResponse(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onPostLoaded(url, filePath);
                    }
                }
            });
        }
    }

    void onLoadingError(final String errorInfo) {
        if (mDeliver != null && mRequest != null) {
            final String url = mRequest.getUrl();
            final Callback<Type> callback = mRequest.getCallback();
            mDeliver.postResponse(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onLoadingError(url, errorInfo);
                    }
                }
            });
        }
    }
}
