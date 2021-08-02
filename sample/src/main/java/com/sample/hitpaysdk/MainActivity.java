package com.sample.hitpaysdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hit_pay.hitpay.ClientAPI.HitPayAuthenticationListener;
import com.hit_pay.hitpay.ClientAPI.HitPayPayNowChargeListener;
import com.hit_pay.hitpay.ClientAPI.HitPayRefundListener;
import com.hit_pay.hitpay.ClientAPI.HitPayTerminalChargeListener;
import com.hit_pay.hitpay.ClientAPI.HitPayTerminalListener;
import com.hit_pay.hitpay.ClientAPI.Hitpay;
import com.hit_pay.hitpay.Util.HitpayUtil;

public class MainActivity extends AppCompatActivity implements HitPayAuthenticationListener, HitPayTerminalListener, HitPayTerminalChargeListener, HitPayPayNowChargeListener, HitPayRefundListener {
    private static final int REQUEST_CODE_LOCATION = 1;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Hitpay.init(this);
        Hitpay.setHitPayAuthenticationListener(this);
        Hitpay.setHitPayTerminalListener(this);
        Hitpay.setHitPayTerminalChargeListener(this);
        Hitpay.setHitPayPayNowChargeListener(this);
        Hitpay.setHitPayRefundListener(this);

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

        Button btnMakeTerminalPayment = findViewById(R.id.btn_make_terminal_payment);
        btnMakeTerminalPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (Hitpay.isTerminalConnectted()) {
                   progressDialog = new ProgressDialog(MainActivity.this);
                   progressDialog.setMessage("Please insert card to charging...");
                   progressDialog.setCancelable(false);
                   progressDialog.show();
                   Hitpay.makeTerminalPayment("10.0", "SGD");
               } else {
                   Toast.makeText(MainActivity.this, "Please connect terminal first", Toast.LENGTH_SHORT).show();
               }
            }
        });

        Button btnMakePayNowPayment = findViewById(R.id.btn_make_paynow_payment);
        btnMakePayNowPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hitpay.makePayNowPayment("10.0", "SGD");
            }
        });

        Button btnRefund = findViewById(R.id.btn_refund_charge);
        btnRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hitpay.refundCharge("charge_id");
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
            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setupCompleted(boolean status) {
        if (status) {
            Toast.makeText(MainActivity.this, "Connect Reader Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Connect Reader Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void chargeCompleted(boolean status) {
        if (progressDialog.isShowing()) progressDialog.dismiss();
        if (status) {
            Toast.makeText(MainActivity.this, "Charge Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Charge Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void chargePayNowCompleted(boolean status) {
        findViewById(R.id.ln_qr_paynow).setVisibility(View.GONE);
        if (status) {
            Toast.makeText(MainActivity.this, "Charge Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Charge Failed", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onQRUrlReturn(String url) {
        findViewById(R.id.ln_qr_paynow).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tv_qr_paynow)).setText(url);
    }

    @Override
    public void refundCompleted(boolean status) {
        if (status) {
            Toast.makeText(MainActivity.this, "Refund Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Refund Failed", Toast.LENGTH_SHORT).show();
        }
    }
}