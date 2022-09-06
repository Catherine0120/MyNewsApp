package com.java.zhangshiying;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

public class GlobalApplication extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext;
    }
}
