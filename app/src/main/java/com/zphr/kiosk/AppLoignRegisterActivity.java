package com.zphr.kiosk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activity.ParentActivity;
import com.fragments.SignInFragment;
import com.fragments.SignUpFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.utils.Utils;
import com.view.MTextView;

public class AppLoignRegisterActivity extends ParentActivity implements GoogleApiClient.OnConnectionFailedListener {

    public MTextView titleTxt;

    ImageView backImgView;
    FrameLayout container;
    String type = "";
    GoogleApiClient mGoogleApiClient;
    public SignUpFragment signUpFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.IS_KIOSK_APP) {
            setContentView(R.layout.activity_app_loign_register);
            type = getIntent().getStringExtra("type");

            initview();
            setLabel();
        } else {
            try {
                setContentView(R.layout.activity_app_loign_register);
                type = getIntent().getStringExtra("type");

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                mGoogleApiClient = new GoogleApiClient.Builder(getActContext())
                        .enableAutoManage(this, this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();

                initview();
                setLabel();
            } catch (Exception e) {

            }
        }

    }

    private void initview() {
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        addToClickHandler(backImgView);
        container = (FrameLayout) findViewById(R.id.container);

        if (Utils.IS_KIOSK_APP) {
            backImgView.setVisibility(View.GONE);
        }
    }

    public Context getActContext() {
        return AppLoignRegisterActivity.this;
    }


    private void setLabel() {

        if (type != null) {
            if (type.equals("login")) {
                if (Utils.IS_KIOSK_APP) {
                    ((LinearLayout.LayoutParams) container.getLayoutParams()).gravity = Gravity.CENTER | Gravity.CENTER_VERTICAL;
                }

                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SIGN_IN_TXT"));
                signUpFragment = null;
                hadnleFragment(new SignInFragment());
            } else {
                if (Utils.IS_KIOSK_APP) {
                    backImgView.setVisibility(View.VISIBLE);
                }

                titleTxt.setText(Utils.IS_KIOSK_APP ? generalFunc.retrieveLangLBl("", "LBL_BOOK_TAXI") : generalFunc.retrieveLangLBl("", "LBL_SIGN_UP"));
                signUpFragment = new SignUpFragment();
                hadnleFragment(signUpFragment);
            }
        }
    }

    public void hadnleFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE) {
            if (signUpFragment != null) {
                SignUpFragment.setdata(requestCode, resultCode, data);
            } else {
                SignInFragment.setdata(requestCode, resultCode, data);
            }
        } else {
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionExit();
        finish();
    }


    public void onClick(View view) {

        int i = view.getId();
        Utils.hideKeyboard(getActContext());
        if (i == backImgView.getId()) {
            Utils.hideKeyboard(AppLoignRegisterActivity.this);
            finish();
        }

    }


    // activity start & finish transition effect started

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    // activity start & finish transition effect ended

}
