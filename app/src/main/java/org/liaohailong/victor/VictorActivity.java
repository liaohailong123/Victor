package org.liaohailong.victor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.liaohailong.library.victor.RequestPriority;
import org.liaohailong.library.victor.Util;
import org.liaohailong.library.victor.Victor;
import org.liaohailong.library.victor.callback.FileCallback;
import org.liaohailong.library.victor.callback.TextCallback;
import org.liaohailong.library.victor.interceptor.Interceptor;
import org.liaohailong.library.victor.request.Request;

import java.io.File;

/**
 * Describe as: 测试
 * Created by LiaoHaiLong on 2018/5/3.
 */

public class VictorActivity extends AppCompatActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, VictorActivity.class);
        context.startActivity(intent);
    }

    private static final String url = "https://z.hidajian.com/api/charts/wall_data";
    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victor);
        mTextView = findViewById(R.id.text_view);
        Button startBtn = findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });
    }

    private final static int count = 2000;
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
        mTextView.setText("一共耗时 " + seconds + " 秒" + "   一共发送" + count + "请求个任务");
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
        /*refreshStartTime();
        for (int i = 0; i < count; i++) {
            doRequest(i);
        }*/
        loadFile();
    }

    private void doRequest(int offset) {
        int wallId = 1 + offset;
        final String wallIdStr = String.valueOf(wallId);
        Victor.getInstance().newTextRequest()
                .doPost()
                .setUrl(url)
                .setUseCache(true)
                .setUseCookie(true)
                .addParam("wall", wallIdStr)
                .setCallback(new TextCallback<JsonObject>() {
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
    }

    private void loadFile() {
        Victor.getInstance().newDownloadRequest()
                .setUrl("http://z.hidajian.com/Public/Api/images/charts/miaopai_1.mp4")
                .doGet()
                .setConnectTimeOut((int) DateUtils.DAY_IN_MILLIS)
                .setReadTimeOut((int) DateUtils.DAY_IN_MILLIS)
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
                    public void onPostLoaded(String url, String resultInfo) {
                        Toast.makeText(mTextView.getContext(), "onPostLoaded filePath = " + resultInfo, Toast.LENGTH_LONG).show();
                        uploadFile(resultInfo);
                    }

                    @Override
                    public void onLoadingError(String url, String info) {
                        Toast.makeText(mTextView.getContext(), "onLoadingError info = " + info, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadFile(String filePath) {
        //理论上最丰富的url
        //协议方案  登录信息       ip地址   端口号    文件路径  请求参数    片段标识符
        // http://user:password@192.168.1.38:8080/xxx/xxx/xxx?query=1&name=2#ch1
        Victor.getInstance().newUploadRequest()
                .setUrl("http://192.168.1.155/Adc/public/upload_file")
                .addFile("filename", new File(filePath))
                .addParam("params", "{\"type\":\"media\"}")
                .setConnectTimeOut((int) DateUtils.DAY_IN_MILLIS)
                .setReadTimeOut((int) DateUtils.DAY_IN_MILLIS)
                .setRequestPriority(RequestPriority.HIGHT)
                .setCallback(new FileCallback() {

                    @Override
                    public void onPreLoading(String url) {
                        Toast.makeText(mTextView.getContext(), "文件上传开始了", Toast.LENGTH_LONG).show();
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLoading(String url, String tempFilePath, int progress) {
                        mTextView.setText("url = " + url + "     文件上传进度 = " + progress + "%");
                    }

                    @Override
                    public void onPostLoaded(String url, String resultInfo) {
                        Toast.makeText(mTextView.getContext(), "文件上传完毕", Toast.LENGTH_LONG).show();
                        mTextView.setText(resultInfo);
                    }

                    @Override
                    public void onLoadingError(String url, String info) {
                        Toast.makeText(mTextView.getContext(), "文件上传失败 info = " + info, Toast.LENGTH_LONG).show();
                    }

                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Victor.getInstance().release();
    }
}
