package com.hit_pay.hitpay.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.hit_pay.hitpay.ClientAPI.HitPayAPI;
import com.hit_pay.hitpay.ClientAPI.OnComplete;
import com.hit_pay.hitpay.Managers.AppManager;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.HitpayUtil;
import com.hit_pay.hitpay.adapter.CurrencyAdapter;

import org.json.JSONObject;

import java.util.ArrayList;

public class SignUpPage2Activity extends AppCompatActivity {
    CurrencyAdapter adapter;
    Spinner payMode;
    ArrayList<String> countryModels = new ArrayList<>();
    String mCountryCode;

    AppCompatEditText edtCompanyName, edtCompanyEmail, edtCompanyAddress, edtContactNumber;
    ImageView imgBack;
    CardView btnNext;
    TextView tvTerms;
    CheckBox ckbEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page2);
//        edtSelectCountry = findViewById(R.id.tv_select_country);
        edtCompanyName = findViewById(R.id.tv_company_name);
        edtCompanyEmail = findViewById(R.id.tv_company_email);
        edtCompanyAddress = findViewById(R.id.tv_company_address);
        edtContactNumber = findViewById(R.id.tv_contact_number);
        tvTerms = findViewById(R.id.tv_terms);
        imgBack = findViewById(R.id.img_back);
        btnNext = findViewById(R.id.btn_next);
        ckbEmail = findViewById(R.id.ckb_same_login_email);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValidate()) {
                    createBusiness();
                }
            }
        });

        tvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hitpayapp.com/privacy-and-terms"));
                startActivity(browserIntent);
            }
        });

        edtCompanyEmail.setText(getIntent().getStringExtra("EMAIL"));
        ckbEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtCompanyEmail.setText(getIntent().getStringExtra("EMAIL"));
                } else {
                    edtCompanyEmail.setText("");
                }
            }
        });

        payMode = (Spinner) findViewById(R.id.payment_spinner);
        countryModels.add("Singapore");
        if (mCountryCode == null) {
            mCountryCode = "SG";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SignUpPage2Activity.this,
                android.R.layout.simple_spinner_item, countryModels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payMode.setAdapter(adapter);

//        payMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String curr = countryModels.get(position);
//                mCountryCode = curr;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
    }


    private void createBusiness() {
        startLoading();
        new HitPayAPI(this).createBussiness(edtCompanyName.getText().toString(), mCountryCode, edtCompanyEmail.getText().toString(), edtContactNumber.getText().toString(), new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endLoading();
                        if (errorMessage != null) {
                            AppManager.showErrorAlert(SignUpPage2Activity.this, errorMessage);
                        } else {
                            Intent i = new Intent(SignUpPage2Activity.this, SignUpPage3Activity.class);
                            startActivity(i);
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
                        HitpayUtil.showUpdateDialog(SignUpPage2Activity.this);
                    }
                });
            }
        });
    }

    private boolean checkValidate() {
        String titleM = "Error!";
        if (edtCompanyName.getText().toString().length() == 0) {
            AppManager.showErrorAlert(SignUpPage2Activity.this, titleM, "Please enter display name");
            return false;
        } else if (edtContactNumber.getText().toString().length() == 0) {
            AppManager.showErrorAlert(SignUpPage2Activity.this, titleM, "Please enter contact number");
            return false;
        } else if (edtContactNumber.getText().toString().length() != 8) {
            AppManager.showErrorAlert(SignUpPage2Activity.this, titleM, "Contact number must be 8 digit number");
            return false;
        }
        return true;
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
