package com.hit_pay.hitpay.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.hit_pay.hitpay.ClientAPI.HitPayAPI;
import com.hit_pay.hitpay.ClientAPI.OnComplete;
import com.hit_pay.hitpay.Managers.AppManager;
import com.hit_pay.hitpay.Managers.ApplicationPreferencesManager;
import com.hit_pay.hitpay.R;
import com.hit_pay.hitpay.Util.AppConstants;
import com.hit_pay.hitpay.Util.HitpayUtil;

import org.json.JSONObject;

public class NewStripeConnectLoginActivity extends AppCompatActivity {

    WebView webView;
    CircularProgressView progressView;
    ImageView close;
    TextView loadinTxt;
    Boolean hasStripe = false;
    TextView heading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_stripe_connect);
        webView = (WebView) findViewById(R.id.webview);
        webView.clearCache(true);
        progressView = (CircularProgressView) findViewById(R.id.progress_view);
        heading = (TextView) findViewById(R.id.heading);
        loadinTxt = (TextView) findViewById(R.id.loading_text);
        close = (ImageView) findViewById(R.id.ic_cancel);

        hasStripe = getIntent().getBooleanExtra(AppConstants.EXTRA_HAS_STRIPE , false);
        System.out.println("has stripe "+hasStripe);
        selectionDone(hasStripe);
    }

    private void selectionDone(Boolean hasStripe){
        this.hasStripe = hasStripe;
        heading.setVisibility(View.VISIBLE);
        if (hasStripe){
            heading.setText("Login With Stripe");
        }else{
            heading.setText("Create HitPay Account");
        }

        close.setVisibility(View.VISIBLE);
        webViewSetup();
    }

    private void webViewSetup(){
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean aBoolean) {

            }
        });
        HitPayAPI api = new HitPayAPI(this);
        final String baseURL = api.baseUrl;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                startLoading();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                System.out.println("finished \n\n");
                System.out.println("AAAAAAAAAAA: " + url);
                if (url.startsWith("https://connect.stripe")){
                    endLoading();
                }else{
                    startLoading();
                }
                if (url.contains(baseURL + "/callback/stripe")) {
                    getJsonCallback(url);
                }

//                String urlB = baseURL.replace("/api","");
//                if(url.contains(urlB)){
//                    Uri uri = Uri.parse(url);
//                    Set<String> args = uri.getQueryParameterNames();
//                    String code = uri.getQueryParameter("code");
//                    String state= uri.getQueryParameter("state");
//                    String scope = uri.getQueryParameter("scope");
//                    AppManager.hideKeyboard(NewStripeConnectActivity.this);
//                    submitStripeCode(code,scope,state);
//                }
            }
        });
//        loadURL();
        webView.loadUrl(getIntent().getStringExtra("URL"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void getJsonCallback(String url) {
        new HitPayAPI(NewStripeConnectLoginActivity.this).getJsonCallback(url, new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorMessage != null) {
                            endLoading();
                            AppManager.showErrorAlert(NewStripeConnectLoginActivity.this, errorMessage);
                        } else {
                            submitJson(response);
                        }
                    }
                });
            }
            @Override
            public void needUpdate() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HitpayUtil.showUpdateDialog(NewStripeConnectLoginActivity.this);
                    }
                });
            }
        });
    }

    private void submitJson(final JSONObject jsonObject) {
        new HitPayAPI(NewStripeConnectLoginActivity.this).submitJsonLogin(jsonObject,  new OnComplete<JSONObject>() {
            @Override
            public void done(final JSONObject response, final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endLoading();
                        if (errorMessage != null) {
                            AppManager.showErrorAlert(NewStripeConnectLoginActivity.this, errorMessage);
                        } else {
                            HitPayAPI api = new HitPayAPI(NewStripeConnectLoginActivity.this);
                            api.getProfileInfo(new OnComplete<Boolean>() {
                                @Override
                                public void done(final Boolean response, final String errorMessage) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            endLoading();
                                            if (errorMessage == null) {
                                                Toast.makeText(NewStripeConnectLoginActivity.this, "Login success", Toast.LENGTH_SHORT).show();

//                                                Intent i = new Intent(NewStripeConnectLoginActivity.this, HomeDrawerActivity.class);
//                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                startActivity(i);
                                            } else {
                                                AppManager.showErrorAlert(NewStripeConnectLoginActivity.this, errorMessage);
                                            }
                                        }
                                    });
                                }
                                @Override
                                public void needUpdate() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            HitpayUtil.showUpdateDialog(NewStripeConnectLoginActivity.this);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void needUpdate() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HitpayUtil.showUpdateDialog(NewStripeConnectLoginActivity.this);
                    }
                });
            }
        });
    }

    private void startLoading(){
        progressView.setVisibility(View.VISIBLE);
        progressView.animate();
        webView.setVisibility(View.INVISIBLE);
        loadinTxt.setVisibility(View.VISIBLE);
    }

    private void endLoading(){
        progressView.setVisibility(View.INVISIBLE);
        webView.setVisibility(View.VISIBLE);
        loadinTxt.setVisibility(View.INVISIBLE);
    }

    private void loadURL(){
        startLoading();
        String bussinessID = ApplicationPreferencesManager.getStringValue(NewStripeConnectLoginActivity.this, AppConstants.PREF_BUSSINESS_ID);
        new HitPayAPI(this).getStripeUrlForHitpaySignup(bussinessID, new OnComplete<String>() {
            @Override
            public void done(final String url, final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (url == null){
                            AppManager.showErrorAlert(NewStripeConnectLoginActivity.this,"Error",errorMessage);
                            return;
                        }
                        webView.loadUrl(url);
                    }
                });
            }
            @Override
            public void needUpdate() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HitpayUtil.showUpdateDialog(NewStripeConnectLoginActivity.this);
                    }
                });
            }
        });
    }
}
