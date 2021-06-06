package com.hit_pay.hitpay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hit_pay.hitpay.ClientAPI.HitPayAPI;
import com.hit_pay.hitpay.ClientAPI.HitPayAuthenticationListener;
import com.hit_pay.hitpay.ClientAPI.Hitpay;
import com.hit_pay.hitpay.Util.HitpayUtil;
import com.hit_pay.hitpay.activity.HitPayLoginPageActivity;
import com.hit_pay.hitpay.activity.HitPayLoginPageActivity3Activity;

public class MainActivity extends AppCompatActivity implements HitPayAuthenticationListener {
    private static final int REQUEST_CODE_LOCATION = 1;

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

        Button connectTerminal = findViewById(R.id.btn_connect_terminal);
        connectTerminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Enable Bluetooth
                if (BluetoothAdapter.getDefaultAdapter() != null &&
                        !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    BluetoothAdapter.getDefaultAdapter().enable();
                }

                // Check for location permissions
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    if (Hitpay.verifyGpsEnabled(MainActivity.this)) {
                        Hitpay.initiateTerminalSetup();
                    }
                } else {
                    // If we don't have them yet, request them before doing anything else
                    final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH};
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE_LOCATION);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (Hitpay.verifyGpsEnabled(MainActivity.this)) {
                    Hitpay.initiateTerminalSetup();
                }
            } else {
                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void authenticationCompleted(boolean status) {
        if (status) {
            Toast.makeText(MainActivity.this, "Login success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Login fail", Toast.LENGTH_SHORT).show();
        }
    }
}