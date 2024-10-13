package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.ActivityCompat;

import com.zphr.kiosk.BuildConfig;
import com.zphr.kiosk.KioskLandingScreenActivity;
import com.service.handler.ApiHandler;
import com.service.handler.AppService;
import com.utils.Utils;
import com.view.GenerateAlertBox;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 19-06-2017.
 */

public class GetUserData {

    GeneralFunctions generalFunc;
    Context mContext;
    /*Multi*/
    String tripId = "";
    boolean releaseCurrActInstance = true;

    public GetUserData(GeneralFunctions generalFunc, Context mContext) {
        this.generalFunc = generalFunc;
        this.mContext = mContext;
        this.releaseCurrActInstance = true;
    }

    /*Track Particular Trip Data*/
    public GetUserData(GeneralFunctions generalFunc, Context mContext, String tripID) {
        this.generalFunc = generalFunc;
        this.mContext = mContext;
        this.tripId = tripID;

        if (Utils.checkText(tripID)) {
            this.releaseCurrActInstance = false;
        }

    }


    public void getData() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", Utils.IS_KIOSK_APP ? generalFunc.getHotelId() : generalFunc.getMemberId());
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);
        if (Utils.checkText(tripId)) {
            generalFunc.storeData(Utils.isMultiTrackRunning, "Yes");
            parameters.put("LiveTripId", tripId);
        } else {
            generalFunc.storeData(Utils.isMultiTrackRunning, "No");
        }

        ApiHandler.execute(mContext, parameters, true, true, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {


                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                if (Utils.checkText(responseString) && message.equals("SESSION_OUT")) {
                    AppService.destroy();
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    ArrayList<String> removeData = new ArrayList<>();
                    removeData.add(Utils.LANGUAGE_CODE_KEY);
                    removeData.add(Utils.DEFAULT_CURRENCY_VALUE);
                    generalFunc.removeValue(removeData);
                    return;
                }

                if (isDataAvail == true) {

                    generalFunc.storeData(Utils.IS_KIOSK_APP ? Utils.HOTEL_PROFILE_JSON : Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));

                    if (Utils.IS_KIOSK_APP) {

                        Bundle bn = new Bundle();
                        new ActUtils(mContext).startActWithData(KioskLandingScreenActivity.class, bn);
                    } else {
                        new OpenMainProfile(mContext,
                                generalFunc.getJsonValue(Utils.message_str, responseString), true, generalFunc, tripId).startProcess();
                    }

                    if (releaseCurrActInstance) {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            try {
                                ActivityCompat.finishAffinity((Activity) mContext);

                            } catch (Exception e) {
                            }
                            Utils.runGC();
                        }, 300);

                    }


                } else {
                    if (!generalFunc.getJsonValue("isAppUpdate", responseString).trim().equals("")
                            && generalFunc.getJsonValue("isAppUpdate", responseString).equals("true")) {

                    } else {

                        if (generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_COMPANY") ||
                                generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_ACC_DELETE_TXT") ||
                                generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_DRIVER")) {

                            GenerateAlertBox alertBox = generalFunc.notifyRestartApp("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                            alertBox.setCancelable(false);
                            alertBox.setBtnClickList(btn_id -> {

                                if (btn_id == 1) {
//                                            generalFunc.logoutFromDevice(mContext,"GetUserData",generalFunc);
                                    MyApp.getInstance().logOutFromDevice(true, "");
                                }
                            });
                            return;
                        }
                    }

                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TRY_AGAIN_TXT"), "", generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"), buttonId -> generalFunc.restartApp());
                }
            } else {

                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TRY_AGAIN_TXT"), "", generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"), buttonId -> generalFunc.restartApp());
            }
        });

    }

    public void getLatestDataOnly() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", Utils.IS_KIOSK_APP ? generalFunc.getHotelId() : generalFunc.getMemberId());
        //parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);
        if (Utils.checkText(tripId)) {
            generalFunc.storeData(Utils.isMultiTrackRunning, "Yes");
            parameters.put("LiveTripId", tripId);
        } else {
            generalFunc.storeData(Utils.isMultiTrackRunning, "No");
        }
        if (!generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY).equalsIgnoreCase("")) {
            parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        }
        ApiHandler.execute(mContext, parameters, true, true, generalFunc, responseString -> {
            if (responseString != null && !responseString.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);
                if (isDataAvail == true) {
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                }
            }
        });
    }
}
