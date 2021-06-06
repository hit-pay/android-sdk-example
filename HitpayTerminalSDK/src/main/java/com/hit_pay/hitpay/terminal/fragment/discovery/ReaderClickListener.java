package com.hit_pay.hitpay.terminal.fragment.discovery;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.hit_pay.hitpay.Managers.ApplicationPreferencesManager;
import com.hit_pay.hitpay.Util.AppConstants;
import com.hit_pay.hitpay.terminal.TerminalActivity;
import com.hit_pay.hitpay.terminal.viewmodel.DiscoveryViewModel;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener;
import com.stripe.stripeterminal.external.callable.ReaderCallback;
import com.stripe.stripeterminal.external.models.ConnectionConfiguration;
import com.stripe.stripeterminal.external.models.DiscoveryMethod;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderEvent;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class ReaderClickListener implements @Nullable BluetoothReaderListener {
    @NotNull
    private WeakReference<TerminalActivity> activityRef;
    @NotNull
    private final DiscoveryViewModel viewModel;
    private ProgressDialog mProgressBar;
    private DiscoveryMethod discoveryMethod;

    ReaderClickListener(
            @NotNull WeakReference<TerminalActivity> activityRef,
            @NotNull DiscoveryViewModel viewModel
    ) {
        this.activityRef = activityRef;
        this.viewModel = viewModel;
    }

    public void setActivityRef(@NotNull WeakReference<TerminalActivity> newRef) {
        activityRef = newRef;
    }

    public void onClick(@NotNull Reader reader) {
//        Terminal.getInstance().setTerminalListener(new TerminalEventListener() {
//            @Override
//            public void onConnectionStatusChange(@NotNull ConnectionStatus status) {
//                super.onConnectionStatusChange(status);
//                if (status == ConnectionStatus.CONNECTED) {
//                    connectSuccess();
//                } else if (status == ConnectionStatus.NOT_CONNECTED) {
//                    final TerminalActivity activity = activityRef.get();
//                    if (activity != null) {
//                        activity.runOnUiThread(() -> {
//                            viewModel.isConnecting.setValue(false);
//                            activity.onCancelDiscovery();
//                        });
//                    }
//                }
//            }
//        });
        viewModel.isConnecting.setValue(true);
        discoveryMethod = DiscoveryMethod.BLUETOOTH_SCAN;
        if (ApplicationPreferencesManager.getStringValue(activityRef.get(), AppConstants.DEVICE_TYPE_TERMINAL) != null) {
            if (ApplicationPreferencesManager.getStringValue(activityRef.get(), AppConstants.DEVICE_TYPE_TERMINAL).equals(DiscoveryMethod.INTERNET.toString())) {
                discoveryMethod = DiscoveryMethod.INTERNET;
            } else if (ApplicationPreferencesManager.getStringValue(activityRef.get(), AppConstants.DEVICE_TYPE_TERMINAL).equals(DiscoveryMethod.COTS.toString())) {
                discoveryMethod = DiscoveryMethod.COTS;
            }
        }

        if (discoveryMethod == DiscoveryMethod.INTERNET) {
            activityRef.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Terminal.getInstance().connectInternetReader(reader, new ConnectionConfiguration.InternetConnectionConfiguration(), readerCallback);
                }
            });
        } else {
            activityRef.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    ConnectionConfiguration.BluetoothConnectionConfiguration connectionConfig = new ConnectionConfiguration.BluetoothConnectionConfiguration("tml_exampleid");
                    Terminal.getInstance().connectBluetoothReader(reader, new ConnectionConfiguration.BluetoothConnectionConfiguration(reader.getLocation().getId()), ReaderClickListener.this, readerCallback);
                }
            });
        }
    }

    ReaderCallback readerCallback = new ReaderCallback() {
        @Override
        public void onSuccess(@NotNull Reader reader) {
            connectSuccess();
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            final TerminalActivity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    viewModel.isConnecting.setValue(false);
                    activity.onCancelDiscovery();
                });
            }
        }
    };

    private void connectSuccess() {
        final TerminalActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                viewModel.isConnecting.setValue(false);
                activity.onConnectReader();
            });
        }
    }

    private void showUpdateDialog(final ReaderSoftwareUpdate update, Reader reader) {
        final TerminalActivity activity = activityRef.get();
        new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Bluetooth Pairing Request")
                .setMessage(reader.getSerialNumber() + " would like to pair with your phone")
                .setCancelable(true)
                .setPositiveButton("Pair", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.runOnUiThread(() -> {
                            dialog.dismiss();
                            if (update != null) {
                                Terminal.getInstance().installAvailableUpdate();
                            }
                        });

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.runOnUiThread(() -> {
                            dialog.dismiss();
                        });
                    }
                }).create().show();
    }

    @Override
    public void onReportAvailableUpdate(@NotNull ReaderSoftwareUpdate readerSoftwareUpdate) {
        final TerminalActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                showUpdateDialog(readerSoftwareUpdate, Terminal.getInstance().getConnectedReader());
            });
        }
    }

    @Override
    public void onReportLowBatteryWarning() {

    }

    @Override
    public void onReportReaderSoftwareUpdateProgress(float progress) {
        final TerminalActivity activity = activityRef.get();
        Integer percent = (int) (progress * 100);
        if (activity != null) {
            activity.runOnUiThread(() -> {
                mProgressBar.setMessage("Update progress: " + percent + "%\n\nThe reader will temporarily become unresponsive. Do not leave this page, and keep the reader in range and powered on until the update is complete.");
                if (percent > 99) {
                    mProgressBar.cancel();
                }
            });
        }
    }

    @Override
    public void onFinishInstallingUpdate(@Nullable ReaderSoftwareUpdate readerSoftwareUpdate, @Nullable TerminalException e) {
        final TerminalActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (mProgressBar != null && mProgressBar.isShowing())
                    mProgressBar.cancel();
                Toast.makeText(activity, "Updated success", Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    public void onReportReaderEvent(@NotNull ReaderEvent readerEvent) {

    }

    @Override
    public void onRequestReaderDisplayMessage(@NotNull ReaderDisplayMessage readerDisplayMessage) {

    }

    @Override
    public void onRequestReaderInput(@NotNull ReaderInputOptions readerInputOptions) {

    }

    @Override
    public void onStartInstallingUpdate(@NotNull ReaderSoftwareUpdate readerSoftwareUpdate, @Nullable com.stripe.stripeterminal.external.callable.Cancelable cancelable) {
        final TerminalActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                mProgressBar = new ProgressDialog(activity);
                mProgressBar.setCancelable(false);
                mProgressBar.setMessage("Updating...");
                mProgressBar.show();
            });
        }
    }
}
