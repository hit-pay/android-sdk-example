package com.hit_pay.hitpay.terminal;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.hit_pay.hitpay.ClientAPI.Hitpay;
import com.hit_pay.hitpay.activity.HitPayLoginPageActivity;
import com.hit_pay.hitpay.terminal.fragment.TokenProvider;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.HitpayUtil;
import com.hit_pay.hitpay.terminal.fragment.ConnectedReaderFragment;
import com.hit_pay.hitpay.terminal.fragment.PaymentFragment;
import com.hit_pay.hitpay.terminal.fragment.TerminalFragment;
import com.hit_pay.hitpay.terminal.fragment.discovery.DiscoveryFragment;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.log.LogLevel;

import org.jetbrains.annotations.NotNull;

public class TerminalActivity extends AppCompatActivity implements NavigationListener {
    private static final int REQUEST_CODE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        //open Bluetooth
        if (BluetoothAdapter.getDefaultAdapter() != null &&
                !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if ((!Terminal.isInitialized() && Hitpay.verifyGpsEnabled(TerminalActivity.this))) {
                initialize();
            } else {
                onRequestExitWorkflow();
            }
        } else {
            // If we don't have them yet, request them before doing anything else
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION);
        }
    }

    public void initialize() {
        try {
            Terminal.initTerminal(getApplicationContext(), LogLevel.VERBOSE, new TokenProvider(TerminalActivity.this),
                    new TerminalEventListener());
        } catch (TerminalException e) {
            Toast.makeText(TerminalActivity.this, "Location services are required in order to initialize the Terminal.", Toast.LENGTH_LONG).show();
            throw new RuntimeException("Location services are required in order to initialize " +
                    "the Terminal.", e);
        }
        navigateTo(TerminalFragment.TAG, new TerminalFragment());
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NotNull String[] permissions,
            @NotNull int[] grantResults
    ) {

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(TerminalActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                // If we receive a response to our permission check, initialize
                if ((!Terminal.isInitialized() && Hitpay.verifyGpsEnabled(TerminalActivity.this))) {
                    initialize();
                } else {
                    onRequestExitWorkflow();
                }
            } else {
                Toast.makeText(TerminalActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Callback function called when discovery has been canceled by the [DiscoveryFragment]
     */
    @Override
    public void onCancelDiscovery() {
        navigateTo(TerminalFragment.TAG, new TerminalFragment());
    }

    /**
     * Callback function called once discovery has been selected by the [TerminalFragment]
     */
    @Override
    public void onRequestDiscovery(boolean isSimulated) {
        navigateTo(DiscoveryFragment.TAG, DiscoveryFragment.newInstance(isSimulated));
    }

    /**
     * Callback function called to exit the payment workflow
     */
    @Override
    public void onRequestExitWorkflow() {
        if (Terminal.getInstance().getConnectionStatus() == ConnectionStatus.CONNECTED) {
            navigateTo(ConnectedReaderFragment.TAG, new ConnectedReaderFragment());
        } else {
            navigateTo(TerminalFragment.TAG, new TerminalFragment());
        }
    }

    /**
     * Callback function called to start a payment by the [PaymentFragment]
     */
    @Override
    public void onRequestPayment(int amount, @NotNull String currency) {
//        navigateTo(EventFragment.TAG, EventFragment.requestPayment(amount, currency));
    }

    /**
     * Callback function called once the payment workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    @Override
    public void onSelectPaymentWorkflow() {
        navigateTo(PaymentFragment.TAG, new PaymentFragment());
    }

    /**
     * Callback function called once the read card workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    @Override
    public void onSelectReadReusableCardWorkflow() {
//        navigateTo(EventFragment.TAG, EventFragment.readReusableCard());
    }

    /**
     * Callback function called once the update reader workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    @Override
    public void onSelectUpdateWorkflow() {
//        navigateTo(UpdateReaderFragment.TAG, new UpdateReaderFragment());
    }

    /**
     * Callback function called when collect payment method has been canceled
     */
    @Override
    public void onCancelCollectPaymentMethod() {
        navigateTo(ConnectedReaderFragment.TAG, new ConnectedReaderFragment());
    }

    /**
     * Callback function called on completion of [Terminal.connectReader]
     */
    @Override
    public void onConnectReader() {
        navigateTo(ConnectedReaderFragment.TAG, new ConnectedReaderFragment());
        if (Hitpay.hitPayTerminalListener != null) {
            Hitpay.hitPayTerminalListener.setupCompleted(true);
        }
    }

    @Override
    public void onDisconnectReader() {
        navigateTo(TerminalFragment.TAG, new TerminalFragment());
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment Fragment to navigate to.
     */
    private void navigateTo(String tag, Fragment fragment) {
        final Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, frag != null ? frag : fragment, tag)
                .commitAllowingStateLoss();
    }

    public void startLoading() {
        CircularProgressView progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressView.setVisibility(View.VISIBLE);
    }

    public void endLoading() {
        CircularProgressView progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressView.setVisibility(View.INVISIBLE);
    }
}