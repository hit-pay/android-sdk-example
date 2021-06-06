package com.hit_pay.hitpay.terminal.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TerminalViewModel extends ViewModel {
    public final MutableLiveData<Boolean> simulated;

    public TerminalViewModel() {
        this(false);
    }

    public TerminalViewModel(boolean simulated) {
        this.simulated = new MutableLiveData<>(simulated);
    }

}
