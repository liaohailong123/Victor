package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;
import org.liaohailong.library.victor.HttpInfo;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.request.Request;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Describe as : 文件文件下载/上传处理器
 * Created by LHL on 2018/5/2.
 */

class FileDownLoader<Type> extends FileLoader<Type> {
    private static final String TEMP = ".temp";
    private static final String mDirectory;

    static {
        mDirectory = Victor.getInstance().getConfig().getRootDirectory().getAbsolutePath() + "/";
    }

    FileDownLoader(Request<Type> request, Deliver deliver) {
        super(request, deliver);
    }


    @Override
    Response<Type> doWork() {
        onPreLoading();
        try {
            String url = mRequest.getUrl();
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod(HttpInfo.HEAD);
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
                onLoadingError("code = " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mRequest.setCallback(null);
            mRequest = null;
            onLoadingError(e.toString());
            return null;
        }
    }

    private Response<Type> downLoad(long lastPosition, int maxLength) {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
            if (mRequest.isCanceled()) {
                return null;
            }
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
                InputStream errorStream = urlConnection.getErrorStream();
                String errorInfo = Util.streamToString(errorStream);
                onLoadingError(errorInfo);
                return null;
            }
            switch (code) {
                case HttpURLConnection.HTTP_PARTIAL://请求部分网络成功
                    break;
                case HttpURLConnection.HTTP_OK://请求网络成功
                    lastPosition = 0;//请求部分网络失败
                    break;
            }
            inputStream = urlConnection.getInputStream();

            String tempPath = getTempPath(url);
            File file = new File(tempPath);
            Util.createFileIfMissed(file);
            randomAccessFile = new RandomAccessFile(file, "rwd");
            randomAccessFile.seek(lastPosition);
            byte[] buffer = new byte[8 * 1024];
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
                    return null;
                }
                if (mRequest.isCanceled()) {
                    return null;
                }
            }
            String path = getPath(url);
            Util.renameTo(file, new File(path));
            urlConnection.disconnect();
            //加载完毕
            onPostLoaded(path);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            onLoadingError(e.toString());
            return null;
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

    static String getPath(String url) {
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

    static boolean isLoaded(String tempPath) {
        File file = new File(tempPath);
        return file.exists() && file.isFile();
    }
}
