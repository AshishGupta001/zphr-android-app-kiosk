package com.zphr.kiosk;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activity.ParentActivity;
import com.general.files.GeneralFunctions;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class FareBreakDownActivity extends ParentActivity {


    MTextView titleTxt;
    ImageView backImgView;
    MTextView fareBreakdownNoteTxt;
    MTextView carTypeTitle;
    LinearLayout fareDetailDisplayArea;

    String selectedcar = "";
    String iUserId = "";
    String distance = "";
    String time = "";
    String PromoCode = "";
    String vVehicleType = "";

    boolean isSkip;
    boolean isFixFare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_break_down);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        fareBreakdownNoteTxt = (MTextView) findViewById(R.id.fareBreakdownNoteTxt);
        carTypeTitle = (MTextView) findViewById(R.id.carTypeTitle);
        backImgView = (ImageView) findViewById(R.id.backImgView);
      addToClickHandler(backImgView);
        fareDetailDisplayArea = (LinearLayout) findViewById(R.id.fareDetailDisplayArea);
        selectedcar = getIntent().getStringExtra("SelectedCar");
        iUserId = getIntent().getStringExtra("iUserId");
        distance = getIntent().getStringExtra("distance");
        time = getIntent().getStringExtra("time");
        PromoCode = getIntent().getStringExtra("PromoCode");
        vVehicleType = getIntent().getStringExtra("vVehicleType");
        isFixFare = getIntent().getBooleanExtra("isFixFare", false);
        setLabels();
        isSkip = getIntent().getBooleanExtra("isSkip", false);
        callBreakdownRequest();

    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FARE_BREAKDOWN_TXT"));
        if (isFixFare) {
            fareBreakdownNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GENERAL_NOTE_FLAT_FARE_EST"));
        } else {
            fareBreakdownNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GENERAL_NOTE_FARE_EST"));
        }
        carTypeTitle.setText(vVehicleType);


    }

    public Context getActContext() {
        return FareBreakDownActivity.this;
    }

    public void callBreakdownRequest() {


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getEstimateFareDetailsArr");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("distance", distance);
        parameters.put("time", time);
        parameters.put("SelectedCar", selectedcar);
        parameters.put("PromoCode", PromoCode);

        if (!isSkip) {
            if (getIntent().getStringExtra("destLat") != null && !getIntent().getStringExtra("destLat").equalsIgnoreCase("")) {
                parameters.put("DestLatitude", getIntent().getStringExtra("destLat"));
                parameters.put("DestLongitude", getIntent().getStringExtra("destLong"));
                parameters.put("isDestinationAdded", "Yes");
            } else {
                parameters.put("isDestinationAdded", "No");

            }
            if (getIntent().getStringExtra("picupLat") != null && !getIntent().getStringExtra("picupLat").equalsIgnoreCase("")) {
                parameters.put("StartLatitude", getIntent().getStringExtra("picupLat"));
                parameters.put("EndLongitude", getIntent().getStringExtra("pickUpLong"));
            }

        } else {
            parameters.put("isDestinationAdded", "No");


        }


        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {


                    JSONArray FareDetailsArrNewObj = null;
                    FareDetailsArrNewObj = generalFunc.getJsonArray(Utils.message_str, responseString);
                    addFareDetailLayout(FareDetailsArrNewObj);


                } else {

                }
            } else {
                generalFunc.showError();
            }
        });


    }

    private void addFareDetailLayout(JSONArray jobjArray) {

        if (fareDetailDisplayArea.getChildCount() > 0) {
            fareDetailDisplayArea.removeAllViewsInLayout();
        }

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                addFareDetailRow(jobject.names().getString(0), jobject.get(jobject.names().getString(0)).toString(), (jobjArray.length() - 1) == i ? true : false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        View convertView = null;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            params.setMarginStart(Utils.dipToPixels(getActContext(), 10));
            params.setMarginEnd(Utils.dipToPixels(getActContext(), 10));
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_breakdown_row, null);

            convertView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            convertView.setMinimumHeight(Utils.dipToPixels(getActContext(), 40));

            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            titleHTxt.setTextColor(Color.parseColor("#303030"));
            titleVTxt.setTextColor(Color.parseColor("#111111"));
        }

        if (convertView != null)
            fareDetailDisplayArea.addView(convertView);
    }


    public void onClick(View view) {
        Utils.hideKeyboard(getActContext());
        switch (view.getId()) {
            case R.id.backImgView:
                FareBreakDownActivity.super.onBackPressed();
                break;

        }
    }

}
