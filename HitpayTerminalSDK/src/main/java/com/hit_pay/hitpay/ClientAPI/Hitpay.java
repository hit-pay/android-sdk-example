package com.hit_pay.hitpay.ClientAPI;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.hit_pay.hitpay.Managers.AppManager;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.HitpayUtil;
import com.hit_pay.hitpay.activity.HitPayLoginPageActivity;
import com.hit_pay.hitpay.terminal.TerminalActivity;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Hitpay {
    private static Activity mContect;
    private static Thread thread;
    private static boolean isStop = false;
    private static CountDownTimer countDownTimer;

    // add a private listener variable
    public static HitPayAuthenticationListener hitPayAuthenticationListener = null;
    public static HitPayTerminalListener hitPayTerminalListener = null;
    public static boolean simulated = false;
    public static HitPayTerminalChargeListener hitPayTerminalChargeListener = null;
    public static HitPayPayNowChargeListener hitPayPayNowChargeListener = null;
    public static HitPayRefundListener hitPayRefundListener = null;

    // provide a way for another class to set the listener
    public static void setHitPayAuthenticationListener(HitPayAuthenticationListener listener) {
        hitPayAuthenticationListener = listener;
    }

    public static void setHitPayTerminalListener(HitPayTerminalListener hitPayTerminalListener) {
        Hitpay.hitPayTerminalListener = hitPayTerminalListener;
    }

    public static void setHitPayTerminalChargeListener(HitPayTerminalChargeListener hitPayTerminalChargeListener) {
        Hitpay.hitPayTerminalChargeListener = hitPayTerminalChargeListener;
    }

    public static void setHitPayPayNowChargeListener(HitPayPayNowChargeListener hitPayPayNowChargeListener) {
        Hitpay.hitPayPayNowChargeListener = hitPayPayNowChargeListener;
    }

    public static void setHitPayRefundListener(HitPayRefundListener hitPayRefundListener) {
        Hitpay.hitPayRefundListener = hitPayRefundListener;
    }

    public static void init(Activity context) {
        mContect = context;
    }

    public static void initiateAuthentication() {
        mContect.startActivity(new Intent(mContect, HitPayLoginPageActivity.class));
    }

    public static void setSimulatedTerminal(boolean simulated) {
        Hitpay.simulated = simulated;
    }

    public static void initiateTerminalSetup() {
        mContect.startActivity(new Intent(mContect, TerminalActivity.class));
    }

    public static boolean isTerminalConnectted() {
        if (Terminal.isInitialized() && verifyGpsEnabled(mContect) && Terminal.getInstance().getConnectionStatus() == ConnectionStatus.CONNECTED) {
            return true;
        }
        return false;
    }

    public static void makeTerminalPayment(String amount, String currency) {
        transactionTerminalPayment(amount, currency);
    }

    public static void makePayNowPayment(String amount, String currency) {
        transactionPayNowPayment(amount, currency);
    }

    public static void refundCharge(String chargeId) {
        new HitPayAPI(mContect).refundTransaction(chargeId,  new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                mContect.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorMessage != null) {
                            if (Hitpay.hitPayRefundListener != null) {
                                Hitpay.hitPayRefundListener.refundCompleted(false);
                            }
                        } else {
                            if (Hitpay.hitPayRefundListener != null) {
                                Hitpay.hitPayRefundListener.refundCompleted(true);
                            }
                        }

                    }
                });
            }
            @Override
            public void needUpdate() {
            }
        });
    }

    private static void transactionTerminalPayment(String amount, String currency) {
//        startLoading();
        new HitPayAPI(mContect).getIntentTerminal("stripe", amount, "", currency, new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                mContect.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorMessage != null) {
                            AppManager.showErrorAlert(mContect, errorMessage);
                        } else {
                            try {
                                String clientSrt = response.getJSONObject("payment_intent").getString("client_secret");
                                String chargeId = response.getString("charge_id");
                                String orderId = response.getString("id");
                                Terminal.getInstance().retrievePaymentIntent(clientSrt, new PaymentIntentCallback() {
                                    @Override
                                    public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                                        Terminal.getInstance().collectPaymentMethod(paymentIntent, new PaymentIntentCallback() {
                                            @Override
                                            public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                                                Terminal.getInstance().processPayment(paymentIntent, new PaymentIntentCallback() {
                                                    @Override
                                                    public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                                                        chargeTerminalPayment(chargeId, orderId, amount, currency);
                                                    }

                                                    @Override
                                                    public void onFailure(@NotNull TerminalException e) {
                                                        showFailTerminalScreen();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(@NotNull TerminalException e) {
                                                showFailTerminalScreen();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(@NotNull TerminalException e) {
                                        showFailTerminalScreen();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            public void needUpdate() {

            }
        });
    }

    private static void chargeTerminalPayment(String chargeId, String orderId, String amount, String currency) {
        new HitPayAPI(mContect).chargeTerminal(orderId, "stripe", amount, "", currency, new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                mContect.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorMessage != null) {
                            AppManager.showErrorAlert(mContect, errorMessage);
                        } else {
                            showDoneTerminalScreen(chargeId);
                            try {
                                JSONObject jsCharges = response.getJSONObject("charges");
                                JSONObject jsData = jsCharges.getJSONArray("data").getJSONObject(0);
                                JSONObject jsPaymentMethod = jsData.getJSONObject("payment_method_details").getJSONObject("card_present");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            public void needUpdate() {
            }
        });
    }

    private static void showDoneTerminalScreen(String chargeId) {
        if (Hitpay.hitPayTerminalChargeListener != null) {
            Hitpay.hitPayTerminalChargeListener.chargeTerminalCompleted(true, chargeId);
        }
    }

    private static void showFailTerminalScreen() {
        if (Hitpay.hitPayTerminalChargeListener != null) {
            Hitpay.hitPayTerminalChargeListener.chargeTerminalCompleted(false, "");
        }
    }

    private static void transactionPayNowPayment(String amount, String currency) {
        new HitPayAPI(mContect).getIntent("paynow", amount, "", currency, new OnComplete<JSONObject>() {
            @Override
            public void done(JSONObject response, String errorMessage) {
                mContect.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorMessage != null) {
                            AppManager.showErrorAlert(mContect, errorMessage);
                            return;
                        }
                        try {
//                    transactionId = response.getString("id");
                            String chargeId = response.getString("charge_id");
                            JSONObject jsonWechat = response.getJSONObject("paynow_online");
                            String url = jsonWechat.getString("qr_code_data");
//                    generateQR(url);
                            if (Hitpay.hitPayPayNowChargeListener != null) {
                                Hitpay.hitPayPayNowChargeListener.onQRUrlReturn(url);
                            }
                            checkStatus(chargeId);
                            mContect.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startCountDown();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void needUpdate() {
            }
        });
    }

    private static void startCountDown() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
//                tvCountDown.setText("" + l / 1000);
            }

            @Override
            public void onFinish() {
                isStop = true;
                thread.interrupt();
                showFailPayNowScreen();
            }
        };
        countDownTimer.start();
    }

    private static void checkStatus(String chargeId) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop)
                    try {
                        Thread.sleep(6000);
                        mContect.runOnUiThread(new Runnable() // start actions in UI thread
                        {
                            @Override
                            public void run() {
                                getStatusCharge(chargeId);
                            }
                        });
                    } catch (InterruptedException e) {
                        // ooops
                    }
            }
        });
        thread.start();
    }

    private static void getStatusCharge(String chargeId) {
        new HitPayAPI(mContect).getStatusCharge(chargeId, new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                mContect.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorMessage == null) {
                            try {
                                String status = response.getString("status");
                                if (status.equals("succeeded") || status.equals("completed")) {
                                    mContect.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            countDownTimer.cancel();
                                            isStop = true;
                                            thread.interrupt();
                                            showDonePayNowScreen(chargeId);
                                        }
                                    });
                                } else if (status.equals("failed") || status.equals("canceled")) {
                                    mContect.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            countDownTimer.cancel();
                                            isStop = true;
                                            thread.interrupt();
                                            showFailPayNowScreen();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                mContect.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        countDownTimer.cancel();
                                        isStop = true;
                                        thread.interrupt();
                                        showFailPayNowScreen();
                                    }
                                });
                            }
                        }
                    }
                });
            }

            @Override
            public void needUpdate() {
            }
        });
    }

    private static void showDonePayNowScreen(String chargeId) {
        if (Hitpay.hitPayPayNowChargeListener != null) {
            Hitpay.hitPayPayNowChargeListener.chargePayNowCompleted(true, chargeId);
        }
    }

    private static void showFailPayNowScreen() {
        if (Hitpay.hitPayPayNowChargeListener != null) {
            Hitpay.hitPayPayNowChargeListener.chargePayNowCompleted(false, "");
        }
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

}
