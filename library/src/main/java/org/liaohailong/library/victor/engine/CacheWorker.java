package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.lrucache.DiskLruCache;
import org.liaohailong.library.victor.request.Request;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * Describe as: 缓存读取
 * Created by LiaoHaiLong on 2018/5/1.
 */

class CacheWorker {
    private static final int DISK_CACHE_INDEX = 0;
    private static DiskLruCache mDiskLruCache;
    private final static String mRootDirectoryPath;

    static {
        File rootDirectory = Victor.getInstance().getConfig().getRootDirectory();
        mRootDirectoryPath = rootDirectory.getAbsolutePath();

        int maxDiskCacheBytes = Victor.getInstance().getConfig().getMaxDiskCacheBytes();
        try {
            mDiskLruCache = DiskLruCache.open(rootDirectory, 1, 1, maxDiskCacheBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRootDirectoryPath() {
        return mRootDirectoryPath;
    }

    private static DiskLruCache getDiskLruCache() {
        return mDiskLruCache;
    }

    static void saveCache(Request<?> request, Response<?> response) {
        String cacheKey = request.getCacheKey();
        BufferedOutputStream bos = null;
        try {
            DiskLruCache.Editor edit = getDiskLruCache().edit(cacheKey);
            OutputStream outputStream = edit.newOutputStream(DISK_CACHE_INDEX);
            String rawResult = response.getRawResult();
            bos = new BufferedOutputStream(outputStream);
            bos.write(rawResult.getBytes());
            bos.flush();
            CacheInfo cacheInfo = response.getCacheInfo();
            Victor.getInstance().getConfig().addCacheInfo(cacheKey, cacheInfo);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.close(bos);
        }
    }

    static <T> Response<T> getCache(Request<T> request) {
        String cacheKey = request.getCacheKey();
        try {
            DiskLruCache.Snapshot snapshot = getDiskLruCache().get(cacheKey);
            if (snapshot == null) {
                return null;
            }
            String result = snapshot.getString(DISK_CACHE_INDEX);
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
