package com.hit_pay.hitpay.ClientAPI;

// interface
public interface HitPayPayNowChargeListener {
    // add whatever methods you need here
    public void chargePayNowCompleted(boolean status);

    public void onQRUrlReturn(String url);
}