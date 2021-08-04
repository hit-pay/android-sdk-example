package com.hit_pay.hitpay.ClientAPI;

// interface
public interface HitPayTerminalChargeListener {
    // add whatever methods you need here
    public void chargeTerminalCompleted(boolean status, String chargeId);
}