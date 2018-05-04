package org.liaohailong.library.victor;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;

import org.liaohailong.library.victor.engine.CacheInfo;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Describe as: Victor请求网络库的工具类
 * Created by LiaoHaiLong on 2018/5/1.
 */

public final class Util {

    private Util() throws IllegalAccessException {
        throw new IllegalAccessException("no instance!");
    }

    private static final int FILE_STREAM_BUFFER_SIZE = 32;
    private static final Gson GSON = new Gson();
    private static final JsonParser PARSER = new JsonParser();


    static boolean isNetEnable(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return network != null && network.isAvailable();
    }

    public static String hashKeyFromUrl(String url) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = byteToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private static String byteToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);//得到十六进制字符串
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    public static String createQueryStringForParameters(Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();
        if (parameters != null) {
            boolean firstParameter = true;

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (!firstParameter) {
                    sb.append(HttpInfo.PARAMETER_DELIMITER);
                }

                String name = entry.getKey();
                String value = entry.getValue();
                sb.append(URLEncoder.encode(name))
                        .append(HttpInfo.PARAMETER_EQUALS_CHAR)
                        .append(!TextUtils.isEmpty(value) ? URLEncoder.encode(value) : "");

                firstParameter = false;
            }
        }
        return sb.toString();
    }

    /**
     * 转换Stream成string
     *
     * @param is Stream源
     * @return 目标String
     */
    @NonNull
    public static String streamToString(@NonNull InputStream is) {
        return streamToString(is, Xml.Encoding.UTF_8.toString());
    }

    /**
     * 按照特定的编码格式转换Stream成string
     *
     * @param is  Stream源
     * @param enc 编码格式
     * @return 目标String
     */
    @NonNull
    private static String streamToString(@NonNull InputStream is, String enc) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            int availableLength = is.available();
            int count;
            if (availableLength < FILE_STREAM_BUFFER_SIZE)
                availableLength = FILE_STREAM_BUFFER_SIZE;
            final byte[] data = new byte[availableLength];
            while ((count = is.read(data)) > 0) {
                os.write(data, 0, count);
            }
            return new String(os.toByteArray(), Xml.Encoding.UTF_8.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            close(os);
            close(is);
        }
        return "";
    }

    public static void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取cls中的泛型参数类型。参考自{@link TypeToken#getSuperclassTypeParameter(Class)}。
     * <br/>注意：泛型类本身不能获取参数类型（因为被擦除了），只有泛型类的子类才可以获取。
     * <br/>比如ArrayList&lt;String&gt;无法通过ArrayList.class获取String类型；
     * <br/>但如果class MyList extends ArrayList&lt;String&gt;，那么MyList.class是可以获取String类型的。
     */
    public static Type getClassTypeParameter(Class<?> cls) {
        Type superclass = cls.getGenericSuperclass();
        while (superclass instanceof Class) {
            if (superclass == Object.class) {
                throw new RuntimeException(cls.getName() + " extends " + cls.getSuperclass().getName() + ": missing type parameter.");
            } else {
                superclass = ((Class) superclass).getGenericSuperclass();
            }
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    public static String getCacheFileName(String url, String method, Map<String, String> params) {
        String postParameters = Util.createQueryStringForParameters(params);
        return url + ":" + method + ":" + postParameters;
    }

    public static Map<String, String> convertHeaders(Map<String, List<String>> headers) {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            //请求行 key = null需要跳过
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            List<String> value = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (String s : value) {
                sb.append(s).append(";");
            }
            int index = sb.lastIndexOf(";");
            sb.deleteCharAt(index);
            String valueStr = sb.toString();
            result.put(key, valueStr);
        }
        return result;
    }

    public static CacheInfo parseCacheHeaders(Map<String, String> headers) {
        long now = System.currentTimeMillis();

        long serverDate = 0;
        long lastModified = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long finalExpire = 0;
        long maxAge = 0;
        long staleWhileRevalidate = 0;
        boolean hasCacheControl = false;
        boolean mustRevalidate = false;

        String serverEtag;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Cache-Control");
        if (headerValue != null) {
            hasCacheControl = true;
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equals("no-cache") || token.equals("no-store")) {
                    return null;
                } else if (token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                    }
                } else if (token.startsWith("stale-while-revalidate=")) {
                    try {
                        staleWhileRevalidate = Long.parseLong(token.substring(23));
                    } catch (Exception e) {
                    }
                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    mustRevalidate = true;
                }
            }
        }

        headerValue = headers.get("Expires");
        if (headerValue != null) {
            serverExpires = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Last-Modified");
        if (headerValue != null) {
            lastModified = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        // Cache-Control takes precedence over an Expires header, even if both exist and Expires
        // is more restrictive.
        if (hasCacheControl) {
            softExpire = now + maxAge * 1000;
            finalExpire = mustRevalidate
                    ? softExpire
                    : softExpire + staleWhileRevalidate * 1000;
        } else if (serverDate > 0 && serverExpires >= serverDate) {
            // Default semantic for Expire header in HTTP specification is softExpire.
            softExpire = now + (serverExpires - serverDate);
            finalExpire = softExpire;
        }

        CacheInfo entry = new CacheInfo();
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = finalExpire;
        entry.serverDate = serverDate;
        entry.lastModified = lastModified;
        entry.responseHeaders = headers;
        return entry;
    }

    private static long parseDateAsEpoch(String dateStr) {
        return Date.parse(dateStr);
    }

    static String objectToJsonString(Object obj) {
        try {
            return GSON.toJson(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static <T> T jsonStringToObject(String jsonStr, @NonNull Type type) {
        try {
            return GSON.fromJson(jsonStr, type);
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonObject stringToJson(String str) {
        try {
            return PARSER.parse(str).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> List<T> jsonArrayToGenericList(JsonArray array, @NonNull Type elementType) {
        List<T> result = new ArrayList<>();
        if (array == null) {
            return result;
        }

        try {
            for (JsonElement element : array) {
                //noinspection unchecked
                result.add((T) GSON.fromJson(element, elementType));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> T jsonToGenericObject(JsonElement json, @NonNull Type type) {
        try {
            return GSON.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 如果需要请求权限，则弹框提示，并作出需要权限的解释（以toast展示）
     *
     * @param activity
     * @param permission  权限
     * @param explanation 解释
     * @param requestCode 触发requestPermissions后，在onRequestPermissionsResult里用到（区分是在请求哪组权限）
     * @return 如果为true，表示已经获取了这些权限；false表示还未获取，并正在执行请求
     */
    public static boolean requestPermissionIfNeed(Activity activity,
                                                  String permission, CharSequence explanation, int requestCode) {
        return requestPermissionIfNeed(activity, new String[]{permission}, explanation, requestCode);
    }

    /**
     * 如果需要请求权限，则弹框提示，并作出需要权限的解释（以toast展示）
     *
     * @param activity
     * @param permissions 权限
     * @param explanation 解释
     * @param requestCode 触发requestPermissions后，在onRequestPermissionsResult里用到（区分是在请求哪组权限）
     * @return 如果为true，表示已经获取了这些权限；false表示还未获取，并正在执行请求
     */
    private static boolean requestPermissionIfNeed(Activity activity,
                                                   String[] permissions, CharSequence explanation, int requestCode) {
        ArrayList<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if (permissionList.isEmpty()) {
            return true;
        }

        String[] needRequestPermissions = permissionList.toArray(new String[permissionList.size()]);

        for (String permission : needRequestPermissions) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                if (!TextUtils.isEmpty(explanation)) {
                    Toast.makeText(activity, explanation, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

        ActivityCompat.requestPermissions(activity,
                needRequestPermissions,
                requestCode);

        return false;
    }

    /**
     * 创建文件（如果不存在）
     *
     * @param file 目标文件
     */
    public static void createFileIfMissed(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                boolean delete = file.delete();
            }
        }
        if (!file.exists()) {
            try {
                boolean mkdir = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件或目录重命名。如果失败，则打出error级别的log
     *
     * @param srcFile 原始文件或目录
     * @param dstFile 重命名后的文件或目录
     * @return 成功与否
     */
    public static boolean renameTo(File srcFile, @Nullable File dstFile) {
        if (srcFile == null || dstFile == null) {
            return false;
        }
        if (!srcFile.renameTo(dstFile)) {
            Log.e("Victor", "FileUtil cannot rename " + srcFile + " to " + dstFile);
            return false;
        }
        return true;
    }
}
