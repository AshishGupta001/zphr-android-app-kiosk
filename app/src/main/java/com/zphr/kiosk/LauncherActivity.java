package com.zphr.kiosk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import com.activity.ParentActivity;
import com.general.files.ActUtils;
import com.general.files.ConfigureMemberData;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.OpenMainProfile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.snackbar.Snackbar;
import com.service.handler.ApiHandler;
import com.service.server.ServerTask;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class LauncherActivity extends ParentActivity implements GenerateAlertBox.HandleAlertBtnClick, ProviderInstaller.ProviderInstallListener {

    private GenerateAlertBox generateAlert;
    private static final int ERROR_DIALOG_REQUEST_CODE = 1;

    private AVLoadingIndicatorView loaderView;
    private String alertType = "";

    private long autoLoginStartTime = 0;
    private boolean mRetryProviderInstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        generalFunc.storeData("isInLauncher", "true");

        generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setBtnClickList(this);
        setDefaultAlertBtn();
        generateAlert.setCancelable(false);

        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);

        ProviderInstaller.installIfNeededAsync(this, this);
    }

    private void setDefaultAlertBtn() {
        generateAlert.resetBtn();
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));
    }

    private void checkConfigurations(boolean isPermissionShown) {
        int status = (GoogleApiAvailability.getInstance()).isGooglePlayServicesAvailable(getActContext());
        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            showErrorOnPlayServiceDialog(generalFunc.retrieveLangLBl("This application requires updated google play service. " +
                    "Please install Or update it from play store", "LBL_UPDATE_PLAY_SERVICE_NOTE"));
            return;
        } else if (status != ConnectionResult.SUCCESS) {
            showErrorOnPlayServiceDialog(generalFunc.retrieveLangLBl("This application requires updated google play service. " +
                    "Please install Or update it from play store", "LBL_UPDATE_PLAY_SERVICE_NOTE"));
            return;
        }

        if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
            showNoInternetDialog();
        } else {

            continueProcess();
        }
    }

    private void continueProcess() {
        showLoader();
        Utils.setAppLocal(getActContext());
        if (Utils.IS_KIOSK_APP ? Utils.checkText(generalFunc.getHotelId()) : (generalFunc.isUserLoggedIn() && Utils.checkText(generalFunc.getMemberId()))) {
            autoLogin();
        } else {
            downloadGeneralData();
        }
    }

    private void restartappDailog() {
        GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        alertType = "";
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("Please try again.", "LBL_TRY_AGAIN_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateAlert.setBtnClickList(btn_id -> generalFunc.restartApp());
        generateAlert.showAlertBox();
    }

    private void downloadGeneralData() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "generalConfigData");
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);
        parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("vCurrency", generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE));
        ApiHandler.execute(getActContext(), parameters, responseString -> {
            if (isFinishing()) {
                restartappDailog();
                return;
            }
            if (responseString != null && !responseString.equals("")) {
                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                    generalFunc.storeData("TSITE_DB", generalFunc.getJsonValue("TSITE_DB", responseString));
                    generalFunc.storeData("GOOGLE_API_REPLACEMENT_URL", generalFunc.getJsonValue("GOOGLE_API_REPLACEMENT_URL", responseString));
                    storeData(responseString);
                    Utils.setAppLocal(getActContext());
                    closeLoader();
                    if (generalFunc.getJsonValue("SERVER_MAINTENANCE_ENABLE", responseString).equalsIgnoreCase("Yes")) {
                        new ActUtils(getActContext()).startAct(MaintenanceActivity.class);
                        finish();
                        return;
                    }
                    if (Utils.IS_KIOSK_APP) {
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "login");
                        new ActUtils(getActContext()).startActWithData(AppLoignRegisterActivity.class, bundle);
                    }
                    try {
                        ActivityCompat.finishAffinity(LauncherActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!generalFunc.getJsonValue("isAppUpdate", responseString).trim().equals("") && generalFunc.getJsonValue("isAppUpdate", responseString).equals("true")) {
                        showAppUpdateDialog(generalFunc.retrieveLangLBl("New update is available to download. " +
                                        "Downloading the latest update, you will get latest features, improvements and bug fixes.",
                                generalFunc.getJsonValue(Utils.message_str, responseString)));
                    } else {
                        String setMsg = generalFunc.retrieveLangLBl("Please try again.", "LBL_TRY_AGAIN_TXT");
                        if (Utils.checkText(generalFunc.getJsonValue(Utils.message_str, responseString))) {
                            setMsg = generalFunc.getJsonValue(Utils.message_str, responseString);
                        }
                        generateAlert = generalFunc.showGeneralMessage("", setMsg, generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"), generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT"), buttonId -> {
                            if (buttonId == 1) {
                                continueProcess();
                            }
                        });
                    }
                }
            } else {
                showError();
            }
        });

    }

    private void storeData(String responseString) {
        HashMap<String, String> storeData = new HashMap<>();

        ServerTask.MAPS_API_REPLACEMENT_STRATEGY = generalFunc.getJsonValue("MAPS_API_REPLACEMENT_STRATEGY", responseString);
        storeData.put("MAPS_API_REPLACEMENT_STRATEGY", ServerTask.MAPS_API_REPLACEMENT_STRATEGY);

        storeData.put(Utils.FACEBOOK_APPID_KEY, generalFunc.getJsonValue("FACEBOOK_APP_ID", responseString));
        storeData.put(Utils.LINK_FORGET_PASS_KEY, generalFunc.getJsonValue("LINK_FORGET_PASS_PAGE_PASSENGER", responseString));
        storeData.put(Utils.APP_GCM_SENDER_ID_KEY, generalFunc.getJsonValue("GOOGLE_SENDER_ID", responseString));
        storeData.put(Utils.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValue("MOBILE_VERIFICATION_ENABLE", responseString));

        storeData.put(Utils.CURRENCY_LIST_KEY, generalFunc.getJsonValue("LIST_CURRENCY", responseString));
        storeData.put("showCountryList", generalFunc.getJsonValue("showCountryList", responseString));

        storeData.put("CURRENCY_OPTIONAL", generalFunc.getJsonValue("CURRENCY_OPTIONAL", responseString));
        storeData.put("LANGUAGE_OPTIONAL", generalFunc.getJsonValue("LANGUAGE_OPTIONAL", responseString));

        storeData.put(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vGMapLangCode", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE", responseString));
        storeData.put(Utils.SITE_TYPE_KEY, generalFunc.getJsonValue("SITE_TYPE", responseString));


        storeData.put(Utils.languageLabelsKey, generalFunc.getJsonValue("LanguageLabels", responseString));
        storeData.put(Utils.LANGUAGE_LIST_KEY, generalFunc.getJsonValue("LIST_LANGUAGES", responseString));
        storeData.put(Utils.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValue("eType", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
        storeData.put(Utils.LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vCode", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
        storeData.put(Utils.DEFAULT_LANGUAGE_VALUE, generalFunc.getJsonValue("vTitle", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));

        if (!generalFunc.getJsonValue("vDefaultCountry", responseString).equals("")) {
            storeData.put(Utils.DefaultCountry, generalFunc.getJsonValue("vDefaultCountry", responseString));
        }

        if (!generalFunc.getJsonValue("vDefaultCountryCode", responseString).equals("")) {
            storeData.put(Utils.DefaultCountryCode, generalFunc.getJsonValue("vDefaultCountryCode", responseString));
        }
        if (!generalFunc.getJsonValue("vDefaultPhoneCode", responseString).equals("")) {
            storeData.put(Utils.DefaultPhoneCode, generalFunc.getJsonValue("vDefaultPhoneCode", responseString));
        }

        String UPDATE_TO_DEFAULT = generalFunc.getJsonValue("UPDATE_TO_DEFAULT", responseString);
        storeData.put("UPDATE_TO_DEFAULT", UPDATE_TO_DEFAULT);

        if (generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE).equalsIgnoreCase("") || UPDATE_TO_DEFAULT.equalsIgnoreCase("Yes")) {
            storeData.put(Utils.DEFAULT_CURRENCY_VALUE, generalFunc.getJsonValue("vName", generalFunc.getJsonValue("DefaultCurrencyValues", responseString)));
        }

        storeData.put(Utils.FACEBOOK_LOGIN, generalFunc.getJsonValue("FACEBOOK_LOGIN", responseString));
        storeData.put(Utils.GOOGLE_LOGIN, generalFunc.getJsonValue("GOOGLE_LOGIN", responseString));
        storeData.put(Utils.TWITTER_LOGIN, generalFunc.getJsonValue("TWITTER_LOGIN", responseString));

        storeData.put(Utils.DefaultCountry, generalFunc.getJsonValue("vDefaultCountry", responseString));
        storeData.put(Utils.DefaultCountryCode, generalFunc.getJsonValue("vDefaultCountryCode", responseString));
        storeData.put(Utils.DefaultPhoneCode, generalFunc.getJsonValue("vDefaultPhoneCode", responseString));

        storeData.put(Utils.DELIVERALL_KEY, generalFunc.getJsonValue(Utils.DELIVERALL_KEY, responseString));
        storeData.put(Utils.ONLYDELIVERALL_KEY, generalFunc.getJsonValue(Utils.ONLYDELIVERALL_KEY, responseString));

        storeData.put(Utils.GOOGLE_SERVER_KEY, generalFunc.getJsonValue("GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY", responseString));

        storeData.put(Utils.DEVICE_SESSION_ID_KEY, generalFunc.getJsonValue("tDeviceSessionId", responseString));
        storeData.put(Utils.SESSION_ID_KEY, generalFunc.getJsonValue("tSessionId", responseString));

        JSONObject jo = generalFunc.getJsonObject(Utils.message_str, responseString);

        if (!generalFunc.getJsonValue("Visit_Locations", jo).equals("")) {
            storeData.put(Utils.VisitLocationsKey, generalFunc.getJsonValueStr("Visit_Locations", jo));
        }
        storeData.put("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD", generalFunc.getJsonValue("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD", responseString));
        storeData.put("ENABLE_EMAIL_OPTIONAL", generalFunc.getJsonValue("ENABLE_EMAIL_OPTIONAL", responseString));
        storeData.put("ENABLE_CARD_IN_KIOSK", generalFunc.getJsonValue("ENABLE_CARD_IN_KIOSK", responseString));

        generalFunc.storeData(storeData);
    }

    private void autoLogin() {
        autoLoginStartTime = Calendar.getInstance().getTimeInMillis();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", Utils.IS_KIOSK_APP ? generalFunc.getHotelId() : generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        if (!generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY).equalsIgnoreCase("")) {
            parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        }

        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);

        ApiHandler.execute(getActContext(), parameters, false, true, generalFunc, responseString -> {

            closeLoader();

            if (isFinishing()) {
                return;
            }

            if (responseString != null && !responseString.equals("")) {
                if (generalFunc.getJsonValue("changeLangCode", responseString).equalsIgnoreCase("Yes")) {
                    //here to manage code
                    new ConfigureMemberData(responseString, generalFunc, getActContext(), false);
                }

                String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                if (message.equals("SESSION_OUT")) {
                    autoLoginStartTime = 0;
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    ArrayList<String> removeData = new ArrayList<>();
                    removeData.add(Utils.LANGUAGE_CODE_KEY);
                    removeData.add(Utils.DEFAULT_CURRENCY_VALUE);
                    generalFunc.removeValue(removeData);
                    return;
                }

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                    generalFunc.storeData("TSITE_DB", generalFunc.getJsonValue("TSITE_DB", responseString));
                    generalFunc.storeData("GOOGLE_API_REPLACEMENT_URL", generalFunc.getJsonValue("GOOGLE_API_REPLACEMENT_URL", responseString));

                    if (generalFunc.getJsonValue("SERVER_MAINTENANCE_ENABLE", message).equalsIgnoreCase("Yes")) {
                        new ActUtils(getActContext()).startAct(com.zphr.kiosk.MaintenanceActivity.class);
                        finish();
                        return;
                    }
                    HashMap<String, String> storeData = new HashMap<>();
                    if (Utils.IS_KIOSK_APP) {
                        storeData.put(Utils.HOTEL_PROFILE_JSON, message);
                    } else {
                        storeData.put(Utils.USER_PROFILE_JSON, message);
                    }

                    // Store Deliverall & onlyDeliverAll enabled
                    ServerTask.MAPS_API_REPLACEMENT_STRATEGY = generalFunc.getJsonValue("MAPS_API_REPLACEMENT_STRATEGY", message);
                    storeData.put("MAPS_API_REPLACEMENT_STRATEGY", ServerTask.MAPS_API_REPLACEMENT_STRATEGY);
                    storeData.put(Utils.DELIVERALL_KEY, generalFunc.getJsonValue(Utils.DELIVERALL_KEY, message));
                    storeData.put(Utils.ONLYDELIVERALL_KEY, generalFunc.getJsonValue(Utils.ONLYDELIVERALL_KEY, message));

                    storeData.put(Utils.FACEBOOK_APPID_KEY, generalFunc.getJsonValue("FACEBOOK_APP_ID", responseString));
                    storeData.put(Utils.LINK_FORGET_PASS_KEY, generalFunc.getJsonValue("LINK_FORGET_PASS_PAGE_PASSENGER", responseString));
                    storeData.put(Utils.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValue("MOBILE_VERIFICATION_ENABLE", responseString));

                    storeData.put(Utils.ENABLE_TOLL_COST, generalFunc.getJsonValue("ENABLE_TOLL_COST", responseString));
                    storeData.put(Utils.TOLL_COST_APP_ID, generalFunc.getJsonValue("TOLL_COST_APP_ID", responseString));
                    storeData.put(Utils.TOLL_COST_APP_CODE, generalFunc.getJsonValue("TOLL_COST_APP_CODE", responseString));

                    if (!generalFunc.getJsonValue("GOOGLE_SENDER_ID", responseString).equals("")) {
                        storeData.put(Utils.APP_GCM_SENDER_ID_KEY, generalFunc.getJsonValue("GOOGLE_SENDER_ID", responseString));
                    }

                    if (!generalFunc.getJsonValue("LIST_LANGUAGES", responseString).equals("")) {
                        storeData.put(Utils.LANGUAGE_LIST_KEY, generalFunc.getJsonValue("LIST_LANGUAGES", responseString));
                    }
                    if (!generalFunc.getJsonValue("LanguageLabels", responseString).equals("")) {
                        storeData.put(Utils.languageLabelsKey, generalFunc.getJsonValue("LanguageLabels", responseString));
                    }

                    if (!generalFunc.getJsonValue("DefaultLanguageValues", responseString).equals("")) {
                        storeData.put(Utils.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValue("eType", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
                        storeData.put(Utils.LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vCode", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
                        storeData.put(Utils.DEFAULT_LANGUAGE_VALUE, generalFunc.getJsonValue("vTitle", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
                        storeData.put(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vGMapLangCode", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
                    }

                    if (!generalFunc.getJsonValue("LIST_CURRENCY", responseString).equals("")) {
                        storeData.put(Utils.CURRENCY_LIST_KEY, generalFunc.getJsonValue("LIST_CURRENCY", responseString));
                    }

                    storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE", responseString));
                    storeData.put(Utils.SITE_TYPE_KEY, generalFunc.getJsonValue("SITE_TYPE", responseString));

                    if (generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE).equalsIgnoreCase("")) {
                        storeData.put(Utils.DEFAULT_CURRENCY_VALUE, generalFunc.getJsonValue("vName", generalFunc.getJsonValue("DefaultCurrencyValues", responseString)));
                    }

                    storeData.put(Utils.FACEBOOK_LOGIN, generalFunc.getJsonValue("FACEBOOK_LOGIN", responseString));
                    storeData.put(Utils.GOOGLE_LOGIN, generalFunc.getJsonValue("GOOGLE_LOGIN", responseString));
                    storeData.put(Utils.TWITTER_LOGIN, generalFunc.getJsonValue("TWITTER_LOGIN", responseString));

                    storeData.put("CURRENCY_OPTIONAL", generalFunc.getJsonValue("CURRENCY_OPTIONAL", responseString));
                    storeData.put("LANGUAGE_OPTIONAL", generalFunc.getJsonValue("LANGUAGE_OPTIONAL", responseString));

                    if (!generalFunc.getJsonValue("vDefaultCountry", responseString).equals("")) {
                        storeData.put(Utils.DefaultCountry, generalFunc.getJsonValue("vDefaultCountry", responseString));
                    }

                    if (!generalFunc.getJsonValue("vDefaultCountryCode", responseString).equals("")) {
                        storeData.put(Utils.DefaultCountryCode, generalFunc.getJsonValue("vDefaultCountryCode", responseString));

                    }
                    if (!generalFunc.getJsonValue("vDefaultPhoneCode", responseString).equals("")) {
                        storeData.put(Utils.DefaultPhoneCode, generalFunc.getJsonValue("vDefaultPhoneCode", responseString));

                    }
                    //Email Optional
                    storeData.put("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD", generalFunc.getJsonValue("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD", responseString));
                    storeData.put("ENABLE_EMAIL_OPTIONAL", generalFunc.getJsonValue("ENABLE_EMAIL_OPTIONAL", responseString));
                    if (!generalFunc.getJsonValue("GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY", responseString).equals("")) {
                        storeData.put(Utils.GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY, generalFunc.getJsonValue("GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY", responseString));
                    }

                    ServerTask.MAPS_API_REPLACEMENT_STRATEGY = generalFunc.getJsonValue("MAPS_API_REPLACEMENT_STRATEGY", message);
                    storeData.put("MAPS_API_REPLACEMENT_STRATEGY", ServerTask.MAPS_API_REPLACEMENT_STRATEGY);
                    storeData.put("ENABLE_CARD_IN_KIOSK", generalFunc.getJsonValue("ENABLE_CARD_IN_KIOSK", message));

                    if (!generalFunc.getJsonValue("tDeviceSessionId", responseString).equals("")) {
                        storeData.put(Utils.DEVICE_SESSION_ID_KEY, generalFunc.getJsonValue("tDeviceSessionId", responseString));
                    }
                    if (!generalFunc.getJsonValue("tSessionId", responseString).equals("")) {
                        storeData.put(Utils.SESSION_ID_KEY, generalFunc.getJsonValue("tSessionId", responseString));
                    }
                    if (!generalFunc.getJsonValue("Visit_Locations", message).equals("")) {
                        storeData.put(Utils.VisitLocationsKey, generalFunc.getJsonValue("Visit_Locations", message));
                    }
                    generalFunc.storeData(storeData);
                    if (Utils.IS_KIOSK_APP) {
                        Bundle bn = new Bundle();
                        new ActUtils(getActContext()).startActWithData(KioskLandingScreenActivity.class, bn);
                        try {
                            ActivityCompat.finishAffinity(LauncherActivity.this);
                        } catch (Exception ignored) {

                        }
                    } else {
                        if (Calendar.getInstance().getTimeInMillis() - autoLoginStartTime < 2000) {
                            new Handler().postDelayed(() -> {
                                //
                                new OpenMainProfile(getActContext(), generalFunc.getJsonValue(Utils.message_str, responseString), true, generalFunc).startProcess();
                            }, 2000);
                        } else {
                            new OpenMainProfile(getActContext(), generalFunc.getJsonValue(Utils.message_str, responseString), true, generalFunc).startProcess();
                        }
                    }
                } else {
                    autoLoginStartTime = 0;
                    if (!generalFunc.getJsonValue("isAppUpdate", responseString).trim().equals("")
                            && generalFunc.getJsonValue("isAppUpdate", responseString).equals("true")) {
                        showAppUpdateDialog(generalFunc.retrieveLangLBl("New update is available to download. " +
                                        "Downloading the latest update, you will get latest features, improvements and bug fixes.",
                                generalFunc.getJsonValue(Utils.message_str, responseString)));
                    } else {
                        if (generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_PASSENGER") ||
                                generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_ACC_DELETE_TXT")) {
                            GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                            alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                            alertBox.setCancelable(false);
                            alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                            alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
                            alertBox.setBtnClickList(btn_id -> {
                                if (btn_id == 0) {
                                    new ActUtils(getActContext()).startAct(com.zphr.kiosk.ContactUsActivity.class);
                                    alertBox.showAlertBox();
                                } else if (btn_id == 1) {
                                    MyApp.getInstance().logOutFromDevice(true, "");
                                }
                            });
                            alertBox.showAlertBox();
                            return;
                        }
                        showError(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                }
            } else {
                autoLoginStartTime = 0;
                showError();
            }
        });

    }

    private void showLoader() {
        loaderView.setVisibility(View.VISIBLE);
    }

    private void closeLoader() {
        loaderView.setVisibility(View.GONE);
    }

    private void showError() {
        generateAlert.closeAlertBox();
        alertType = "ERROR";
        setDefaultAlertBtn();
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("Please try again.", "LBL_TRY_AGAIN_TXT"));
        generateAlert.showAlertBox();
    }

    private void showError(String contentMsg) {
        generateAlert.closeAlertBox();
        alertType = "ERROR";
        setDefaultAlertBtn();
        generateAlert.setContentMessage("", contentMsg);
        generateAlert.showAlertBox();
    }

    private void showNoInternetDialog() {
        generateAlert.closeAlertBox();
        alertType = "NO_INTERNET";
        setDefaultAlertBtn();
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
        generateAlert.showAlertBox();
    }


    private void showErrorOnPlayServiceDialog(String content) {
        generateAlert.closeAlertBox();
        alertType = "NO_PLAY_SERVICE";
        generateAlert.setContentMessage("", content);
        generateAlert.resetBtn();
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT"));
        generateAlert.showAlertBox();
    }

    private void showAppUpdateDialog(String content) {
        generateAlert.closeAlertBox();
        alertType = "APP_UPDATE";
        generateAlert.setContentMessage(generalFunc.retrieveLangLBl("New update available", "LBL_NEW_UPDATE_AVAIL"), content);
        generateAlert.resetBtn();
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT"));
        generateAlert.showAlertBox();
    }

    private Context getActContext() {
        return LauncherActivity.this;
    }

    @Override
    public void handleBtnClick(int btn_id) {
        Utils.hideKeyboard(getActContext());
        if (btn_id == 0) {
            generateAlert.closeAlertBox();
            if (!alertType.equals("NO_PLAY_SERVICE") && !alertType.equals("APP_UPDATE")) {
                finish();
            } else {
                checkConfigurations(false);
            }
        } else {
            if (alertType.equals("NO_PLAY_SERVICE")) {
                boolean isSuccessfulOpen = new ActUtils(getActContext()).openURL("market://details?id=com.google.android.gms");
                if (!isSuccessfulOpen) {
                    new ActUtils(getActContext()).openURL("http://play.google.com/store/apps/details?id=com.google.android.gms");
                }
                generateAlert.closeAlertBox();
                checkConfigurations(false);
            } else if (alertType.equals("APP_UPDATE")) {
                boolean isSuccessfulOpen = new ActUtils(getActContext()).openURL("market://details?id=" + BuildConfig.APPLICATION_ID);
                if (!isSuccessfulOpen) {
                    new ActUtils(getActContext()).openURL("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                }
                generateAlert.closeAlertBox();
                checkConfigurations(false);
            } else if (!alertType.equals("NO_GPS")) {
                generateAlert.closeAlertBox();
                checkConfigurations(false);
            } else {
                new ActUtils(getActContext()).startActForResult(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Utils.REQUEST_CODE_GPS_ON);
            }
        }
    }

    @Override
    public void onResume() {
        if (generalFunc.prefHasKey(Utils.iServiceId_KEY) && generalFunc != null) {
            generalFunc.removeValue(Utils.iServiceId_KEY);
        }
        if (generalFunc != null && generalFunc.prefHasKey(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY) && generalFunc != null) {
            generalFunc.removeValue(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY);
        }
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        generateAlert.closeAlertBox();
        switch (requestCode) {
            case Utils.REQUEST_CODE_GPS_ON:
                checkConfigurations(false);
                break;
            case GeneralFunctions.MY_SETTINGS_REQUEST:
                generateAlert.closeAlertBox();
                checkConfigurations(false);
                break;
            // SSL Certificate issue
            case ERROR_DIALOG_REQUEST_CODE:
                // Adding a fragment via GooglePlayServicesUtil.showErrorDialogFragment
                // before the instance state is restored throws an error. So instead,
                // set a flag here, which will cause the fragment to delay until
                // onPostResume.
                mRetryProviderInstall = true;
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GeneralFunctions.MY_PERMISSIONS_REQUEST) {
            generateAlert.closeAlertBox();
            checkConfigurations(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        generalFunc.storeData("isInLauncher", "false");

    }

    @Override
    public void onProviderInstalled() {
        checkConfigurations(true);
    }

    @Override
    public void onProviderInstallFailed(int errorCode, Intent intent) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            GooglePlayServicesUtil.showErrorDialogFragment(errorCode, this, ERROR_DIALOG_REQUEST_CODE, dialog -> {
                // The user chose not to take the recovery action
                onProviderInstallerNotAvailable();
            });
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }
    }

    private void onProviderInstallerNotAvailable() {
        checkConfigurations(true);
        showMessageWithAction(loaderView, generalFunc.retrieveLangLBl("provider cannot be updated for some reason.", "LBL_PROVIDER_NOT_AVALIABLE_TXT"));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mRetryProviderInstall) {
            ProviderInstaller.installIfNeededAsync(this, this);
        }
        mRetryProviderInstall = false;
    }

    private void showMessageWithAction(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setDuration(10000);
        snackbar.show();
    }
}