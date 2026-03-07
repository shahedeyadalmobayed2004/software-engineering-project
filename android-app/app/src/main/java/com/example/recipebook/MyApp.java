package com.example.recipebook;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CloudinaryHelper.init(getApplicationContext());
    }
}