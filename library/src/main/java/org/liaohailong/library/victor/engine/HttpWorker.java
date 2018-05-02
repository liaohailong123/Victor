package org.liaohailong.library.victor.engine;

import android.text.TextUtils;

import org.liaohailong.library.victor.HttpConnectSetting;
import org.liaohailong.library.victor.HttpField;
import org.liaohailong.library.victor.HttpInfo;
import org.liaohailong.library.victor.Response;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.request.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Describe as: 网络请求
 * Created by LiaoHaiLong on 2018/5/1.
 */

class HttpWorker<Type> {
    Request<Type> mRequest;

    static <T> HttpWorker create(Request<T> request) {
        return new HttpWorker<>(request);
    }

    HttpWorker(Request<Type> request) {
        mRequest = request;
    }

    Response<Type> doWork() {
        try {
            URL url = new URL(mRequest.getUrl());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            setConnection(httpURLConnection);
            addHeaders(httpURLConnection);
            postParamsIfNeed(httpURLConnection);
            int responseCode = httpURLConnection.getResponseCode();

            InputStream inputStream;
            if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = httpURLConnection.getInputStream();
            } else {
                inputStream = httpURLConnection.getErrorStream();
            }

            String enc = httpURLConnection.getContentEncoding();
            // 注意这里 ↓
            if (enc != null && enc.equals(HttpInfo.GZIP)) {
                inputStream = new java.util.zip.GZIPInputStream(inputStream);
            }

            Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();
            Map<String, String> convertHeaders = Util.convertHeaders(headerFields);
            CacheInfo cacheInfo = Util.parseCacheHeaders(convertHeaders);

            String setCookie = convertHeaders.get(HttpInfo.SET_COOKIE);

            String result = Util.streamToString(inputStream);
            Response<Type> response = mRequest.generateResponse();
            response.setCode(responseCode)
                    .setResult(result)
                    .setCacheInfo(cacheInfo)
                    .setCookie(setCookie)
                    .setCallback(mRequest.getCallback());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Response<Type> response = mRequest.generateResponse();
            response.setCode(HttpURLConnection.HTTP_BAD_REQUEST)
                    .setResult(e.toString())
                    .setCallback(mRequest.getCallback());
            return response;
        } finally {
            mRequest.setCallback(null);
            mRequest = null;
        }
    }

    void setConnection(HttpURLConnection connect) {
        try {
            String httpMethod = mRequest.getHttpMethod();
            connect.setRequestMethod(httpMethod);
            connect.setDoInput(true);
            connect.setDoOutput(mRequest.isPost());

            HttpConnectSetting httpConnectSetting = mRequest.getHttpConnectSetting();
            connect.setConnectTimeout(httpConnectSetting.getConnectTimeout());
            connect.setReadTimeout(httpConnectSetting.getReadTimeout());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addHeaders(HttpURLConnection connection) {
        HttpField httpHeader = mRequest.getHttpHeader();

        for (Map.Entry<String, String> entry : httpHeader.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                continue;
            }
            connection.addRequestProperty(key, value);
        }
    }

    void postParamsIfNeed(HttpURLConnection connection) {
        HttpField httpParams = mRequest.getPostParams();
        Map<String, String> params = httpParams.getParams();
        if (params.isEmpty()) {
            return;
        }

        HttpField httpHeader = mRequest.getHttpHeader();
        boolean hasContentType = false;
        for (Map.Entry<String, String> entry : httpHeader.entrySet()) {
            String key = entry.getKey();
            if (TextUtils.equals(HttpInfo.CONTENT_TYPE, key)) {
                hasContentType = true;
                break;
            }
        }
        if (!hasContentType) {
            connection.addRequestProperty(HttpInfo.CONTENT_TYPE, HttpInfo.X_WWW_FORM_URLENCODE);
        }

        if (!mRequest.isPost()) {
            return;
        }

        String postParameters = Util.createQueryStringForParameters(params);
        try {
            connection.setFixedLengthStreamingMode(postParameters.getBytes(HttpInfo.UTF_8).length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    connection.getOutputStream(), HttpInfo.UTF_8));
            out.print(postParameters);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
