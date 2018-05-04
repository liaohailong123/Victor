package org.liaohailong.victor;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
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
        //初始化需要SD卡写入权限，控制缓存
        if (Util.requestPermissionIfNeed(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "", 0)) {
            String cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            //初始化基本配置
            Victor.getInstance().initConfig(getApplicationContext())//生成全局配置对象
                    .createCacheDirectory(cacheDir, 50 * 1024 * 1024)//创建缓存路径，xxx/xxx/victor；设置最大存储容量
                    .setConnectTimeout(3 * 1000)//全局请求默认的连接超时
                    .setReadTimeout(3 * 1000)//全局请求默认的读取超时
                    .setLogEnable(true)//是否开启Log打印
                    .addInterceptor(new Interceptor() {
                        @Override
                        public void process(Request<?> request) {
                            Log.i("Victor", request.toString());
                        }
                    });//设置拦截器，所有通过网络的请求都会回调
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }
}
