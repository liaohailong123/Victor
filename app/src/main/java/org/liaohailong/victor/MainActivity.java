package org.liaohailong.victor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.VictorConfig;
import org.liaohailong.library.victor.callback.FileCallback;
import org.liaohailong.library.victor.callback.HttpCallback;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

public class MainActivity extends AppCompatActivity {

    private static final String url = "https://z.hidajian.com/api/charts/wall_data";
    private TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text_view);
        init();
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
            //初始化基本配置
            VictorConfig victorConfig = Victor.getInstance().initConfig(getApplicationContext());
            victorConfig.setCacheMaxSize(50 * 1024 * 1024)
                    .setConnectTimeout(3 * 1000)
                    .setReadTimeout(3 * 1000)
                    .setLogEnable(true)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public void process(Request<?> request) {
                            Log.i("Victor", request.toString());
                        }
                    });

            /*refreshStartTime();
            for (int i = 0; i < 4000; i++) {
                doRequest(i);
            }*/

            loadFile();
        }
    }

    private void doRequest(int offset) {
        int wallId = 2000 + offset;
        final String wallIdStr = String.valueOf(wallId);
        Request<JsonObject> request = Victor.getInstance().newTextRequest()
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

    private void loadFile() {
        Victor.getInstance().newFileRequest()
                .setUrl("http://z.hidajian.com/Public/Api/images/charts/miaopai_1.mp4")
                .doGet()
                .setConnectTimeOut(30 * 1000)
                .setReadTimeOut(30 * 1000)
                .setCallback(new FileCallback() {
                    @Override
                    public void onPreLoading(String url) {
                        Toast.makeText(mTextView.getContext(), "onPreLoading", Toast.LENGTH_LONG).show();
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLoading(String url, String tempFilePath, int progress) {
                        mTextView.setText("url = " + url + "     progress = " + progress + "%");
                    }

                    @Override
                    public void onPostLoaded(String url, String filePath) {
                        Toast.makeText(mTextView.getContext(), "onPostLoaded filePath = " + filePath, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onLoadingError(String url, String info) {
                        Toast.makeText(mTextView.getContext(), "onLoadingError", Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Victor.getInstance().restore();
    }
}
