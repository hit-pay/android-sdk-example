package com.hit_pay.hitpay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hit_pay.hitpay.ClientAPI.HitPayAPI;
import com.hit_pay.hitpay.ClientAPI.HitPayAuthenticationListener;
import com.hit_pay.hitpay.ClientAPI.Hitpay;
import com.hit_pay.hitpay.activity.HitPayLoginPageActivity;
import com.hit_pay.hitpay.activity.HitPayLoginPageActivity3Activity;

public class MainActivity extends AppCompatActivity implements HitPayAuthenticationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Hitpay.init(this);
        Hitpay.setHitPayAuthenticationListener(this);
        Button hitpayLogin = findViewById(R.id.btn_login);
        hitpayLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hitpay.initiateAuthentication();
            }
        });
    }

    @Override
    public void authenticationCompleted(boolean status) {
        Toast.makeText(MainActivity.this, "Login success AAAAAA", Toast.LENGTH_SHORT).show();
    }
}