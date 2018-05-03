package org.liaohailong.victor;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Describe as : Victor APP
 * Created by LHL on 2018/5/3.
 */

public class VictorApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
