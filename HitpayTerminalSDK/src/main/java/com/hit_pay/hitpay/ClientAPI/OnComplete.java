package com.hit_pay.hitpay.ClientAPI;

/**
 * Created by Nitin on 25/7/16.
 */
public interface OnComplete<T> {
    void done(T response, String errorMessage);

    void needUpdate();
}

