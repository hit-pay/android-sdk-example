package com.hit_pay.hitpay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.CurrencyModel;

import java.util.ArrayList;

public class CurrencyAdapter extends BaseAdapter {
    Context context;
    ArrayList<CurrencyModel> data;
    LayoutInflater inflter;

    public CurrencyAdapter(Context applicationContext, ArrayList<CurrencyModel> data) {
        this.context = applicationContext;
        this.data = data;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = inflter.inflate(R.layout.spinner_pay_mode, null);
        TextView names = (TextView) view.findViewById(R.id.currency_text);
        names.setText(data.get(i).code);
        return view;

    }
}
