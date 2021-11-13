package com.sample.hitpaysdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hit_pay.hitpay.ClientAPI.HitPayAuthenticationListener;
import com.hit_pay.hitpay.ClientAPI.HitPayPayNowChargeListener;
import com.hit_pay.hitpay.ClientAPI.HitPayRefundListener;
import com.hit_pay.hitpay.ClientAPI.HitPayTerminalChargeListener;
import com.hit_pay.hitpay.ClientAPI.HitPayTerminalListener;
import com.hit_pay.hitpay.ClientAPI.Hitpay;
import com.hit_pay.hitpay.Managers.AppManager;
import com.hit_pay.hitpay.Util.HitpayUtil;
import com.hit_pay.hitpay.activity.SignUpPage2Activity;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements HitPayAuthenticationListener, HitPayTerminalListener, HitPayTerminalChargeListener, HitPayPayNowChargeListener, HitPayRefundListener {
    private static final int REQUEST_CODE_LOCATION = 1;
    private ProgressDialog progressDialog;
    private SwitchCompat simulated_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Hitpay.init(this);
        Hitpay.setEnv(false);
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

        Button hitpayLogout = findViewById(R.id.btn_logout);
        hitpayLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hitpay.signOut();
                Toast.makeText(MainActivity.this, "Logout success", Toast.LENGTH_SHORT).show();
            }
        });

        simulated_switch = findViewById(R.id.simulated_switch);
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
                        Hitpay.setSimulatedTerminal(simulated_switch.isChecked());
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
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_amount, null);
                dialogBuilder.setView(dialogView);
                EditText edtAmount = (EditText) dialogView.findViewById(R.id.edt_amount);
                Button btnPay = (Button) dialogView.findViewById(R.id.btn_pay);
                AlertDialog alertDialog = dialogBuilder.create();

                btnPay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (edtAmount.getText().toString().length() > 0) {
                            if (Hitpay.isTerminalConnectted()) {
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setMessage("Charging...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                Hitpay.makeTerminalPayment(edtAmount.getText().toString(), "SGD");
                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(MainActivity.this, "Please connect terminal first", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.show();
            }
        });

        Button btnMakePayNowPayment = findViewById(R.id.btn_make_paynow_payment);
        btnMakePayNowPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_amount, null);
                dialogBuilder.setView(dialogView);
                EditText edtAmount = (EditText) dialogView.findViewById(R.id.edt_amount);
                Button btnPay = (Button) dialogView.findViewById(R.id.btn_pay);
                AlertDialog alertDialog = dialogBuilder.create();

                btnPay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (edtAmount.getText().toString().length() > 0) {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setMessage("Charging...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            Hitpay.makePayNowPayment(edtAmount.getText().toString(), "SGD", true);
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alertDialog.show();

            }
        });

        Button btnRefund = findViewById(R.id.btn_refund_charge);
        btnRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_refund, null);
                dialogBuilder.setView(dialogView);
                EditText edtChargeId = (EditText) dialogView.findViewById(R.id.edt_charge_id);
                Button btnRefund = (Button) dialogView.findViewById(R.id.btn_refund);
                AlertDialog alertDialog = dialogBuilder.create();

                btnRefund.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Refunding...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Hitpay.refundCharge(edtChargeId.getText().toString());
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
                    Hitpay.setSimulatedTerminal(simulated_switch.isChecked());
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
            AppManager.showNormalAlert(MainActivity.this, "Login Successful");
        } else {
            AppManager.showNormalAlert(MainActivity.this, "Login Failed");
        }
    }

    @Override
    public void setupCompleted(boolean status, String message) {
        if (status) {
            AppManager.showNormalAlert(MainActivity.this, "Connect Reader Successful");
        } else {
            AppManager.showNormalAlert(MainActivity.this, message);
        }
    }

    @Override
    public void chargeTerminalCompleted(boolean status, String chargeId) {
        if (progressDialog.isShowing()) progressDialog.dismiss();
        if (status) {
            @SuppressLint("WrongConstant") ClipboardManager clipboard = (ClipboardManager) MainActivity.this.getSystemService("clipboard");
            ClipData clip = ClipData.newPlainText("chargeId", chargeId);
            clipboard.setPrimaryClip(clip);
            AppManager.showErrorAlert(MainActivity.this, "Charge Successful", "Charge Id:\n" + chargeId + ". Copied to clipboard");
        } else {
            AppManager.showNormalAlert(MainActivity.this, "Charge Failed");
        }
    }

    @Override
    public void cancelTerminalPayment(boolean status) {
        if (progressDialog.isShowing()) progressDialog.dismiss();
        if (status) {
            AppManager.showNormalAlert(MainActivity.this, "Cancel Terminal Payment Successful");
        } else {
            AppManager.showNormalAlert(MainActivity.this, "Cancel Terminal Payment Failed");
        }
    }

    @Override
    public void chargePayNowCompleted(boolean status, String chargeId) {
        if (progressDialog.isShowing()) progressDialog.dismiss();
        if (status) {
            @SuppressLint("WrongConstant") ClipboardManager clipboard = (ClipboardManager) MainActivity.this.getSystemService("clipboard");
            ClipData clip = ClipData.newPlainText("chargeId", chargeId);
            clipboard.setPrimaryClip(clip);
            AppManager.showErrorAlert(MainActivity.this, "Charge Successful", "Charge Id:\n" + chargeId + ". Copied to clipboard");
        } else {
            AppManager.showNormalAlert(MainActivity.this, "Timed Out");
        }

    }

    @Override
    public void onQRUrlReturn(String url) {
        AppManager.showErrorAlert(MainActivity.this, "QR CODE", url);
    }

    @Override
    public void refundCompleted(boolean status) {
        if (progressDialog.isShowing()) progressDialog.dismiss();
        if (status) {
            AppManager.showNormalAlert(MainActivity.this, "Refund Successful");
        } else {
            AppManager.showNormalAlert(MainActivity.this, "Refund Failed");
        }
    }
}