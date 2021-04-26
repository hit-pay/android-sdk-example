package com.hit_pay.hitpay.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.View;

import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.AppConstants;

public class SignUpPage3Activity extends AppCompatActivity {

    CardView btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page3);
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUpPage3Activity.this, NewStripeConnectActivity.class);
                i.putExtra(AppConstants.EXTRA_HAS_STRIPE, false);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
