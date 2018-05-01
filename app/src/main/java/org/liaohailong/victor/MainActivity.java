package org.liaohailong.victor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.liaohailong.library.victor.HttpInfo;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.callback.HttpCallback;
import org.liaohailong.library.victor.engine.CacheInfo;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String url = "https://z.hidajian.com/api/charts/wall_data";
    private TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text_view);
        init();
//        init2();
    }

    long start;
    long end;

    private void refreshStartTime() {
        start = System.currentTimeMillis();
    }

    private void refreshEndTime() {
        end = System.currentTimeMillis();
    }

    @SuppressLint("SetTextI18n")
    private void showTimeCost() {
        long during = end - start;
        long seconds = during / 1000;
        mTextView.setText("一共耗时 " + seconds + " 秒");
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshEndTime();
            showTimeCost();
        }
    };

    private void postTimeCost() {
        mTextView.removeCallbacks(runnable);
        mTextView.postDelayed(runnable, 6000);
    }

    private void init() {
        if (Util.requestPermissionIfNeed(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "", 0)) {
            Victor.getInstance()
                    .initConfig(getApplicationContext())
                    .setCacheMaxSize(50 * 1024 * 1024)
                    .setConnectTimeout(3 * 1000)
                    .setReadTimeout(3 * 1000)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public void process(Request<?> request) {
                            Log.i("Victor", request.toString());
                        }
                    });

            refreshStartTime();
            for (int i = 0; i < 4000; i++) {
                doRequest(i);
            }
        }
    }

    private void init2() {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        refreshStartTime();
        for (int i = 0; i < 4000; i++) {
            int wallId = 2000 + i;
            final String wallIdStr = String.valueOf(wallId);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        try {
                            URL url = new URL(MainActivity.url);
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                            try {
                                httpURLConnection.setRequestMethod("POST");
                                httpURLConnection.setDoInput(true);
                                httpURLConnection.setDoOutput(true);

                                httpURLConnection.setConnectTimeout(3000);
                                httpURLConnection.setReadTimeout(3000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Map<String, String> params = new HashMap<>();
                            params.put("wall", wallIdStr);
                            String postParameters = Util.createQueryStringForParameters(params);
                            try {
                                httpURLConnection.setFixedLengthStreamingMode(postParameters.getBytes(HttpInfo.UTF_8).length);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            PrintWriter out = null;
                            try {
                                out = new PrintWriter(new OutputStreamWriter(
                                        httpURLConnection.getOutputStream(), HttpInfo.UTF_8));
                                out.print(postParameters);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (out != null) {
                                    out.close();
                                }
                            }

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
                            final String result = Util.streamToString(inputStream);

                            mTextView.post(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {
                                    mTextView.setText("onSuccess wallId = " + wallIdStr + "   \n" + result);
                                    postTimeCost();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    private void doRequest(int offset) {
        int wallId = 2000 + offset;
        final String wallIdStr = String.valueOf(wallId);
        Request<?> request = Victor.getInstance().newRequest()
                .doPost()
                .setUrl(url)
                .setUseCache(true)
                .setUseCookie(true)
                .addParam("wall", wallIdStr)
                .setCallback(new HttpCallback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(JsonObject result) {
                        mTextView.setText("onSuccess wallId = " + wallIdStr + "   \n" + result.toString());
                        postTimeCost();
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(int code, String error) {
                        mTextView.setText("onFailure wallId = " + wallIdStr);
                        postTimeCost();
                    }
                });
//        request.cancel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }
}
