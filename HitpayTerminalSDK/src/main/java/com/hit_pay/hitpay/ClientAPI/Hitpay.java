package com.hit_pay.hitpay.ClientAPI;


import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AlertDialog;

import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.activity.HitPayLoginPageActivity;
import com.hit_pay.hitpay.terminal.TerminalActivity;

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

    public static void initiateTerminalSetup() {
        mContect.startActivity(new Intent(mContect, TerminalActivity.class));
    }

    public static boolean verifyGpsEnabled(Context context) {
        final LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        try {
            gpsEnabled = locationManager != null &&
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception exception) {
        }
        if (!gpsEnabled) {
            // notify user
            new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_MaterialComponents_DayNight_DarkActionBar))
                    .setMessage("Please enable location services")
                    .setCancelable(false)
                    .setPositiveButton("Open location settings", (dialog, which) -> {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .create()
                    .show();
        }

        return gpsEnabled;
    }
    @Override
    public void authenticationCompleted(boolean status) {
        authenticationCompleted(status);
    }
}
