package com.hit_pay.hitpay.ClientAPI;

import android.text.BoringLayout;

/**
 * Created by Nitin on 24/7/16.
 */
public class HitPayResponse {

    String errorMessage;
    boolean success;

    public HitPayResponse(String errorMessage, boolean success) {
        this.errorMessage = errorMessage;
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
