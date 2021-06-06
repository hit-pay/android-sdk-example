package com.hit_pay.hitpay.terminal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.hit_pay.hitpay.Managers.ApplicationPreferencesManager;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.AppConstants;
import com.stripe.stripeterminal.external.models.DiscoveryMethod;

public class DeviceTypeTerminalActivity extends AppCompatActivity {

    ImageView imgBack;
    MaterialButton btnBluetooth, btnInternet, btnCotsDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_type_terminal);
        btnBluetooth = findViewById(R.id.bluetooth);
        btnInternet = findViewById(R.id.internet);
        btnCotsDevice = findViewById(R.id.cots_device);
        imgBack = findViewById(R.id.img_back);
        initUI();
        initListener();
    }

    private void initUI() {
        if (ApplicationPreferencesManager.getStringValue(DeviceTypeTerminalActivity.this, AppConstants.DEVICE_TYPE_TERMINAL) != null) {
            if (ApplicationPreferencesManager.getStringValue(DeviceTypeTerminalActivity.this, AppConstants.DEVICE_TYPE_TERMINAL).equals(DiscoveryMethod.INTERNET.toString())) {
                btnBluetooth.setIcon(null);
                btnInternet.setIcon(getDrawable(R.mipmap.done_green));
                btnCotsDevice.setIcon(null);
            } else if (ApplicationPreferencesManager.getStringValue(DeviceTypeTerminalActivity.this, AppConstants.DEVICE_TYPE_TERMINAL).equals(DiscoveryMethod.COTS.toString())) {
                btnBluetooth.setIcon(null);
                btnInternet.setIcon(null);
                btnCotsDevice.setIcon(getDrawable(R.mipmap.done_green));
            } else {
                btnBluetooth.setIcon(getDrawable(R.mipmap.done_green));
                btnInternet.setIcon(null);
                btnCotsDevice.setIcon(null);
            }
        } else {
            btnBluetooth.setIcon(getDrawable(R.mipmap.done_green));
            btnInternet.setIcon(null);
            btnCotsDevice.setIcon(null);
        }
    }
    private void initListener() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("DATA", DiscoveryMethod.BLUETOOTH_SCAN.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        btnInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("DATA", DiscoveryMethod.INTERNET.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        btnCotsDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("DATA", DiscoveryMethod.COTS.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
