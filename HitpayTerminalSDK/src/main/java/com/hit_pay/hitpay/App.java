package com.hit_pay.hitpay;

import android.app.Application;

import com.stripe.stripeterminal.TerminalLifecycleObserver;


public class App extends Application {
    private TerminalLifecycleObserver observer;

    @Override
    public void onCreate() {
        super.onCreate();
        observer = TerminalLifecycleObserver.getInstance();
        registerActivityLifecycleCallbacks(observer);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        observer.onTrimMemory(level, this);
    }
}
