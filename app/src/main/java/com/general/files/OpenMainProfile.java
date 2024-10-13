package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import com.service.server.ServerTask;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 29-06-2016.
 */
public class OpenMainProfile {
    Context mContext;
    String responseString;
    boolean isCloseOnError;
    GeneralFunctions generalFun;
    String tripId = "";
    String eType = "";
    boolean isnotification = false;
    JSONObject userProfileJsonObj;
    private boolean isFromOngoing;
    HashMap<String,String> storeData=new HashMap<>();
    ArrayList<String> removeData=new ArrayList<>();
    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun) {
        this.mContext = mContext;
        //  this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;

        this.responseString = generalFun.retrieveValue(Utils.USER_PROFILE_JSON);

        userProfileJsonObj = generalFun.getJsonObject(this.responseString);

        storeData=new HashMap<>();
        storeData.put(Utils.DefaultCountry, generalFun.getJsonValueStr("vDefaultCountry", userProfileJsonObj));
        storeData.put(Utils.DefaultCountryCode, generalFun.getJsonValueStr("vDefaultCountryCode", userProfileJsonObj));
        storeData.put(Utils.DefaultPhoneCode, generalFun.getJsonValueStr("vDefaultPhoneCode", userProfileJsonObj));
        generalFun.storeData(storeData);
    }

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun, String tripId) {
        this.mContext = mContext;
        this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;
        this.tripId = tripId;

        this.responseString = generalFun.retrieveValue(Utils.USER_PROFILE_JSON);

        userProfileJsonObj = generalFun.getJsonObject(this.responseString);
    }

    public void startProcess() {

        if (generalFun == null)
            return;

        generalFun.sendHeartBeat();

        setGeneralData();

        String vTripStatus = generalFun.getJsonValueStr("vTripStatus", userProfileJsonObj);
        String PaymentStatus_From_Passenger_str = "";
        String Ratings_From_Passenger_str = "";
        String vTripPaymentMode_str = "";
        String eVerified_str = "";
        String eSystem = "";

        JSONObject Last_trip_data = generalFun.getJsonObject("TripDetails", userProfileJsonObj);
        eType = generalFun.getJsonValueStr("eType", Last_trip_data);
        eSystem = generalFun.getJsonValueStr("eSystem", Last_trip_data);

        PaymentStatus_From_Passenger_str = generalFun.getJsonValueStr("PaymentStatus_From_Passenger", userProfileJsonObj);
//        Ratings_From_Passenger_str = generalFun.getJsonValueStr(eSystem.equalsIgnoreCase(Utils.eSystem_Type)?"Ratings_From_DeliverAll":"Ratings_From_Passenger", userProfileJsonObj);
        Ratings_From_Passenger_str = generalFun.getJsonValueStr("Ratings_From_Passenger", userProfileJsonObj);
        eVerified_str = generalFun.getJsonValueStr("eVerified", Last_trip_data);
        vTripPaymentMode_str = generalFun.getJsonValueStr("vTripPaymentMode", Last_trip_data);

        vTripPaymentMode_str = "Cash";// to remove paypal
        PaymentStatus_From_Passenger_str = "Approved"; // to remove paypal
        //  }

        Bundle bn = new Bundle();

            if (Utils.checkText(tripId)) {
                bn.putString("tripId", tripId);
            }


        if (Utils.checkText(tripId) && isFromOngoing) {

        } else {
            try {
                ActivityCompat.finishAffinity((Activity) mContext);
            } catch (Exception e) {
            }
        }

        /*
        try {
            ActivityCompat.finishAffinity((Activity) mContext);
        } catch (Exception e) {
        }*/
    }

    public void setGeneralData() {
        storeData=new HashMap<>();
        ServerTask.MAPS_API_REPLACEMENT_STRATEGY = generalFun.getJsonValueStr("MAPS_API_REPLACEMENT_STRATEGY", userProfileJsonObj);
        storeData.put("MAPS_API_REPLACEMENT_STRATEGY", ServerTask.MAPS_API_REPLACEMENT_STRATEGY);
        storeData.put(Utils.PUBNUB_PUB_KEY, generalFun.getJsonValueStr("PUBNUB_PUBLISH_KEY", userProfileJsonObj));
        storeData.put(Utils.PUBNUB_SUB_KEY, generalFun.getJsonValueStr("PUBNUB_SUBSCRIBE_KEY", userProfileJsonObj));
        storeData.put(Utils.PUBNUB_SEC_KEY, generalFun.getJsonValueStr("PUBNUB_SECRET_KEY", userProfileJsonObj));
        storeData.put(Utils.RIDER_REQUEST_ACCEPT_TIME_KEY, generalFun.getJsonValueStr("RIDER_REQUEST_ACCEPT_TIME", userProfileJsonObj));

        storeData.put(Utils.DEVICE_SESSION_ID_KEY, generalFun.getJsonValueStr("tDeviceSessionId", userProfileJsonObj));
        storeData.put(Utils.SESSION_ID_KEY, generalFun.getJsonValueStr("tSessionId", userProfileJsonObj));

        storeData.put(Utils.SMS_BODY_KEY, generalFun.getJsonValueStr(Utils.SMS_BODY_KEY, userProfileJsonObj));
        storeData.put("DESTINATION_UPDATE_TIME_INTERVAL", generalFun.getJsonValueStr("DESTINATION_UPDATE_TIME_INTERVAL", userProfileJsonObj));
        storeData.put("showCountryList", generalFun.getJsonValueStr("showCountryList", userProfileJsonObj));

        storeData.put(Utils.FETCH_TRIP_STATUS_TIME_INTERVAL_KEY, generalFun.getJsonValueStr("FETCH_TRIP_STATUS_TIME_INTERVAL", userProfileJsonObj));

        storeData.put(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, userProfileJsonObj));
        storeData.put(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, userProfileJsonObj));
        storeData.put(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, userProfileJsonObj));

        storeData.put(Utils.APP_DESTINATION_MODE, generalFun.getJsonValueStr("APP_DESTINATION_MODE", userProfileJsonObj));
        storeData.put(Utils.APP_TYPE, generalFun.getJsonValueStr("APP_TYPE", userProfileJsonObj));
        storeData.put(Utils.SITE_TYPE_KEY, generalFun.getJsonValueStr("SITE_TYPE", userProfileJsonObj));
        storeData.put(Utils.ENABLE_TOLL_COST, generalFun.getJsonValueStr("ENABLE_TOLL_COST", userProfileJsonObj));
        storeData.put(Utils.TOLL_COST_APP_ID, generalFun.getJsonValueStr("TOLL_COST_APP_ID", userProfileJsonObj));
        storeData.put(Utils.TOLL_COST_APP_CODE, generalFun.getJsonValueStr("TOLL_COST_APP_CODE", userProfileJsonObj));
        storeData.put(Utils.HANDICAP_ACCESSIBILITY_OPTION, generalFun.getJsonValueStr("HANDICAP_ACCESSIBILITY_OPTION", userProfileJsonObj));
        storeData.put(Utils.FEMALE_RIDE_REQ_ENABLE, generalFun.getJsonValueStr("FEMALE_RIDE_REQ_ENABLE", userProfileJsonObj));
        storeData.put(Utils.PUBNUB_DISABLED_KEY, generalFun.getJsonValueStr("PUBNUB_DISABLED", userProfileJsonObj));
        storeData.put(Utils.ENABLE_SOCKET_CLUSTER_KEY, generalFun.getJsonValueStr("ENABLE_SOCKET_CLUSTER", userProfileJsonObj));

        storeData.put(Utils.SC_CONNECT_URL_KEY, generalFun.getJsonValueStr("SC_CONNECT_URL", userProfileJsonObj));
        storeData.put(Utils.GOOGLE_SERVER_KEY, generalFun.getJsonValueStr("GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY", userProfileJsonObj));

        storeData.put(Utils.ISWALLETBALNCECHANGE, "No");

        storeData.put(Utils.DELIVERALL_KEY, generalFun.getJsonValueStr(Utils.DELIVERALL_KEY, userProfileJsonObj));
        storeData.put(Utils.ONLYDELIVERALL_KEY, generalFun.getJsonValueStr(Utils.ONLYDELIVERALL_KEY, userProfileJsonObj));
    //  Email Optional
        storeData.put("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD", generalFun.getJsonValue("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD", responseString));
        storeData.put("ENABLE_EMAIL_OPTIONAL", generalFun.getJsonValue("ENABLE_EMAIL_OPTIONAL", responseString));
		
        /*Multi Delivery Enabled*/
        storeData.put(Utils.ENABLE_MULTI_DELIVERY_KEY, generalFun.getJsonValueStr(Utils.ENABLE_MULTI_DELIVERY_KEY,userProfileJsonObj));
        storeData.put(Utils.ALLOW_MULTIPLE_DEST_ADD_KEY, generalFun.getJsonValueStr(Utils.ALLOW_MULTIPLE_DEST_ADD_KEY,userProfileJsonObj));


        removeData=new ArrayList<>();
        removeData.add("userHomeLocationLatitude");
        removeData.add("userHomeLocationLongitude");
        removeData.add("userHomeLocationAddress");
        removeData.add("userWorkLocationLatitude");
        removeData.add("userWorkLocationLongitude");
        removeData.add("userWorkLocationAddress");
        generalFun.removeValue(removeData);

        JSONArray userFavouriteAddressArr = generalFun.getJsonArray("UserFavouriteAddress", responseString);
        if (userFavouriteAddressArr != null && userFavouriteAddressArr.length() > 0) {

            for (int i = 0; i < userFavouriteAddressArr.length(); i++) {
                JSONObject dataItem = generalFun.getJsonObject(userFavouriteAddressArr, i);

                if (generalFun.getJsonValueStr("eType", dataItem).equalsIgnoreCase("HOME")) {

                    storeData.put("userHomeLocationLatitude", generalFun.getJsonValueStr("vLatitude", dataItem));
                    storeData.put("userHomeLocationLongitude", generalFun.getJsonValueStr("vLongitude", dataItem));
                    storeData.put("userHomeLocationAddress", generalFun.getJsonValueStr("vAddress", dataItem));

                } else if (generalFun.getJsonValueStr("eType", dataItem).equalsIgnoreCase("WORK")) {
                    storeData.put("userWorkLocationLatitude", generalFun.getJsonValueStr("vLatitude", dataItem));
                    storeData.put("userWorkLocationLongitude", generalFun.getJsonValueStr("vLongitude", dataItem));
                    storeData.put("userWorkLocationAddress", generalFun.getJsonValueStr("vAddress", dataItem));
                }

            }
        }

        generalFun.storeData(storeData);

    }
}
