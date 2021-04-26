package com.hit_pay.hitpay.Util;

import java.util.ArrayList;

public class CurrencyModel {
    public String code;
    public String country;
    public int minimum;
    public Boolean isZeroDecimal;

    public CurrencyModel(String code, String country) {
        this.code = code;
        this.country = country;
    }

    public CurrencyModel(String code, String country, int minimum, Boolean isZeroDecimal) {
        this.code = code;
        this.country = country;
        this.minimum = minimum;
        this.isZeroDecimal = isZeroDecimal;
    }

    public static ArrayList<CurrencyModel> getAllCurrencies() {
        ArrayList<CurrencyModel> result = new ArrayList<>();
        result.add(new CurrencyModel("SGD","Singapore"));
//        result.add(new CurrencyModel("AED","United Arab Emirates Dirham"));
//        result.add(new CurrencyModel("AUD","Australian Dollar"));
//        result.add(new CurrencyModel("BRL","Brazilian Real"));
//        result.add(new CurrencyModel("CHF","Swiss Franc"));
//        result.add(new CurrencyModel("CNY","Chinese Yuan Renminbi"));
//        result.add(new CurrencyModel("DKK","Danish Krone"));
//        result.add(new CurrencyModel("EUR","Euro"));
//        result.add(new CurrencyModel("GBP", "Pound Sterling"));
//        result.add(new CurrencyModel("HKD", "Hong Kong Dollar"));
//        result.add(new CurrencyModel("IDR", "Indonesian rupiah"));
//        result.add(new CurrencyModel("JPY", "Japanese Yen"));
//        result.add(new CurrencyModel("KRW", "South Korean Won"));
//        result.add(new CurrencyModel("MXN", "Mexican Peso"));
//        result.add(new CurrencyModel("MYR", "Malaysian Ringgit"));
//        result.add(new CurrencyModel("NOK", "Norwegian Krone"));
//        result.add(new CurrencyModel("SEK", "Swedish Krona"));
//        result.add(new CurrencyModel("THB", "Thai Babt"));
//        result.add(new CurrencyModel("TRY", "Turkish Lira"));
//        result.add(new CurrencyModel("TWD", "New Taiwan Dollar"));
//        result.add(new CurrencyModel("USD", "United States Dollar"));
//        result.add(new CurrencyModel("VND", "Vietnamese Dong"));
//        result.add(new CurrencyModel("ZAR", "South African Rand"));
        return result;
    }

    public static ArrayList<String> getDecimalCurrencies() {
        ArrayList<String> result = new ArrayList<>();
        result.add("KRW");
        result.add("IDR");
        result.add("JPY");
        result.add("VND");
        return result;
    }
}
