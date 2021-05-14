package com.hit_pay.hitpay.ClientAPI;


import android.content.Context;
import android.content.Intent;

import com.hit_pay.hitpay.activity.HitPayLoginPageActivity;

public class Hitpay implements HitPayAuthenticationListener {
    private static Context mContect;

    // add a private listener variable
    public static HitPayAuthenticationListener mListener = null;

    // provide a way for another class to set the listener
    public static void setHitPayAuthenticationListener(HitPayAuthenticationListener listener) {
        mListener = listener;
    }

    public static void init(Context context) {
        mContect = context;
    }

    public static void initiateAuthentication() {
        mContect.startActivity(new Intent(mContect, HitPayLoginPageActivity.class));
    }

    @Override
    public void authenticationCompleted(boolean status) {
        authenticationCompleted(status);
    }
}
