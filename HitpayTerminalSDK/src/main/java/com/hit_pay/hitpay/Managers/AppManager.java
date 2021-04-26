package com.hit_pay.hitpay.Managers;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.hit_pay.hitpay.Util.AppConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Nitin on 23/7/16.
 */
public class AppManager {

    private static final String TAG = "AppManager";
    public static final String SERVER_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DISPLAY_TIME_FORMAT = "dd MMM yyyy \n HH:mm";
    public static final String DISPLAY_TIME_FORMAT2 = "dd MMM yyyy HH:mm";

    public static void showErrorAlert(Context context, final String message) {
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Whatever...

                    }
                }).create().show();
    }

    public static void showErrorAlert(Context context, final String title, final String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Whatever...

                    }
                }).create().show();
    }

    public static void showNormalAlert(Context context, final String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Whatever...

                    }
                }).create().show();
    }

    public static int checkSighUpStatus(Context context) {

        String userId = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_USER_ID);
        Log.d(TAG,"user id "+userId);
        if ((userId == null) || (userId.length() < 1)) {
            return AppConstants.SIGN_UP_NO_USER;
        }
        String token = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_USER_TOKEN);
        Log.d(TAG,"user token"+token);
        if ((token == null) || (token.length() < 1)) {
            return AppConstants.SIGN_UP_NO_USER;
        }

        return AppConstants.SIGN_UP_OK;
    }

    public static Boolean isProfileMerchant(Context context){
        return true;
    }

    public static String convertDate(int dateInSecs) {
        Date time = new Date((long) dateInSecs * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");
        sdf.setTimeZone(TimeZone.getDefault());
        String newDateString = sdf.format(time);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        timeFormat.setTimeZone(TimeZone.getDefault());
        String newTimeString = timeFormat.format(time);
        newDateString = newDateString + "\n" + newTimeString;
        return newDateString;
    }

    public static String convertDateTime(String serverDateTime) { //Server day 2018-08-01T19:30:26Z
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat(SERVER_TIME_FORMAT);
            serverFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = serverFormat.parse(serverDateTime);
            SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_TIME_FORMAT);
            displayFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String convertDateTime2(String serverDateTime) { //Server day 2018-08-01T19:30:26Z
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat(SERVER_TIME_FORMAT);
            serverFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = serverFormat.parse(serverDateTime);
            SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_TIME_FORMAT2);
            displayFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Boolean canShowLockScreen(Context context) {

        String mobileNo = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_USER_MOBILE_NO);
        String token = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_USER_TOKEN);
        String name = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_USER_NAME);
        if (mobileNo != null && token != null && name != null) {
            return true;
        }

        return false;
    }

    public static ViewGroup getParent(View view) {
        return (ViewGroup) view.getParent();
    }

    public static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if (parent != null) {
            parent.removeView(view);
        }
    }

    public static void showKeyBoard(AppCompatEditText editText, Activity context) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    public static void hideKeyboard(Activity activity) {

        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(activity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    public static void replaceView(int optionId, View C, Activity context) {
        ViewGroup parent = (ViewGroup) C.getParent();
        int index = parent.indexOfChild(C);
        parent.removeView(C);
        C = context.getLayoutInflater().inflate(optionId, parent, false);
        parent.addView(C, index);
    }

//    public static boolean isServerMigrated(Context activity){

//        String status = ApplicationPreferencesManager.getStringValue(activity,AppConstants.PREF_SERVER_VERSION);
//        if (status == null){
//            return false;
//        }
//        if (status.equals("2")){
//            return true;
//        }
//        return true;
//    }

//    public static void checkServerStatus(final Context context , final OnComplete<String> onComplete){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String reqUrl = "https://hit-pay.com/api/v3/status";
//                try {
//                    HttpResponse response = HttpManager.getInstance().doGet(reqUrl, null, null);
//                    HttpEntity entity = response.getEntity();
//                    String responseString = EntityUtils.toString(entity, "UTF-8");
//                    System.out.println("" + response.getStatusLine().getStatusCode());
//                    System.out.println("" + responseString);
//
//                    JSONObject jsonObject = new JSONObject(responseString);
//                    int status = response.getStatusLine().getStatusCode();
//                    if (status == 200) {
//                        String serverStatus = jsonObject.getString("status");
//                        System.out.println("status is "+serverStatus);
//                        ApplicationPreferencesManager.setValue(context,AppConstants.PREF_SERVER_VERSION,serverStatus);
//                        onComplete.done("", null);
//                        return;
//                    } else{
//                        onComplete.done(null, "");
//                        return;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    onComplete.done(null, AppConstants.GENERAL_ERROR);
//                    return;
//                }
//
//            }
//        }).start();
//    }

    public static boolean isTutorialSeen(Context context) {
        String tutorial = ApplicationPreferencesManager.getStringValue(context, AppConstants.TUTORIAL_SEEN);
        if (tutorial == null) {
            return false;
        }
        return true;
    }

    public static boolean isKeyboardTutorialSeen(Context context) {
        // First check if already using keyboard
        if (isHitpayKeyboardEnabled(context)) {
            return true;
        }
        String tutorial = ApplicationPreferencesManager.getStringValue(context, AppConstants.KEYBOARD_TUTORIAL_SEEN);
        if (tutorial == null) {
            return false;
        }

        return true;
    }

    public static boolean isHitpayKeyboardEnabled(Context context) {
        InputMethodManager im = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        String list = im.getEnabledInputMethodList().toString();
        if (list.contains(AppConstants.KEYBOARD_ID)) {
            return true;
        }
        return false;
    }

    public static boolean isHitpayKeyboardDefault(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (id != null && id.equals(AppConstants.KEYBOARD_ID)) {
            return true;
        }
        return false;
    }

    public static void tutorialSeen(Context context) {
        ApplicationPreferencesManager.setValue(context, AppConstants.TUTORIAL_SEEN, "seen");
    }

    public static void keyboardTutorialSeen(Context context) {
        ApplicationPreferencesManager.setValue(context, AppConstants.KEYBOARD_TUTORIAL_SEEN, "seen");
    }


    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }


    public static String enableFingerPrint(Activity activity){
        // check if device is capable
        String error = isDeviceFingerprintCapable(activity);
        if (error != null) return error;

        // Check if password in stored
        String password = ApplicationPreferencesManager.getStringValue(activity,AppConstants.PREF_USER_PASSWORD);
        if (password == null || password.length() == 0){
            error = "Please Sign Out and Log In again to enable Fingerprint Authentication";
            return error;
        }

        ApplicationPreferencesManager.setValue(activity,AppConstants.PREF_FINGER_PRINT_ENABLED,"true");
        return null;
    }

    public static void disableFingerPrintAuth(Activity activity){
        ApplicationPreferencesManager.setValue(activity,AppConstants.PREF_FINGER_PRINT_ENABLED,"false");
    }

    public static boolean canAuthenticateFingerprint(Activity activity) {

        // FIRST CHECK IF THE SETTINGS IS ENABLED BY THE USER
        String pref = ApplicationPreferencesManager.getStringValue(activity,AppConstants.PREF_FINGER_PRINT_ENABLED);
        if (pref == null){
            ApplicationPreferencesManager.setValue(activity,AppConstants.PREF_FINGER_PRINT_ENABLED,"true");
        }else {
            if (pref.equals("false")) return false;
        }

        // Check if password is saved
        String pwd= ApplicationPreferencesManager.getStringValue(activity,AppConstants.PREF_USER_PASSWORD);
        if (pwd == null || pwd.length() == 0) return false;

        // Check if device is ready for fingerprint
        String error =  isDeviceFingerprintCapable(activity);
        return (error == null);

    }

    public static String isDeviceFingerprintCapable(Activity activity){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
            KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(activity.KEYGUARD_SERVICE);
            if (!keyguardManager.isKeyguardSecure()) return "Lock screen settings not enabled"; // Lock screen settings not enabled
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("failed to get finger print permission");
                return "Fingerprint permission denied. Grant permission to enable Fingerprint Authentication";
            }
            if (fingerprintManager.hasEnrolledFingerprints()) {
                return null;
            }

            return "Fingerprint not enabled";

        }else{
            return "Device not capable for Fingerprint Authentication";
        }
    }

    public static String perfectDecimal(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL){
        if(str.charAt(0) == '.') str = "0"+str;
        int max = str.length();

        String rFinal = "";
        boolean after = false;
        int i = 0, up = 0, decimal = 0; char t;
        while(i < max){
            t = str.charAt(i);
            if(t != '.' && after == false){
                up++;
                if(up > MAX_BEFORE_POINT) return rFinal;
            }else if(t == '.'){
                after = true;
            }else{
                decimal++;
                if(decimal > MAX_DECIMAL)
                    return rFinal;
            }
            rFinal = rFinal + t;
            i++;
        }return rFinal;
    }

}
