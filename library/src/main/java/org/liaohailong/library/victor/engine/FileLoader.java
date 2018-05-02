package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.HttpInfo;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.callback.Callback;
import org.liaohailong.library.victor.request.Request;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * Describe as : 文件文件下载/上传处理器
 * Created by LHL on 2018/5/2.
 */

class FileLoader<Type> extends HttpWorker<Type> {
    private static final String TEMP = ".temp";
    private static final String mDirectory;

    static {
        mDirectory = Victor.getInstance().getConfig().getRootDirectory().getAbsolutePath();
    }

    private Deliver mDeliver;

    static <T> FileLoader create(Request<T> request, Deliver deliver) {
        FileLoader<T> tFileLoader = new FileLoader<>(request);
        tFileLoader.mDeliver = deliver;
        return tFileLoader;
    }

    private FileLoader(Request<Type> request) {
        super(request);
    }

    @Override
    Response<Type> doWork() {
        onPreLoading();
        try {
            String url = mRequest.getUrl();
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.setRequestProperty("Accept-Encoding", "identity");
            int responseCode = urlConnection.getResponseCode();
            if (responseCode >= HttpURLConnection.HTTP_OK
                    && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                int maxProgress = urlConnection.getContentLength();//获取文件
                //检查用户是否已经删除了临时下载文件
                String tempPath = getTempPath(url);
                boolean loaded = isLoaded(tempPath);
                if (!loaded) {
                    Victor.getInstance().getConfig().saveFileLoadingLength(url, 0);
                }
                long lastPosition = Victor.getInstance().getConfig().getFileLoadingLength(url);
                urlConnection.disconnect();
                return downLoad(lastPosition, maxProgress);
            } else {
                urlConnection.disconnect();
                Response<Type> response = mRequest.generateResponse();
                response.setCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .setResult("code = " + responseCode)
                        .setCallback(mRequest.getCallback());
                onLoadingError("code = " + responseCode);
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Response<Type> response = mRequest.generateResponse();
            response.setCode(HttpURLConnection.HTTP_BAD_REQUEST)
                    .setResult(e.toString())
                    .setCallback(mRequest.getCallback());
            mRequest.setCallback(null);
            mRequest = null;
            onLoadingError(e.toString());
            return response;
        }
    }

    private Response<Type> downLoad(long lastPosition, int maxLength) {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
            String url = mRequest.getUrl();
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            setConnection(urlConnection);
            addHeaders(urlConnection);
            postParamsIfNeed(urlConnection);
            urlConnection.setRequestProperty("Accept-Encoding", "identity");
            urlConnection.setRequestProperty("Accept-Ranges", "bytes");
            urlConnection.setRequestProperty("Range", "bytes=" + lastPosition + "-" + maxLength);
            int code = urlConnection.getResponseCode();
            if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                Response<Type> response = mRequest.generateResponse();
                response.setCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .setResult("code = " + code)
                        .setCallback(mRequest.getCallback());
                InputStream errorStream = urlConnection.getErrorStream();
                String errorInfo = Util.streamToString(errorStream);
                onLoadingError("code = " + code + " errorInfo = " + errorInfo);
                return response;
            }
            switch (code) {
                case HttpURLConnection.HTTP_PARTIAL://请求部分网络成功
                    break;
                case HttpURLConnection.HTTP_OK://请求网络成功
                    lastPosition = 0;//请求部分网络失败
                    break;
            }
            inputStream = urlConnection.getInputStream();

            Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
            Map<String, String> convertHeaders = Util.convertHeaders(headerFields);
            CacheInfo cacheInfo = Util.parseCacheHeaders(convertHeaders);
            String setCookie = convertHeaders.get(HttpInfo.SET_COOKIE);
            Response<Type> response = mRequest.generateResponse();
            response.setCode(code)
                    .setResult("")
                    .setCacheInfo(cacheInfo)
                    .setCookie(setCookie)
                    .setCallback(mRequest.getCallback());

            String tempPath = getTempPath(url);
            File file = new File(tempPath);
            Util.createFileIfMissed(file);
            randomAccessFile = new RandomAccessFile(file, "rwd");
            randomAccessFile.seek(lastPosition);
            byte[] buffer = new byte[1024 * 1024 * 2];
            int len;
            int total = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                total += len;
                randomAccessFile.write(buffer, 0, len);
                long progress = total + lastPosition;
                int percent = (int) (((progress * 1f) / (maxLength * 1f)) * 100f);
                //正在下载
                onLoading(tempPath, percent);
                //本地记录文件下载量
                Victor.getInstance().getConfig().saveFileLoadingLength(url, progress);
                //手动打断线程执行
                boolean interrupted = Thread.interrupted();
                if (interrupted) {
                    return response;
                }
            }
            String path = getPath(url);
            Util.renameTo(file, new File(path));
            urlConnection.disconnect();
            //加载完毕
            onPostLoaded(path);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Response<Type> response = mRequest.generateResponse();
            response.setCode(HttpURLConnection.HTTP_BAD_REQUEST)
                    .setResult(e.toString())
                    .setCallback(mRequest.getCallback());
            onLoadingError(e.toString());
            return response;
        } finally {
            Util.close(inputStream);
            Util.close(randomAccessFile);
            mRequest.setCallback(null);
            mRequest = null;
        }
    }

    private String getTempPath(String url) {
        return getPath(url) + TEMP;
    }

    private String getPath(String url) {
        String path;
        if (url.startsWith(HttpInfo.HTTP)) {
            String fileName = Util.hashKeyFromUrl(url);
            path = mDirectory + fileName;
            //尽量添加后缀
            if (url.contains("/")) {
                String[] urlSplit = url.split("/");
                String name = urlSplit[urlSplit.length - 1];
                if (name.contains(".")) {
                    String[] nameSplit = name.split("\\.");
                    path = path + "." + nameSplit[nameSplit.length - 1];
                }
            }
        } else {
            path = url;
        }
        return path;
    }

    private boolean isLoaded(String tempPath) {
        File file = new File(tempPath);
        return file.exists() && file.isFile();
    }

    private void onPreLoading() {
        if (mDeliver != null && mRequest != null) {
            mDeliver.postResponse(new Runnable() {
                @Override
                public void run() {
                    Callback<Type> callback = mRequest.getCallback();
                    if (callback != null) {
                        callback.onPreLoading(mRequest.getUrl());
                    }
                }
            });
        }
    }

    private void onLoading(final String tempPath, final int progress) {
        if (mDeliver != null && mRequest != null) {
            mDeliver.postResponse(new Runnable() {
                @Override
                public void run() {
                    Callback<Type> callback = mRequest.getCallback();
                    if (callback != null) {
                        callback.onLoading(mRequest.getUrl(), tempPath, progress);
                    }
                }
            });
        }
    }

    private void onPostLoaded(final String filePath) {
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

    private void onLoadingError(final String errorInfo) {
        if (mDeliver != null && mRequest != null) {
            mDeliver.postResponse(new Runnable() {
                @Override
                public void run() {
                    Callback<Type> callback = mRequest.getCallback();
                    if (callback != null) {
                        callback.onLoadingError(mRequest.getUrl(), errorInfo);
                    }
                }
            });
        }
    }
}