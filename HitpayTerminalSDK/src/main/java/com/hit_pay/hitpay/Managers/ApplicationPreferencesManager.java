package com.hit_pay.hitpay.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hit_pay.hitpay.Util.AppConstants;

/**
 * Created by Nitin on 23/7/16.
 */
public class ApplicationPreferencesManager {

    private static final String TAG = "AppPref";
    public static void setValue(Context activity, String key, String value) {

        if(key.equals(AppConstants.PREF_PROFILE_PIC) || key.equals(AppConstants.PREF_SERVER_VERSION)){
            SharedPreferences sharedPref = activity.getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE_PIC, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(key, value);
            editor.apply();
            return;
        }

        SharedPreferences sharedPref = activity.getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringValue(Context activity, String key) {


        try {
            if(key.equals(AppConstants.PREF_PROFILE_PIC) || key.equals(AppConstants.PREF_SERVER_VERSION)){
                SharedPreferences sharedPref = activity.getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE_PIC, Context.MODE_PRIVATE);
                return sharedPref.getString(key, null);
            }else {
                SharedPreferences sharedPref = activity.getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
                return sharedPref.getString(key, null);
            }
        } catch (Exception ex) {
            return null;
        }
    }


    public static void clearTempValues(Context activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

}
