package com.poynt.ratingsample;

import android.app.Application;

/**
 * Created by dennis on 1/30/17.
 */

public class RatingSampleApplication extends Application {
    public static RatingSampleApplication instance;

    public static RatingSampleApplication getInstance() {
        return instance;
    }

    SecondScreenManager secondScreenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        secondScreenManager = SecondScreenManager.getInstance(this);
    }

    public SecondScreenManager getSecondScreenManager() {
        return secondScreenManager;
    }
}
