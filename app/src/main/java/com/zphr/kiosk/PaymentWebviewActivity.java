package com.zphr.kiosk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.activity.ParentActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.general.files.ActUtils;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.utils.Logger;
import com.utils.Utils;
import com.view.ErrorView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class PaymentWebviewActivity extends ParentActivity implements ErrorView.RetryListener {

    private LottieAnimationView loaderView;
    private WebView paymentWebview;
    private ImageView cancelImg;

    private boolean handleResponse = false, isApiCall = false;
    private JsPromptResult result = null;
    private String CouponCode = "", mFailingUrl = "";
    private ErrorView errorView;
    private InternetConnection internetConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        if (getIntent().getExtras().containsKey("CouponCode")) {
            CouponCode = getIntent().getStringExtra("CouponCode");
        }

        internetConnection = new InternetConnection(getActContext());

        isApiCall = getIntent().getBooleanExtra("isApiCall", false);

        paymentWebview = findViewById(R.id.paymentWebview);
        loaderView = findViewById(R.id.loaderView);
        cancelImg = findViewById(R.id.cancelImg);
        cancelImg.setOnClickListener(v -> finish());

        String url = getIntent().getStringExtra("url");

        handleResponse = getIntent().getBooleanExtra("handleResponse", false);

        Logger.d("WebViewURL", "::" + url);

        paymentWebview.setWebViewClient(new myWebClient());
        paymentWebview.getSettings().setJavaScriptEnabled(true);
        paymentWebview.getSettings().setDomStorageEnabled(true);
        paymentWebview.setWebChromeClient(new MyWebChromeClient());

        // Clear all the Application Cache, Web SQL Database and the HTML5 Web Storage
        WebStorage.getInstance().deleteAllData();

        // Clear all the cookies
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        paymentWebview.clearCache(true);
        paymentWebview.clearFormData();
        paymentWebview.clearHistory();
        paymentWebview.clearSslPreferences();

        mFailingUrl = url;
        paymentWebview.loadUrl(url);

        //cancelImg.setVisibility(View.VISIBLE);

        paymentWebview.setFocusable(true);
        paymentWebview.setVisibility(View.VISIBLE);

        errorView = findViewById(R.id.errorView);
        errorView.setVisibility(View.GONE);
        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");
        errorView.setOnRetryListener(this);

    }

    private Context getActContext() {
        return PaymentWebviewActivity.this;
    }

    @Override
    public void onBackPressed() {
        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_CANCEL_PAYMENT_PROCESS"), generalFunc.retrieveLangLBl("", "LBL_NO"), generalFunc.retrieveLangLBl("", "LBL_YES"), buttonId -> {
            if (buttonId == 1) {
                super.onBackPressed();
            }
        });
    }

    @Override
    public void onRetry() {
        if (internetConnection.isNetworkConnected() && internetConnection.check_int()) {
            paymentWebview.loadUrl(mFailingUrl);
            errorView.setVisibility(View.GONE);
            cancelImg.setVisibility(View.GONE);
            loaderView.setVisibility(View.VISIBLE);
        }
    }

    private class myWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            mFailingUrl = url;
            return true;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            String data = url;
            Logger.d("WebData", "::" + data);
            data = data.substring(data.indexOf("data") + 5);
            try {

                String datajson = URLDecoder.decode(data, "UTF-8");
                Logger.d("WebData", "::2222222::" + datajson);
                loaderView.setVisibility(View.VISIBLE);

                view.setOnTouchListener(null);

                /*if (url.contains("OPEN_IN_APP_WALLET=Yes")) {
                    new ActUtils(getActContext()).startAct(MyWalletActivity.class);
                    finish();
                }
                if (url.contains("BUSINESS_PROFILE_SETUP=Yes")) {
                    new ActUtils(getActContext()).startAct(BusinessProfileActivity.class);
                    finish();
                }*/


                if (url.contains("success=1") || url.contains("result.php?success=1") || url.contains("success_kiosk.php?success=1")) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("isPaymentSuccess", "Yes");
                    if (isApiCall) {
                        MyApp.getInstance().refreshData();
                        finish();
                        return;
                    }
                    loaderView.setVisibility(View.GONE);
                    cancelImg.setVisibility(View.GONE);
                    if (handleResponse) {



                        if (url.contains("LBL_MANUAL_STORE_THANK_YOU_ORDER_PLACE_ORDER")) {
                            returnIntent.putExtra("isGenieOrderPlace", "Yes");
                        }

                        if (url.contains("orderid")) {
                            Uri uri = Uri.parse(url);
                            String orderid = uri.getQueryParameter("orderid");
                            returnIntent.putExtra("iOrderId", orderid);
                            Logger.d("TESTOREDERID", "::" + orderid);
                        }

                        if (url.contains("paymentModeSelected=Yes")) {
                            returnIntent.putExtra("paymentModeSelected", "Yes");
                        }

                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }

                if (url.contains("success=0") || url.contains("page_action=close") || url.contains("page_action=contactus")) {

                    if (url.contains("page_action=close")) {
                        loaderView.setVisibility(View.GONE);
                        cancelImg.setVisibility(View.GONE);

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("isPaymentSuccess", "No");
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();


                    }
                    if (url.contains("page_action=contactus")) {
                        new ActUtils(getActContext()).startAct(ContactUsActivity.class);

                    }

                }
            } catch (UnsupportedEncodingException e) {
                Logger.d("TESTOREDERID", "::" + e);
                e.printStackTrace();
            }

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            //super.onReceivedError(view, request, error);
            view.stopLoading();
            view.loadData("", "", "");
            errorView.setVisibility(View.VISIBLE);
            cancelImg.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Logger.d("TESTOREDERID", "::" + description + "::" + errorCode + "::" + failingUrl);

            errorView.setVisibility(View.VISIBLE);
            loaderView.setVisibility(View.GONE);
            cancelImg.setVisibility(View.VISIBLE);

            view.loadData("", "", "");
            mFailingUrl = failingUrl;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onPageFinished(WebView view, String url) {
            loaderView.setVisibility(View.GONE);
            cancelImg.setVisibility(View.GONE);
            if (errorView.getVisibility() == View.VISIBLE) {
                cancelImg.setVisibility(View.VISIBLE);
            }

            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            });

        }
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            Logger.d("alert > onJsAlert", message);
            generalFunc.showGeneralMessage("", message, generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TXT"), generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"), button_Id -> {
                if (button_Id == 1) {
                    result.confirm();
                } else {
                    result.cancel();
                }
            });
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {

            Logger.d("alert > onJsConfirm", message);
            generalFunc.showGeneralMessage("", message, generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TXT"), generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"), button_Id -> {
                if (button_Id == 1) {
                    result.confirm();
                } else {
                    result.cancel();
                }
            });

            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            Logger.d("alert > onJsConfirm", message + "::" + url + "::" + defaultValue);

            if (defaultValue.contains("OPEN_PROMOCODE")) {
                Bundle bn = new Bundle();
                String[] filterString = defaultValue.split("_");

                bn.putString("CouponCode", CouponCode);
                if (filterString.length >= 3) {
                    bn.putString("CouponCode", filterString[2]);
                }

                bn.putString("eType", getIntent().getStringExtra("eType"));
                bn.putBoolean("eFly", getIntent().getBooleanExtra("eFly", false));
                if (getIntent().getExtras().containsKey("vSourceLatitude")) {
                    bn.putString("vSourceLatitude", getIntent().getStringExtra("vSourceLatitude"));
                    bn.putString("vSourceLongitude", getIntent().getStringExtra("vSourceLongitude"));
                }
                if (getIntent().getExtras().containsKey("vDestLatitude")) {
                    bn.putString("vDestLatitude", getIntent().getStringExtra("vDestLatitude"));
                    bn.putString("vDestLongitude", getIntent().getStringExtra("vDestLongitude"));
                }
                bn.putString("eTakeAway", getIntent().getStringExtra("eTakeAway"));

                //new ActUtils(getActContext()).startActForResult(CouponActivity.class, bn, Utils.SELECT_COUPON_REQ_CODE);
                setJsPromptResult(result);


                return true;


            } else {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        }
    }

    private void setJsPromptResult(JsPromptResult result) {
        this.result = result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //getProfilePaymentModel.getProfilePayment(getIntent().getStringExtra("eType"), this, null, false, false);

        if (requestCode == Utils.SELECT_COUPON_REQ_CODE && resultCode == RESULT_OK) {
            String couponCode = data != null ? data.getStringExtra("CouponCode") : null;
            CouponCode = couponCode;
            if (couponCode == null) {
                CouponCode = "";
                couponCode = "";
            }
            if (this.result != null) {
                result.confirm(couponCode);
            }
        } else {
            if (this.result != null) {
                result.confirm(CouponCode);
            }
        }

    }
}