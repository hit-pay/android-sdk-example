package com.hit_pay.hitpay.ClientAPI;

// interface
public interface HitPayPayNowChargeListener {
    // add whatever methods you need here
    public void chargePayNowCompleted(boolean status, String chargeId);

    public void onQRUrlReturn(String url);
}