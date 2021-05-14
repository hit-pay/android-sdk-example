package com.hit_pay.hitpay.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.hit_pay.hitpay.ClientAPI.HitPayAPI;
import com.hit_pay.hitpay.ClientAPI.HitPayAuthenticationListener;
import com.hit_pay.hitpay.ClientAPI.Hitpay;
import com.hit_pay.hitpay.ClientAPI.OnComplete;
import com.hit_pay.hitpay.Managers.AppManager;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.HitpayUtil;

import org.json.JSONObject;

public class HitPayLoginPageActivity3Activity extends AppCompatActivity {

    AppCompatEditText edt2FACode;
    ImageView imgBack;
    CardView btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page_step3);
        edt2FACode = findViewById(R.id.tv_2fa_code);
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
                if (edt2FACode.getText().toString().length() == 0) {
                    AppManager.showErrorAlert(HitPayLoginPageActivity3Activity.this, "Error!", "Please enter 2FA Code");
                } else {
                    login();
                }
            }
        });
    }

    private void login() {
        startLoading();
        new HitPayAPI(this).authLogin(getIntent().getStringExtra("TOKEN"), edt2FACode.getText().toString(),  new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endLoading();
                        if (errorMessage != null) {
                            AppManager.showErrorAlert(HitPayLoginPageActivity3Activity.this, errorMessage);
                        } else {
                            HitPayAPI api = new HitPayAPI(HitPayLoginPageActivity3Activity.this);
                            api.getProfileInfo(new OnComplete<Boolean>() {
                                @Override
                                public void done(final Boolean response, final String errorMessage) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            endLoading();
                                            if (errorMessage == null) {
                                                if (Hitpay.mListener != null)
                                                    Hitpay.mListener.authenticationCompleted(true);
//                                                Intent i = new Intent(LoginPage3Activity.this, HomeDrawerActivity.class);
//                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                startActivity(i);
                                            } else {
                                                if (errorMessage.equals("businesses_empty")) {
                                                    new AlertDialog.Builder(HitPayLoginPageActivity3Activity.this)
                                                            .setTitle("Business Credentials Missing")
                                                            .setMessage("You need to create a business account to use HitPay app.")
                                                            .setCancelable(true)
                                                            .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    // Whatever...
                                                                    Intent i = new Intent(HitPayLoginPageActivity3Activity.this, SignUpPage2Activity.class);
                                                                    i.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                                                                    startActivity(i);
                                                                }
                                                            }).create().show();
                                                } else {
                                                    AppManager.showErrorAlert(HitPayLoginPageActivity3Activity.this, errorMessage);
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
                });
            }
            @Override
            public void needUpdate() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endLoading();
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
