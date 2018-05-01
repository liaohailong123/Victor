package org.liaohailong.victor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.callback.HttpCallback;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

public class MainActivity extends AppCompatActivity {

    private static final String url = "https://z.hidajian.com/api/charts/wall_data";
    private TextView mTextView;

    private int mIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text_view);
        init();
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

            mIndex = 0;
            for (int i = 0; i < 4000; i++) {
                doRequest(i);
            }
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
                        mIndex++;
                        mTextView.setText("onSuccess wallId = " + wallIdStr + "   \n" + result.toString());
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(int code, String error) {
                        mTextView.setText("onFailure wallId = " + wallIdStr);
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
