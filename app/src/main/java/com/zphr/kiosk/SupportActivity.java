package com.zphr.kiosk;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activity.ParentActivity;
import com.general.files.ActUtils;
import com.utils.Utils;
import com.view.MTextView;

public class SupportActivity extends ParentActivity {


    MTextView titleTxt;
    ImageView backImgView;

    LinearLayout aboutusarea, privacyarea, contactarea, helparea, termsCondArea;

    MTextView helpHTxt, contactHTxt, privacyHTxt, aboutusHTxt, termsHTxt;

    View seperationLine, seperationLine_contact, seperationLine_help;

    boolean islogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        initView();
        setLabel();
        islogin = getIntent().getBooleanExtra("islogin", false);
        if (islogin) {
            aboutusarea.setVisibility(View.GONE);
            contactarea.setVisibility(View.GONE);
            helparea.setVisibility(View.GONE);
            seperationLine_help.setVisibility(View.GONE);
            seperationLine_contact.setVisibility(View.GONE);
            seperationLine.setVisibility(View.GONE);

        }
    }


    private void initView() {

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        addToClickHandler(backImgView);
        helpHTxt = (MTextView) findViewById(R.id.helpHTxt);
        contactHTxt = (MTextView) findViewById(R.id.contactHTxt);
        privacyHTxt = (MTextView) findViewById(R.id.privacyHTxt);
        aboutusHTxt = (MTextView) findViewById(R.id.aboutusHTxt);
        termsHTxt = (MTextView) findViewById(R.id.termsHTxt);

        aboutusarea = (LinearLayout) findViewById(R.id.aboutusarea);
        privacyarea = (LinearLayout) findViewById(R.id.privacyarea);
        contactarea = (LinearLayout) findViewById(R.id.contactarea);
        helparea = (LinearLayout) findViewById(R.id.helparea);
        termsCondArea = (LinearLayout) findViewById(R.id.termsCondArea);

        seperationLine = (View) findViewById(R.id.seperationLine);
        seperationLine_contact = (View) findViewById(R.id.seperationLine_contact);
        seperationLine_help = (View) findViewById(R.id.seperationLine_help);


        addToClickHandler(aboutusarea);
        addToClickHandler(privacyarea);
        addToClickHandler(contactarea);
        addToClickHandler(helparea);
        addToClickHandler(termsCondArea);


    }

    public Context getActContext() {
        return SupportActivity.this;
    }


    private void setLabel() {

        helpHTxt.setText(generalFunc.retrieveLangLBl("FAQ", "LBL_FAQ_TXT"));
        contactHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
        privacyHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRIVACY_POLICY_TEXT"));
        aboutusHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ABOUT_US_TXT"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUPPORT_HEADER_TXT"));
        termsHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TERMS_AND_CONDITION"));

    }


    public void onClick(View view) {
        Utils.hideKeyboard(SupportActivity.this);
        Bundle bn = new Bundle();
        switch (view.getId()) {

            case R.id.backImgView:
                SupportActivity.super.onBackPressed();
                break;
            case R.id.contactarea:
                new ActUtils(getActContext()).startAct(ContactUsActivity.class);
                break;
        }
    }


}
