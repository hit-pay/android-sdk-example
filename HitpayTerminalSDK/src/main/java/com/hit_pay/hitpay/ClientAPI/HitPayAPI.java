package com.hit_pay.hitpay.ClientAPI;

import android.content.Context;
import android.util.Log;

import com.hit_pay.hitpay.Managers.ApplicationPreferencesManager;
import com.hit_pay.hitpay.Util.AppConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by Nitin on 23/7/16.
 */
public class HitPayAPI {
    public String version = "?version=2";
    public String userId;
    public String businessId;
    public String token;
    public Context context;
    public String baseUrl;
    //    public static String HitPayAPI.this.baseUrl = "http://staging.hit-pay.com/api/v3";
//    public static String HitPayAPI.this.baseUrl = "https://www.hit-pay.com/api/v3";
    public static String TAG = "HitPayAPI";

    public HitPayAPI(Context context) {
        this.userId = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_USER_ID);
        this.businessId = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_BUSSINESS_ID);
        this.token = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_USER_TOKEN);
        this.context = context;
        String baseUrl = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_BASE_URL);
        if (baseUrl == null) {
            this.baseUrl = AppConstants.BASE_URL;
        } else {
            this.baseUrl = baseUrl;
        }
//        this.baseUrl = "https://pos.hit-pay.com/api";
//        this.baseUrl = "https://pos-staging.hit-pay.com/api";
    }

    public HashMap<String, String> getAuthHeader() {
        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Accept", "application/json");
        authHeader.put("Authorization", "Bearer " + token);
        return authHeader;
    }

    /**
     * Used for sign up only
     *
     * @return
     */
    public HashMap<String, String> getAuthTempHeader() {
        HashMap<String, String> authHeader = new HashMap<>();
        String token = ApplicationPreferencesManager.getStringValue(context, AppConstants.PREF_USER_TEMP_TOKEN);
        authHeader.put("Accept", "application/json");
        authHeader.put("Authorization", "Bearer " + token);
        return authHeader;
    }


    public void doSignUp(final String fullName, final String password, final String inviteCode, final Boolean merchant, final OnComplete<Boolean> onComplete) {

        new Thread(new Runnable() {
            @Override
            public void run() {


                String reqUrl = HitPayAPI.this.baseUrl + "/client";
                Log.d(TAG, " sending req");
                HashMap<String, String> params = new HashMap<>();
                params.put("name", fullName);
                params.put("passcode", password);
                if (inviteCode != null && inviteCode.length() > 0) {
                    params.put("referral_code", inviteCode);
                }
                if (merchant) {
                    params.put("type", "merchant");
                }

                try {

                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, null, params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("sign up \n " + responseString);
                    if (responseString == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }

                    if (response.getStatusLine().getStatusCode() == 200) {

                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TEMP_TOKEN, jsonObject.getString("token"));

                        JSONObject data = jsonObject.getJSONObject("data");
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_ID, data.getString("id"));
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_NAME, data.getString("name"));
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_PASSWORD, password);

                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        JSONObject error = jsonObject.getJSONObject("error");
                        String userMessage = error.getString("usr-message");
                        System.out.println(userMessage);
                        onComplete.done(false, userMessage);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onComplete.done(false, AppConstants.GENERAL_ERROR);
                return;
            }
        }).start();
    }

    public void doMerchantSignUp(final String displayName, final String password, final OnComplete<Boolean> onComplete) {

        new Thread(new Runnable() {
            @Override
            public void run() {


                String reqUrl = HitPayAPI.this.baseUrl + "/merchant";
                Log.d(TAG, " sending req");
                HashMap<String, String> params = new HashMap<>();
                params.put("name", displayName);
                params.put("passcode", password);

                try {

                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, null, params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("sign up \n " + responseString);
                    if (responseString == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }

                    if (response.getStatusLine().getStatusCode() == 200) {

                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TEMP_TOKEN, jsonObject.getString("token"));

                        JSONObject data = jsonObject.getJSONObject("data");
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_ID, data.getString("id"));
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_NAME, data.getString("name"));
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_PASSWORD, password);

                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        JSONObject error = jsonObject.getJSONObject("error");
                        String userMessage = error.getString("usr-message");
                        System.out.println(userMessage);
                        onComplete.done(false, userMessage);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onComplete.done(false, AppConstants.GENERAL_ERROR);
                return;
            }
        }).start();
    }

    public void getMerchantDetails(final OnComplete<Boolean> onComplete) {

        new Thread(new Runnable() {
            @Override
            public void run() {


                String reqUrl = HitPayAPI.this.baseUrl + "/merchant";
                try {

                    HttpResponse response = HttpManager.getInstance().doGet(reqUrl, getAuthTempHeader(), null);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("sign up \n " + responseString);
                    if (responseString == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }

                    if (response.getStatusLine().getStatusCode() == 200) {

                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_ID, jsonObject.getString("id"));
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_NAME, jsonObject.getString("name"));
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_MOBILE_NO, jsonObject.getString("mobile"));
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_EMAIL, jsonObject.getString("email"));


                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        JSONObject error = jsonObject.getJSONObject("error");
                        String userMessage = error.getString("usr-message");
                        System.out.println(userMessage);
                        onComplete.done(false, userMessage);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onComplete.done(false, AppConstants.GENERAL_ERROR);
                return;
            }
        }).start();
    }

    public void submitStripeCode(final String code, final String scope, final String state, final OnComplete<Boolean> onComplete) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String reqUrl = HitPayAPI.this.baseUrl + "/stripe/authenticate";
                HashMap<String, String> params = new HashMap<>();
                params.put("code", code);
                params.put("state", state);
                params.put("scope", scope);
                params.put("client_id", "8b7bc7aa-bde1-4380-b49e-7f1baeb8c3ce");
                params.put("client_secret", "cFxpL6tLjcbIAp5WQXHnLjHaaZgPCgUff5UXCzIp");
                try {

                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthTempHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("sibmit stripe code \n " + responseString);
                    if (responseString == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }

                    if (response.getStatusLine().getStatusCode() == 200) {

                        String token = jsonObject.getString("access_token");
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TOKEN, token);
                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(false, userMessage);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onComplete.done(false, AppConstants.GENERAL_ERROR);
                return;
            }
        }).start();
    }


    public void verifyCode(final String code, final String type, final boolean isMerchant, final OnComplete<Boolean> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                String urlReq = isMerchant ? (HitPayAPI.this.baseUrl + "/merchant") : (HitPayAPI.this.baseUrl + "/client");
                urlReq += (type.equals("1") ? "/verify-email" : "/verify-mobile");
                HashMap<String, String> params = new HashMap<>();
                params.put("otp", code);

                try {

                    HttpResponse response = HttpManager.getInstance().doPostRequest(urlReq, getAuthTempHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println(responseString);

                    if (responseString == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }

                    if (response.getStatusLine().getStatusCode() == 200) {
                        if (jsonObject.has("token")) {
                            if (type.equals("1")) {
                                // Still sign up not done.
                                ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TEMP_TOKEN, jsonObject.getString("token"));
                                if (isMerchant) {
                                    ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TOKEN, jsonObject.getString("token"));
                                }
                            } else {
                                ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TOKEN, jsonObject.getString("token"));
                            }
                        }
                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        JSONObject error = jsonObject.getJSONObject("error");
                        String userMessage = error.getString("usr-message");
                        System.out.println(userMessage);
                        onComplete.done(false, userMessage);
                        return;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public boolean resendVerificationCode(final String type, final boolean isMerchant) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                String urlReq = isMerchant ? (HitPayAPI.this.baseUrl + "/merchant") : (HitPayAPI.this.baseUrl + "/client");
                urlReq += (type.equals("1") ? "/resend-email-otp" : "/client/resend-mobile-otp");

                Log.d(TAG, " sending req");
                HashMap<String, String> params = new HashMap<>();
                try {
                    HttpResponse response = HttpManager.getInstance().doGet(urlReq, isMerchant ? getAuthTempHeader() : getAuthHeader(), params);
                    if (response.getStatusLine().getStatusCode() == 200) {
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    public void getProfileInfo(final OnComplete<Boolean> onComplete) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/user?with_businesses_owned=true";
                try {

                    HttpResponse response = HttpManager.getInstance().doGet(reqUrl, getAuthHeader(), null);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println(responseString);
                    System.out.println("" + response.getStatusLine().getStatusCode());
                    if (responseString == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }

                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {

                        if (!jsonObject.isNull("businesses")) {
                            JSONObject businesses = jsonObject.getJSONObject("businesses");
                            JSONArray arrBusiness = businesses.getJSONArray("owned");
                            if (arrBusiness.length() > 0) {
                                ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_BUSSINESS_ID, arrBusiness.getJSONObject(0).getString("id"));
                                if (!arrBusiness.getJSONObject(0).isNull("display_name")) {
                                    ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_NAME, arrBusiness.getJSONObject(0).getString("display_name"));
                                }
                                if (!arrBusiness.getJSONObject(0).isNull("store_url")) {
                                    ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_STORE_URL, arrBusiness.getJSONObject(0).getString("store_url"));
                                }
                                if (!arrBusiness.getJSONObject(0).isNull("stripe_enabled")) {
                                    ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_STRIPE_ENABLE, arrBusiness.getJSONObject(0).getString("stripe_enabled"));
                                } else {
                                    ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_STRIPE_ENABLE, "");
                                }
                                if (!arrBusiness.getJSONObject(0).isNull("paynow_enabled")) {
                                    ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_PAYNOW_ENABLE, arrBusiness.getJSONObject(0).getString("paynow_enabled"));
                                } else {
                                    ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_PAYNOW_ENABLE, "");
                                }
                                if (!arrBusiness.getJSONObject(0).isNull("payment_request_default_link")) {
                                    ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_PAYMENT_REQUEST_LINK, arrBusiness.getJSONObject(0).getString("payment_request_default_link"));
                                } else {
                                    ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_PAYMENT_REQUEST_LINK, "");
                                }
                            } else {
                                onComplete.done(null, "businesses_empty");
                                return;
                            }
                        }

                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_ID, jsonObject.getString("id"));
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_NAME, jsonObject.getString("display_name"));
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_TYPE, "merchant");
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_MERCH_HOME, jsonObject.getString("country_code"));

                        if (!jsonObject.isNull("email_login_enabled")) {
                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_EMAIL_LOGIN_ENABLED, jsonObject.getString("email_login_enabled"));
                        }

                        if (!jsonObject.isNull("logo_url")) {
                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_LOGO_URL, jsonObject.getString("logo_url"));
                        }
                        if (!jsonObject.isNull("subscriptions")) {
                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_SUBSCRIPTION_SETTINGS, jsonObject.getJSONObject("subscriptions").toString());
                        }
                        if (!jsonObject.isNull("display_name")) {
                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_DISPLAY_NAME, jsonObject.getString("display_name"));
                        }
                        if (!jsonObject.isNull("business_category")) {
                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_BUSINESS_CATEGORY, jsonObject.getString("business_category"));
                        }
                        if (!jsonObject.isNull("extra_data")) {
                            JSONObject extraData = jsonObject.getJSONObject("extra_data");
                            if (!extraData.isNull("email")) {
                                ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_EMAIL, extraData.getString("email"));
                            }
                        }

                        if (!jsonObject.isNull("referral") && !jsonObject.getJSONObject("referral").isNull("url")) {
                            String promoURL = jsonObject.getJSONObject("referral").getString("url");
                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_REFERRAL_URL, promoURL);
                        }

                        if (!jsonObject.isNull("is_stripe_login")) {
                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_IS_STRIPE_LOGIN, jsonObject.getString("is_stripe_login"));
                        } else {
                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_IS_STRIPE_LOGIN, "");
                        }

//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_DEFAULT_CURRENCY, jsonObject.getString("default_currency_code"));
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_IS_CART_ENABLED, jsonObject.getString("is_cart_enabled"));

                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                onComplete.done(false, AppConstants.GENERAL_ERROR);
                return;
            }
        }).start();
    }


    public void exportReport(final String starts_at, final String ends_at, final OnComplete<String> onComplete) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/send";
                HashMap<String, String> params = new HashMap<>();
                params.put("starts_at", starts_at);
                params.put("ends_at", ends_at);

                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println(responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(null, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    if (response.getStatusLine().getStatusCode() == 200) {
                        String message = jsonObject.getString("message");
                        onComplete.done(message, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                onComplete.done(null, AppConstants.GENERAL_ERROR);
                return;

            }
        }).start();
    }


    public void sendTransactionReceipt(final String id, final String email, final OnComplete<Boolean> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/" + id + "/send";
                HashMap<String, String> params = new HashMap<>();
                params.put("email", email);

                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println(responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(false, userMessage);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                onComplete.done(false, AppConstants.GENERAL_ERROR);
                return;

            }
        }).start();
    }


    public void makeCardPayment(final String source, final String remark, final String amount, final String currencyCode, final OnComplete<String> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/transaction/payment-card";
                HashMap<String, String> params = new HashMap<>();
                params.put("source", source);
                params.put("remark", remark);
                params.put("amount", amount);
                params.put("currency", currencyCode.toLowerCase());
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + response.getStatusLine().getStatusCode());
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        String id = jsonObject.getString("id");
                        String idString = "" + id;
                        onComplete.done(idString, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void saveNotificationSettings(final HashMap<String, Boolean> notificationSettings, final OnComplete<Boolean> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/user/subscription";
                try {
                    HttpResponse response = HttpManager.getInstance().doPut(reqUrl, getAuthHeader(), new HashMap<String, Object>(notificationSettings));
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + response.getStatusLine().getStatusCode());
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        JSONObject error = jsonObject.getJSONObject("error");
                        String userMessage = error.getString("usr-message");
                        onComplete.done(false, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(false, null);
                    return;
                }

            }
        }).start();
    }

    public void getBusinessCategories(final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/category";
                try {
                    HttpResponse response = HttpManager.getInstance().doGet(reqUrl, getAuthHeader(), null);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + response.getStatusLine().getStatusCode());
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        JSONObject error = jsonObject.getJSONObject("error");
                        String userMessage = error.getString("usr-message");
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void getIntent(final String type, final String amt, final String remark, final String curCode, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/" + type + "/payment-intent";
                HashMap<String, String> params = new HashMap<>();
//                params.put("source", source);
//                params.put("payment_intent", "true");
                params.put("amount", amt);
                params.put("currency", curCode.toLowerCase());
                if (remark != null && remark.length() > 0) {
                    params.put("remark", remark);
                }
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void getIntentTerminal(final String type, final String amt, final String remark, final String curCode, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/" + type + "/payment-intent";
                HashMap<String, Object> params = new HashMap<>();
                params.put("source", "");
                params.put("card_present", true);
                params.put("payment_intent", true);
                params.put("amount", amt);
                params.put("currency", curCode.toLowerCase());
                if (remark != null && remark.length() > 0) {
                    params.put("remark", remark);
                }
                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void getIntentTerminalOrder(final String orderId, final String type, final String amt, final String remark, final String curCode, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/order/" + orderId + "/charge/" + type + "/payment-intent/card_present";
                HashMap<String, Object> params = new HashMap<>();
                params.put("source", "");
                params.put("card_present", true);
                params.put("payment_intent", true);
                params.put("amount", amt);
                params.put("currency", curCode.toLowerCase());
                if (remark != null && remark.length() > 0) {
                    params.put("remark", remark);
                }
                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void chargeTerminal(final String id, final String type, final String amt, final String remark, final String curCode, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/" + type + "/payment-intent/" + id;
                HashMap<String, Object> params = new HashMap<>();
                params.put("source", "");
                params.put("amount", amt);
                params.put("currency", curCode.toLowerCase());
                params.put("card_present", true);
                params.put("payment_intent", true);
                if (remark != null && remark.length() > 0) {
                    params.put("remark", remark);
                }
                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void processOrder(final String id, final OnComplete<String> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/order/" + id + "/pay";
                HashMap<String, Object> params = new HashMap<>();
                params.put("payment_intent", true);
                try {
                    HttpResponse response = HttpManager.getInstance().doPutObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        JSONObject transaction = jsonObject.getJSONArray("transaction").getJSONObject(0);
                        onComplete.done(transaction.getString("id"), null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void logTransaction(final String method, final String amount, final String curCode, final String remark, final OnComplete<String> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge";

                HashMap<String, String> params = new HashMap<>();
                params.put("amount", amount);
                params.put("currency", curCode.toLowerCase());
                params.put("method", method);
                params.put("remark", remark);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        if (!method.equals("charge_link")) {
                            onComplete.done(jsonObject.getString("id"), null);
                        } else {
                            onComplete.done(jsonObject.getJSONObject("link_sent").getString("charge_url"), null);
                        }
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void createPaymentLink(final String amount, final String curCode, final String remark, final OnComplete<String> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/payment-requests";

                HashMap<String, String> params = new HashMap<>();
                params.put("amount", amount);
                params.put("currency", curCode.toLowerCase());
                params.put("purpose", remark);
                params.put("channel", "api_link");
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject.getString("url"), null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }


    public void completePayment(final String id, final OnComplete<String> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/transaction/" + id + "/process";
                HashMap<String, Object> params = new HashMap<>();
                params.put("payment_intent", true);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        String transaction = jsonObject.getString("id");
                        onComplete.done(transaction, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void updateStoreUrl(final String url, final OnComplete<Boolean> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/user/store-url";
                HashMap<String, String> params = new HashMap<>();
                params.put("username", url);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_STORE_URL, jsonObject.getString("store_url"));
                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }


    public void getPaymentIntent(final String orderId, final String type, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/order/" + orderId + "/charge/" + type + "/payment-intent";
                HashMap<String, Object> params = new HashMap<>();
                try {
                    HttpResponse response = HttpManager.getInstance().doPost(reqUrl, getAuthHeader(), null);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }


    public void makeCompleteOrder(final String orderID, final String message, final OnComplete<Boolean> onComplete) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String reqUrl = HitPayAPI.this.baseUrl + "/business/"  + businessId + "/order/" + orderID + "/status/completed";
                HashMap<String, Object> params = new HashMap<>();
                params.put("message", message);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        onComplete.done(null, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }
            }
        }).start();
    }


    public static String randomToken(int size) {
        final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        int len = size;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public void logLong(String veryLongString) {
        int maxLogSize = 1000;
        for (int i = 0; i <= veryLongString.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > veryLongString.length() ? veryLongString.length() : end;
            Log.v(TAG, veryLongString.substring(start, end));
        }
    }

    public void logTransactionPayment(final String method, final String amount, final String curCode, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/stripe/" + method;
                HashMap<String, String> params = new HashMap<>();
                params.put("amount", amount);
                params.put("currency", curCode.toLowerCase());
                params.put("remark", method.equals("wechat-source") ? "Wechat Payment" : method);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void logTransactionAfterPayment(final String url, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = url + version;
                try {
                    HttpResponse response = HttpManager.getInstance().doGet(reqUrl, getAuthHeader(), null);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void chargePayment(final JSONObject jsonObject, final String id, final String method, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/transaction/" + id + "/" + method + "/charge" + version;

                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), jsonObject);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }



    public void addProducts(final String orderId, final String productId, final int quantity, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/order/" + orderId + "/product";
                HashMap<String, Object> params = new HashMap<>();
                params.put("id", productId);
                params.put("quantity", quantity);

                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        if (!jsonObject.isNull("data") && !jsonObject.getJSONObject("data").isNull("product.0.quantity")) {
                            userMessage = jsonObject.getJSONObject("data").getString("product.0.quantity");
                        }
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void payAlipayWechat(final JSONObject jsonObject, final String id, final String method, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/order/" + id + "/pay" + version;
                try {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("source", jsonObject.getString("source"));
                    params.put("client_secret", jsonObject.getString("client_secret"));
                    params.put("method", method);

                    HttpResponse response = HttpManager.getInstance().doPutObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void refundTransaction(final String id, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/" + id + version;
                try {
                    HashMap<String, Object> params = new HashMap<>();
                    HttpResponse response = HttpManager.getInstance().doDelete(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void createNewLogin(final String email, final String password, final OnComplete<Boolean> onComplete) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/user/credentials" + version;
                HashMap<String, Object> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);

                try {
                    HttpResponse response = HttpManager.getInstance().doPutObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println(responseString);
                    System.out.println("" + response.getStatusLine().getStatusCode());
                    if (responseString == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject == null) {
                        onComplete.done(false, AppConstants.GENERAL_ERROR);
                        return;
                    }

                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_ID, jsonObject.getString("id"));
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_STRIPE_ID, jsonObject.getString("auth_id"));
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_NAME, jsonObject.getString("name"));
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_TYPE, "merchant");
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_MERCH_HOME, jsonObject.getString("country_code"));
//
//                        if (!jsonObject.isNull("logo_url")) {
//                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_LOGO_URL, jsonObject.getString("logo_url"));
//                        }
//                        if (!jsonObject.isNull("subscriptions")) {
//                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_SUBSCRIPTION_SETTINGS, jsonObject.getJSONObject("subscriptions").toString());
//                        }
//                        if (!jsonObject.isNull("display_name")) {
//                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_DISPLAY_NAME, jsonObject.getString("display_name"));
//                        }
//                        if (!jsonObject.isNull("business_category")) {
//                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_BUSINESS_CATEGORY, jsonObject.getString("business_category"));
//                        }
//                        if (!jsonObject.isNull("extra_data")) {
//                            JSONObject extraData = jsonObject.getJSONObject("extra_data");
//                            if (!extraData.isNull("email")) {
//                                ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_USER_EMAIL, extraData.getString("email"));
//                            }
//                        }
//                        if (!jsonObject.isNull("store_url")) {
//                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_STORE_URL, jsonObject.getString("store_url"));
//                        }
//                        if (!jsonObject.isNull("referral") && !jsonObject.getJSONObject("referral").isNull("url")) {
//                            String promoURL = jsonObject.getJSONObject("referral").getString("url");
//                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_REFERRAL_URL, promoURL);
//                        }
//
//                        if (!jsonObject.isNull("is_stripe_login")) {
//                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_IS_STRIPE_LOGIN, jsonObject.getString("is_stripe_login"));
//                        } else {
//                            ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_IS_STRIPE_LOGIN, "");
//                        }
//
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_DEFAULT_CURRENCY, jsonObject.getString("default_currency_code"));
//                        ApplicationPreferencesManager.setValue(HitPayAPI.this.context, AppConstants.PREF_IS_CART_ENABLED, jsonObject.getString("is_cart_enabled"));

                        onComplete.done(true, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(false, userMessage);
                        return;
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                onComplete.done(false, AppConstants.GENERAL_ERROR);
                return;
            }
        }).start();
    }

    public void authMethodsLogin(final String email, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/stripe/oauth/search";
                HashMap<String, String> params = new HashMap<>();
                params.put("email", email);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 302) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        try {
                            JSONObject jsError = jsonObject.getJSONObject("errors");
                            for (final Iterator<String> iter = jsError.keys(); iter.hasNext(); ) {
                                final String key = iter.next();
                                final Object value = jsError.get(key);
                                final JSONArray error = (JSONArray) value;
                                userMessage = error.get(0).toString();
                            }
                        } catch (final JSONException e) {
                        }
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void authLogin(final String email, final String password, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/oauth/token";
                HashMap<String, String> params = new HashMap<>();
                params.put("username", email);
                params.put("password", password);
                params.put("grant_type", "password");
                params.put("client_id", "8fba0aa1-36ce-4423-b3d2-76ad7d731630");
                params.put("client_secret", "W4CkZ32h4bNdOUOrV8L9rAArIt4fmJYgjFGCBDNb");
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        if (!jsonObject.isNull("access_token"))
                            ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TOKEN, jsonObject.getString("access_token"));
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        if (!jsonObject.isNull("error")) {
                            if (jsonObject.getString("error").equals("invalid_grant")) {
                                userMessage = "Password incorrect!";
                            }
                        }
                        try {
                            JSONObject jsError = jsonObject.getJSONObject("errors");
                            for (final Iterator<String> iter = jsError.keys(); iter.hasNext(); ) {
                                final String key = iter.next();
                                final Object value = jsError.get(key);
                                final JSONArray error = (JSONArray) value;
                                userMessage = error.get(0).toString();
                            }
                        } catch (final JSONException e) {
                        }
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void forgotPassword(final String email, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/user/password/email";
                HashMap<String, String> params = new HashMap<>();
                params.put("email", email);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 302) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        try {
                            JSONObject jsError = jsonObject.getJSONObject("errors");
                            for (final Iterator<String> iter = jsError.keys(); iter.hasNext(); ) {
                                final String key = iter.next();
                                final Object value = jsError.get(key);
                                final JSONArray error = (JSONArray) value;
                                userMessage = error.get(0).toString();
                            }
                        } catch (final JSONException e) {
                        }
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void createAccount(final String display_name, final String email, final String password, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/user" + version;
                HashMap<String, String> params = new HashMap<>();
                params.put("display_name", display_name);
                params.put("email", email);
                params.put("password", password);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TOKEN, jsonObject.getString("access_token"));
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        try {
                            JSONObject jsError = jsonObject.getJSONObject("errors");
                            for (final Iterator<String> iter = jsError.keys(); iter.hasNext(); ) {
                                final String key = iter.next();
                                final Object value = jsError.get(key);
                                final JSONArray error = (JSONArray) value;
                                userMessage = error.get(0).toString();
                            }
                        } catch (final JSONException e) {
                        }

                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }
            }
        }).start();
    }

    public void createBussiness(final String name, final String country, final String email, final String phone_number, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business" + version;
                HashMap<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("country", country.toLowerCase());
                params.put("email", email);
                params.put("phone_number", phone_number);
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        ApplicationPreferencesManager.setValue(context, AppConstants.PREF_BUSSINESS_ID, jsonObject.getString("id"));
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        try {
                            JSONObject jsError = jsonObject.getJSONObject("errors");
                            for (final Iterator<String> iter = jsError.keys(); iter.hasNext(); ) {
                                final String key = iter.next();
                                final Object value = jsError.get(key);
                                final JSONArray error = (JSONArray) value;
                                userMessage = error.get(0).toString();
                            }
                        } catch (final JSONException e) {
                        }
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void getStripeUrlForHitpaySignup(final String business_id, final OnComplete<String> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + business_id + "/payment-provider/stripe";
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL urlGet = new URL(reqUrl);
                    connection = (HttpURLConnection) urlGet.openConnection();
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                        Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                    }
                    JSONObject jsonObject = new JSONObject(buffer.toString());
                    String url = jsonObject.getString("redirect_url");
                    onComplete.done(url, null);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void getJsonCallback(final String url, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = url;
                try {
                    HttpResponse response = HttpManager.getInstance().doGet(reqUrl, getAuthHeader(), null);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }
            }
        }).start();
    }

    public void submitJson(final JSONObject jsonObject, final String business_id, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + business_id + "/payment-provider/stripe";
                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), jsonObject);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }
            }
        }).start();
    }

    public void submitJsonLogin(final JSONObject jsonObject, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/stripe/oauth/process";
                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), jsonObject);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        if (!jsonObject.isNull("access_token"))
                            ApplicationPreferencesManager.setValue(context, AppConstants.PREF_USER_TOKEN, jsonObject.getString("access_token"));
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }
            }
        }).start();
    }

    public void createCustomer(final HashMap<String, Object> params, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/customer";
                try {
                    HttpResponse response = HttpManager.getInstance().doPostObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        try {
                            JSONObject jsError = jsonObject.getJSONObject("errors");
                            for (final Iterator<String> iter = jsError.keys(); iter.hasNext(); ) {
                                final String key = iter.next();
                                final Object value = jsError.get(key);
                                final JSONArray error = (JSONArray) value;
                                userMessage = error.get(0).toString();
                            }
                        } catch (final JSONException e) {
                        }

                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }
            }
        }).start();
    }

    public void updateCustomer(final String customer_id, final HashMap<String, Object> params, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/customer/" + customer_id;
                try {
                    HttpResponse response = HttpManager.getInstance().doPutObjectRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    JSONObject jsonObject = new JSONObject(responseString);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200 || status == 201) {
                        onComplete.done(jsonObject, null);
                        return;
                    } else if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                        onComplete.needUpdate();
                        return;
                    } else {
                        String userMessage = jsonObject.getString("message");
                        try {
                            JSONObject jsError = jsonObject.getJSONObject("errors");
                            for (final Iterator<String> iter = jsError.keys(); iter.hasNext(); ) {
                                final String key = iter.next();
                                final Object value = jsError.get(key);
                                final JSONArray error = (JSONArray) value;
                                userMessage = error.get(0).toString();
                            }
                        } catch (final JSONException e) {
                        }

                        System.out.println(userMessage);
                        onComplete.done(null, userMessage);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }
            }
        }).start();
    }

    public void getStatusCharge(final String charge_id, final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/" + charge_id;
                HashMap<String, String> params = new HashMap<>();
                try {
                    HttpResponse response = HttpManager.getInstance().doGet(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    int status = response.getStatusLine().getStatusCode();
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (status != 200) {
                        if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                            onComplete.needUpdate();
                            return;
                        } else {
                            String userMessage = jsonObject.getString("message");
                            System.out.println(userMessage);
                            onComplete.done(null, userMessage);
                            return;
                        }
                    }
                    onComplete.done(jsonObject, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

    public void createConnectionToken(final OnComplete<JSONObject> onComplete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String reqUrl = HitPayAPI.this.baseUrl + "/business/" + businessId + "/charge/stripe/connection_token";
                HashMap<String, String> params = new HashMap<>();
                try {
                    HttpResponse response = HttpManager.getInstance().doPostRequest(reqUrl, getAuthHeader(), params);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("" + responseString);

                    int status = response.getStatusLine().getStatusCode();
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (status != 200 && status != 201) {
                        if (response.getStatusLine().getStatusCode() == 308 && !jsonObject.isNull("forced_update") && jsonObject.getBoolean("forced_update")) {
                            onComplete.needUpdate();
                            return;
                        } else {
                            String userMessage = jsonObject.getString("message");
                            System.out.println(userMessage);
                            onComplete.done(null, userMessage);
                            return;
                        }
                    }
                    onComplete.done(jsonObject, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    onComplete.done(null, AppConstants.GENERAL_ERROR);
                    return;
                }

            }
        }).start();
    }

}