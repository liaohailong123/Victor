package org.liaohailong.library.victor.engine;

import android.text.TextUtils;

import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.request.Request;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.HttpURLConnection;

/**
 * Describe as: 缓存读取
 * Created by LiaoHaiLong on 2018/5/1.
 */

class CacheWorker {
    private static String mRootDirectoryPath;

    private static String getRootDirectoryPath() {
        if (TextUtils.isEmpty(mRootDirectoryPath)) {
            File rootDirectory = Victor.getInstance().getConfig().getRootDirectory();
            mRootDirectoryPath = rootDirectory.getAbsolutePath();
        }
        return mRootDirectoryPath;
    }

    static void saveCache(Request<?> request, Response<?> response) {
        String cacheFileName = request.getCacheFileName();
        File file = new File(getRootDirectoryPath(), cacheFileName);
        String rawResult = response.getRawResult();
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(rawResult.getBytes());
            bos.flush();
            CacheInfo cacheInfo = response.getCacheInfo();
            Victor.getInstance().getConfig().addCacheInfo(cacheFileName, cacheInfo);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.close(bos);
        }
    }

    static <T> Response<T> getCache(Request<T> request) {
        String cacheFileName = request.getCacheFileName();
        File file = new File(getRootDirectoryPath(), cacheFileName);

        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(file));
            String readLine;
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
            String result = sb.toString();
            return request.generateResponse()
                    .setCode(HttpURLConnection.HTTP_OK)
                    .setNetworkTimeMs(0)
                    .setResult(result)
                    .setCallback(request.getCallback());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
