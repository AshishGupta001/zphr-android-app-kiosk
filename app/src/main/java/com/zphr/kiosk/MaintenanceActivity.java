package com.zphr.kiosk;

import android.os.Bundle;
import android.view.View;


import com.activity.ParentActivity;
import com.general.files.ActUtils;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

public class MaintenanceActivity extends ParentActivity {

    MTextView maitenanceHTxt, maitenanceMsgTxt;


    MButton btn_type2;
    int submitBtnId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setId(submitBtnId);

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);
        addToClickHandler(btn_type2);
        maitenanceMsgTxt = (MTextView) findViewById(R.id.maitenanceMsgTxt);
        maitenanceHTxt = (MTextView) findViewById(R.id.maitenanceHTxt);
        maitenanceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MAINTENANCE_HEADER_MSG"));
        maitenanceMsgTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MAINTENANCE_CONTENT_MSG"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_HEADER_TXT"));


    }


    public void onClick(View view) {
        int i = view.getId();

        if (i == submitBtnId) {
            new ActUtils(MaintenanceActivity.this).startAct(ContactUsActivity.class);
        }


    }

}
