package org.liaohailong.victor;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        findViewById(R.id.jump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VictorActivity.show(v.getContext());
            }
        });
    }

    private void init() {
        if (Util.requestPermissionIfNeed(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "", 0)) {
            //初始化基本配置
            Victor.getInstance().initConfig(getApplicationContext())
                    .createCacheDirectory("", 50 * 1024 * 1024)
                    .setConnectTimeout(3 * 1000)
                    .setReadTimeout(3 * 1000)
                    .setLogEnable(true)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public void process(Request<?> request) {
                            Log.i("Victor", request.toString());
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }
}
