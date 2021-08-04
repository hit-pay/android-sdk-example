package com.hit_pay.hitpay.terminal.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.hit_pay.hitpay.ClientAPI.Hitpay;
import com.hit_pay.hitpay.Managers.ApplicationPreferencesManager;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.AppConstants;
import com.hit_pay.hitpay.databinding.FragmentTerminalBinding;
import com.hit_pay.hitpay.terminal.DeviceTypeTerminalActivity;
import com.hit_pay.hitpay.terminal.NavigationListener;
import com.hit_pay.hitpay.terminal.viewmodel.TerminalViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static android.app.Activity.RESULT_OK;

/**
 * The `TerminalFragment` is the main [Fragment] shown in the app, and handles navigation to any
 * other [Fragment]s as necessary.
 */
public class TerminalFragment extends Fragment {
    private static final int REQUEST_CODE_DEVICE_TYPE = 111;

    public static final String TAG = "com.stripe.example.fragment.TerminalFragment";
    private static final String SIMULATED_SWITCH = "simulated_switch";

    private TerminalViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            viewModel = new TerminalViewModel(getArguments().getBoolean(SIMULATED_SWITCH));
        } else {
            final FragmentActivity activity = getActivity();
            final boolean isSimulated;
            if (activity != null) {
                final SharedPreferences prefs = activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
                if (prefs != null) {
                    isSimulated = prefs.getBoolean(SIMULATED_SWITCH, false);
                } else {
                    isSimulated = false;
                }
            } else {
                isSimulated = false;
            }
            viewModel = new TerminalViewModel(isSimulated);
        }
    }

    @Override
    public @Nullable View onCreateView(
            @NotNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        final FragmentTerminalBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_terminal, container, false);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.discover_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
//                ((NavigationListener) activity).onRequestDiscovery(viewModel.simulated.getValue());
                ((NavigationListener) activity).onRequestDiscovery(Hitpay.simulated);
            }
        });

        view.findViewById(R.id.simulated_switch).setOnClickListener(v -> {
            viewModel.simulated.setValue(!viewModel.simulated.getValue());
        });

        view.findViewById(R.id.img_back).setOnClickListener(v -> {
            getActivity().finish();
        });

        // TODO: Do this dynamically from the type selected
        setUIDeviceType();

        view.findViewById(R.id.device_type_button).setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), DeviceTypeTerminalActivity.class);
            i.putExtra(AppConstants.DEVICE_TYPE_TERMINAL, ApplicationPreferencesManager.getStringValue(getActivity(), AppConstants.DEVICE_TYPE_TERMINAL));
            startActivityForResult(i, REQUEST_CODE_DEVICE_TYPE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DEVICE_TYPE) {
            if (resultCode == RESULT_OK) {
                String deviceType = data.getStringExtra("DATA");
                ApplicationPreferencesManager.setValue(getActivity(), AppConstants.DEVICE_TYPE_TERMINAL, deviceType);
                setUIDeviceType();
            }
        }
    }

    private void setUIDeviceType() {
        if (ApplicationPreferencesManager.getStringValue(getActivity(), AppConstants.DEVICE_TYPE_TERMINAL) != null) {
            ((TextView) getView().findViewById(R.id.device_type_button)).setText(ApplicationPreferencesManager.getStringValue(getActivity(), AppConstants.DEVICE_TYPE_TERMINAL));
        } else {
            ((TextView) getView().findViewById(R.id.device_type_button)).setText(R.string.bluetooth_scan);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final SharedPreferences prefs = activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            if (prefs != null) {
                prefs.edit().putBoolean(SIMULATED_SWITCH, viewModel.simulated.getValue()).apply();
            }
        }
    }
}