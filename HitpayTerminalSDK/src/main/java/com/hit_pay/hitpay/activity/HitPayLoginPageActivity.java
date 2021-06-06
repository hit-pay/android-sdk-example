package com.hit_pay.hitpay.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.hit_pay.hitpay.ClientAPI.HitPayAPI;
import com.hit_pay.hitpay.ClientAPI.OnComplete;
import com.hit_pay.hitpay.Managers.AppManager;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.AppConstants;
import com.hit_pay.hitpay.Util.HitpayUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class HitPayLoginPageActivity extends AppCompatActivity {
    public static HitPayLoginPageActivity instance = null;

    AppCompatEditText edtEmail;
    ImageView imgBack;
    CardView btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_login_page_step1);
        edtEmail = findViewById(R.id.edt_email);
        imgBack = findViewById(R.id.img_back);
        btnNext = findViewById(R.id.btn_next);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtEmail.getText().toString().length() == 0) {
                    AppManager.showErrorAlert(HitPayLoginPageActivity.this, "Error!", "Please enter your email");
                } else {
                    authMethodsLogin();
                }
            }
        });
    }

    private void authMethodsLogin() {
        startLoading();
        new HitPayAPI(this).authMethodsLogin(edtEmail.getText().toString(),  new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endLoading();
                        if (errorMessage != null) {
                            AppManager.showErrorAlert(HitPayLoginPageActivity.this, errorMessage);
                        } else {
                            try {
                                String status = response.getString("status");
                                if (status.equals("email_login_enabled")) {
                                    Intent i = new Intent(HitPayLoginPageActivity.this, HitPayLoginPageActivity2Activity.class);
                                    i.putExtra("EMAIL", edtEmail.getText().toString());
                                    startActivity(i);
                                } else if (status.equals("stripe_account_found")) {
                                    //stripe
                                    String url = response.getString("redirect_url");
                                    Intent i = new Intent(HitPayLoginPageActivity.this, NewStripeConnectLoginActivity.class);
                                    i.putExtra(AppConstants.EXTRA_HAS_STRIPE, true);
                                    i.putExtra("URL", url);
                                    startActivity(i);
                                } else {
                                    AppManager.showErrorAlert(HitPayLoginPageActivity.this, "Account not found");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                AppManager.showErrorAlert(HitPayLoginPageActivity.this, e.getLocalizedMessage());
                            }
                        }

                    }
                });
            }
            @Override
            public void needUpdate() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endLoading();
                        HitpayUtil.showUpdateDialog(HitPayLoginPageActivity.this);
                    }
                });
            }
        });
    }

    private void startLoading() {
        btnNext.setEnabled(false);
        CircularProgressView progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressView.setVisibility(View.VISIBLE);

    }

    private void endLoading() {
        btnNext.setEnabled(true);
        CircularProgressView progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
