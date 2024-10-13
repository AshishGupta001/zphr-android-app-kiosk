package com.zphr.kiosk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;


import com.activity.ParentActivity;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.ActUtils;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AppLoginActivity extends ParentActivity {


    MTextView introductondetailstext, languageText, currancyText, loginbtn, registerbtn;

    LinearLayout languagearea, currencyarea;

    LinearLayout languageCurrancyArea;

    GenerateAlertBox languageListAlertBox;

    String selected_language_code = "";

    ArrayList<HashMap<String, String>> languageDataList = new ArrayList<>();
    ArrayList<HashMap<String, String>> currencyDataList = new ArrayList<>();

    String selected_currency = "";
    String selected_currency_symbol = "";

    GenerateAlertBox currencyListAlertBox;

    String type = "";

    AVLoadingIndicatorView loaderView;
    InternetConnection intCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_app_login);


        intCheck = new InternetConnection(getActContext());
        generalFunc.getHasKey(getActContext());
        initview();
        setLabel();
        buildLanguageList();


    }

    private void initview() {

        introductondetailstext = (MTextView) findViewById(R.id.introductondetailstext);
        languageText = (MTextView) findViewById(R.id.languageText);
        currancyText = (MTextView) findViewById(R.id.currancyText);

        languagearea = (LinearLayout) findViewById(R.id.languagearea);
        currencyarea = (LinearLayout) findViewById(R.id.currencyarea);
        loginbtn = (MTextView) findViewById(R.id.loginbtn);
        registerbtn = (MTextView) findViewById(R.id.registerbtn);

        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        loaderView.setVisibility(View.GONE);

        languageCurrancyArea = (LinearLayout) findViewById(R.id.languageCurrancyArea);


        addToClickHandler(loginbtn);
        addToClickHandler(registerbtn);
        addToClickHandler(languagearea);
        addToClickHandler(currencyarea);


    }


    private void setLabel() {
        introductondetailstext.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PASSENGER_INTRO_DETAILS"));
        loginbtn.setText(generalFunc.retrieveLangLBl("", "LBL_LOGIN"));
        registerbtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_REGISTER_TXT"));

        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.DEFAULT_LANGUAGE_VALUE, "");
        data.put(Utils.DEFAULT_CURRENCY_VALUE, "");
        data = generalFunc.retrieveValue(data);
        languageText.setText(data.get(Utils.DEFAULT_LANGUAGE_VALUE));
        currancyText.setText(data.get(Utils.DEFAULT_CURRENCY_VALUE));


    }


    public void buildLanguageList() {
        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.LANGUAGE_LIST_KEY, "");
        data.put(Utils.LANGUAGE_CODE_KEY, "");
        data = generalFunc.retrieveValue(data);

        GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActContext());
        generateAlertBox.setContentMessage(getSelectLangText(), null);
        generateAlertBox.setCancelable(true);

        JSONArray languageList_arr = generalFunc.getJsonArray(data.get(Utils.LANGUAGE_LIST_KEY));
        languageDataList.clear();
        for (int i = 0; i < languageList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(languageList_arr, i);

            HashMap<String, String> mapData = new HashMap<>();
            mapData.put("vTitle", generalFunc.getJsonValue("vTitle", obj_temp.toString()));
            mapData.put("vCode", generalFunc.getJsonValue("vCode", obj_temp.toString()));

            languageDataList.add(mapData);

            if ((data.get(Utils.LANGUAGE_CODE_KEY)).equals(generalFunc.getJsonValue("vCode", obj_temp.toString()))) {
                selected_language_code = generalFunc.getJsonValue("vCode", obj_temp.toString());
            }
        }

        generateAlertBox.createList(languageDataList, "vTitle", (position) -> {

            if (languageListAlertBox != null) {
                languageListAlertBox.closeAlertBox();
            }

            HashMap<String, String> mapData = languageDataList.get(position);

            selected_language_code = mapData.get("vCode");

            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                generalFunc.showGeneralMessage("",
                        generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
            } else {
                if (!generalFunc.retrieveValue(Utils.DEFAULT_LANGUAGE_VALUE).equals(mapData.get("vTitle"))) {
                    languageText.setText(mapData.get("vTitle"));
                    HashMap<String, String> storeData = new HashMap<>();
                    storeData.put(Utils.LANGUAGE_CODE_KEY, selected_language_code);
                    storeData.put(Utils.DEFAULT_LANGUAGE_VALUE, mapData.get("vTitle"));
                    generalFunc.storeData(storeData);

                    changeLanguagedata(selected_language_code);
                }
            }
        });

        languageListAlertBox = generateAlertBox;

        if (languageDataList.size() < 2) {
            languagearea.setVisibility(View.GONE);
        }

        buildCurrencyList();

    }

    public void buildCurrencyList() {
        GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActContext());
        generateAlertBox.setContentMessage(generalFunc.retrieveLangLBl("", "LBL_SELECT_CURRENCY"), null);
        generateAlertBox.setCancelable(true);

        JSONArray currencyList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.CURRENCY_LIST_KEY));
        currencyDataList.clear();
        for (int i = 0; i < currencyList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(currencyList_arr, i);

            HashMap<String, String> mapData = new HashMap<>();
            mapData.put("vName", generalFunc.getJsonValue("vName", obj_temp.toString()));
            mapData.put("vSymbol", generalFunc.getJsonValue("vSymbol", obj_temp.toString()));

            currencyDataList.add(mapData);

        }

        generateAlertBox.createList(currencyDataList, "vName", (position) -> {

            if (currencyListAlertBox != null) {
                currencyListAlertBox.closeAlertBox();
            }

            HashMap<String, String> mapData = currencyDataList.get(position);

            selected_currency_symbol = mapData.get("vSymbol");

            selected_currency = mapData.get("vName");
            currancyText.setText(mapData.get("vName"));

            generalFunc.storeData(Utils.DEFAULT_CURRENCY_VALUE, selected_currency);

        });

        currencyListAlertBox = generateAlertBox;

        if (currencyDataList.size() < 2) {
            currencyarea.setVisibility(View.GONE);

            if (languageDataList.size() < 2) {
                languageCurrancyArea.setVisibility(View.GONE);
            }
        }
    }

    public Context getActContext() {
        return AppLoginActivity.this;
    }


    public String getSelectLangText() {
        return ("" + generalFunc.retrieveLangLBl("Select", "LBL_SELECT_LANGUAGE_HINT_TXT"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void showLanguageList() {
        if (currencyListAlertBox.alertDialog == null || (currencyListAlertBox.alertDialog != null && !currencyListAlertBox.alertDialog.isShowing())) {
            languageListAlertBox.showAlertBox();
        }
    }

    public void showCurrencyList() {
        if (languageListAlertBox.alertDialog == null || (languageListAlertBox.alertDialog != null && !languageListAlertBox.alertDialog.isShowing())) {
            currencyListAlertBox.showAlertBox();
        }
    }

    public void changeLanguagedata(String langcode) {
        loaderView.setVisibility(View.VISIBLE);
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "changelanguagelabel");
        parameters.put("vLang", langcode);
        ApiHandler.execute(getActContext(), parameters, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                    HashMap<String, String> storeData = new HashMap<>();
                    storeData.put(Utils.languageLabelsKey, generalFunc.getJsonValue(Utils.message_str, responseString));
                    storeData.put(Utils.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValue("eType", responseString));
                    generalFunc.storeData(storeData);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            loaderView.setVisibility(View.GONE);
                            generalFunc.restartApp();
                        }
                    }, 2000);


                } else {
                    loaderView.setVisibility(View.GONE);

                }
            } else {
                loaderView.setVisibility(View.GONE);

            }

        });

    }


    public void onClick(View view) {
        int i = view.getId();
        Utils.hideKeyboard(getActContext());
        if (i == R.id.languagearea) {

            if (loaderView.getVisibility() == View.GONE) {
                showLanguageList();
            }

        } else if (i == R.id.currencyarea) {
            if (loaderView.getVisibility() == View.GONE) {
                showCurrencyList();
            }
        } else if (i == R.id.loginbtn) {
            if (loaderView.getVisibility() == View.GONE) {
                Bundle bundle = new Bundle();
                bundle.putString("type", "login");

                new ActUtils(getActContext()).startActWithData(AppLoignRegisterActivity.class, bundle);
            }


        } else if (i == R.id.registerbtn) {
            if (loaderView.getVisibility() == View.GONE) {
                Bundle bundle = new Bundle();
                bundle.putString("type", "register");
                new ActUtils(getActContext()).startActWithData(AppLoignRegisterActivity.class, bundle);
            }

        }
    }


}
