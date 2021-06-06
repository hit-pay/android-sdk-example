package com.hit_pay.hitpay.terminal.fragment.discovery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hit_pay.hitpay.Managers.ApplicationPreferencesManager;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.AppConstants;
import com.hit_pay.hitpay.databinding.FragmentDiscoveryBinding;
import com.hit_pay.hitpay.terminal.TerminalActivity;
import com.hit_pay.hitpay.terminal.viewmodel.DiscoveryViewModel;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.DiscoveryListener;
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration;
import com.stripe.stripeterminal.external.models.DiscoveryMethod;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * The `DiscoveryFragment` shows the list of recognized readers and allows the user to
 * select one to connect to.
 */
public class DiscoveryFragment extends Fragment implements DiscoveryListener {

    public static final String TAG = "com.stripe.example.fragment.discovery.DiscoveryFragment";
    private static final String SIMULATED_KEY = "simulated";

    public static DiscoveryFragment newInstance(boolean simulated) {
        final DiscoveryFragment fragment = new DiscoveryFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(SIMULATED_KEY, simulated);
        fragment.setArguments(bundle);
        return fragment;
    }

    private ReaderAdapter adapter;
    private FragmentDiscoveryBinding binding;
    private RecyclerView readerRecyclerView;
    private DiscoveryViewModel viewModel;
    private WeakReference<TerminalActivity> activityRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(DiscoveryViewModel.class);

        activityRef = new WeakReference<>((TerminalActivity) getActivity());
        if (viewModel.readerClickListener == null) {
            viewModel.readerClickListener = new ReaderClickListener(activityRef, viewModel);
        } else {
            viewModel.readerClickListener.setActivityRef(activityRef);
        }

        if (getArguments() != null) {
            DiscoveryMethod discoveryMethod = DiscoveryMethod.BLUETOOTH_SCAN;
            if (ApplicationPreferencesManager.getStringValue(getActivity(), AppConstants.DEVICE_TYPE_TERMINAL) != null) {
                if (ApplicationPreferencesManager.getStringValue(getActivity(), AppConstants.DEVICE_TYPE_TERMINAL).equals(DiscoveryMethod.INTERNET.toString())) {
                    discoveryMethod = DiscoveryMethod.INTERNET;
                } else if (ApplicationPreferencesManager.getStringValue(getActivity(), AppConstants.DEVICE_TYPE_TERMINAL).equals(DiscoveryMethod.COTS.toString())) {
                    discoveryMethod = DiscoveryMethod.COTS;
                }
            }
            DiscoveryConfiguration config = new DiscoveryConfiguration(0,
                    discoveryMethod, getArguments().getBoolean(SIMULATED_KEY));
            if (viewModel.discoveryTask == null && Terminal.getInstance().getConnectedReader() == null) {
                viewModel.discoveryTask = Terminal
                        .getInstance()
                        .discoverReaders(config, this, discoveryCallback);
            }
        }
    }

    final Callback discoveryCallback = new Callback() {
        @Override
        public void onSuccess() {
            viewModel.discoveryTask = null;
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            viewModel.discoveryTask = null;
            final TerminalActivity activity = activityRef.get();
            if (activity != null) {
                activity.onCancelDiscovery();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(
            @NotNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_discovery, container, false);
        binding.setLifecycleOwner(this);
        readerRecyclerView = binding.getRoot().findViewById(R.id.reader_recycler_view);
        readerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.setViewModel(viewModel);

        adapter = new ReaderAdapter(viewModel);
        readerRecyclerView.setAdapter(adapter);

        binding.getRoot()
                .findViewById(R.id.cancel_button)
                .setOnClickListener(view -> {
                    if (viewModel.discoveryTask != null) {
                        viewModel.discoveryTask.cancel(new Callback() {
                            @Override
                            public void onSuccess() {
                                viewModel.discoveryTask = null;
                                final TerminalActivity activity = activityRef.get();
                                if (activity != null) {
                                    activity.onCancelDiscovery();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull TerminalException e) {
                                viewModel.discoveryTask = null;
                            }
                        });
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onUpdateDiscoveredReaders(@NotNull List<? extends Reader> readers) {
        final TerminalActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() ->
                    viewModel.readers.setValue(readers));
        }
    }
}
