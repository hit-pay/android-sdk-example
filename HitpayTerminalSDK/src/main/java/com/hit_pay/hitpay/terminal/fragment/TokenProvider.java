package com.hit_pay.hitpay.terminal.fragment;

import android.app.Activity;
import android.widget.Toast;

import com.hit_pay.hitpay.ClientAPI.HitPayAPI;
import com.hit_pay.hitpay.ClientAPI.OnComplete;
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback;
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider;
import com.stripe.stripeterminal.external.models.ConnectionTokenException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple implementation of the [ConnectionTokenProvider] interface. We just request a
 * new token from our backend simulator and forward any exceptions along to the SDK.
 */
public class TokenProvider implements ConnectionTokenProvider {
    Activity context;

    public TokenProvider(Activity context) {
        this.context = context;
    }

    @Override
    public void fetchConnectionToken(ConnectionTokenCallback callback) {
        new HitPayAPI(context).createConnectionToken(new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorMessage == null) {
                            try {
                                String token = response.getString("secret");
                                callback.onSuccess(token);
                            } catch (JSONException e) {
                                Toast.makeText(context, "Creating connection token failed", Toast.LENGTH_LONG).show();
                                callback.onFailure(new ConnectionTokenException("Creating connection token failed"));
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
}
