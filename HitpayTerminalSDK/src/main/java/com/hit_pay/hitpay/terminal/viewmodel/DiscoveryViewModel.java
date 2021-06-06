package com.hit_pay.hitpay.terminal.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hit_pay.hitpay.terminal.fragment.discovery.ReaderClickListener;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.models.Reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryViewModel extends ViewModel {
    public final MutableLiveData<List<? extends Reader>> readers;
    public final MutableLiveData<Boolean> isConnecting;
    public Cancelable discoveryTask;
    @Nullable public ReaderClickListener readerClickListener;

    public DiscoveryViewModel() {
        this(new ArrayList<>());
    }

    public DiscoveryViewModel(@NotNull List<Reader> readersParam) {
        readers = new MutableLiveData<>(readersParam);
        isConnecting = new MutableLiveData<>(false);
        discoveryTask = null;
        readerClickListener = null;
    }
}
