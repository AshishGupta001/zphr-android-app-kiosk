package com.general.files;

import android.app.Activity;
import android.content.Context;

import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Admin on 20/03/18.
 */

public class FireTripStatusMsg {

    private final String TAGS = FireTripStatusMsg.class.getSimpleName();
    private Context mContext;
    private static String tmp_msg_chk = "";

    public FireTripStatusMsg(Context mContext) {
        this.mContext = mContext;
    }

    public void fireTripMsg(String message) {

        com.utils.Logger.d(TAGS, "MsgReceived :: called");
        if (!Utils.checkText(message) || tmp_msg_chk.equals(message)) {
            Logger.d(TAGS, "MsgReceived :: return");
            return;
        }
        tmp_msg_chk = message;

        Logger.e(TAGS, "MsgReceived::" + message);
        String finalMsg = message;

        if (!GeneralFunctions.isJsonObj(finalMsg)) {
            try {
                finalMsg = new JSONTokener(message).nextValue().toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!GeneralFunctions.isJsonObj(finalMsg)) {
                finalMsg = finalMsg.replaceAll("^\"|\"$", "");
                if (!GeneralFunctions.isJsonObj(finalMsg)) {
                    finalMsg = message.replaceAll("\\\\", "");

                    finalMsg = finalMsg.replaceAll("^\"|\"$", "");

                    if (!GeneralFunctions.isJsonObj(finalMsg)) {
                        finalMsg = message.replace("\\\"", "\"").replaceAll("^\"|\"$", "");
                    }
                    finalMsg = finalMsg.replace("\\\\\"", "\\\"");
                }
            }
        }

        if (MyApp.getInstance() == null) {
            if (mContext != null) {
                dispatchNotification(finalMsg);
            }
            return;
        }

        if (MyApp.getInstance().getCurrentAct() != null) {
            mContext = MyApp.getInstance().getCurrentAct();
        }

        if (mContext == null) {
            dispatchNotification(finalMsg);
            return;
        }

        GeneralFunctions generalFunc = new GeneralFunctions(mContext);
        JSONObject obj_msg = generalFunc.getJsonObject(finalMsg);

        if (!GeneralFunctions.isJsonObj(finalMsg)) {
            LocalNotification.dispatchLocalNotification(mContext, message, true);
            generalFunc.showGeneralMessage("", message);
            return;
        }

        boolean isMsgExist = isTripStatusMsgExist(finalMsg, mContext, generalFunc);
        Logger.d(TAGS, "MsgReceived:: MsgExist-> " + isMsgExist);

        if (isMsgExist) {
            return;
        }

        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(() -> continueDispatchMsg(generalFunc, obj_msg));
        } else {
            dispatchNotification(finalMsg);
        }

    }

    private void continueDispatchMsg(GeneralFunctions generalFunc, JSONObject obj_msg) {

        String messageStr = generalFunc.getJsonValueStr("Message", obj_msg);

        String vTitle = generalFunc.getJsonValueStr("vTitle", obj_msg);
        String eType = generalFunc.getJsonValueStr("eType", obj_msg);

        if (messageStr.equals("")) {

            String msgTypeStr = generalFunc.getJsonValueStr("MsgType", obj_msg);
            //   String messageType_str = generalFunc.getJsonValueStr("MessageType", obj_msg);

            if (msgTypeStr.equalsIgnoreCase("CHAT") && !Utils.IS_KIOSK_APP) {
                LocalNotification.dispatchLocalNotification(mContext, generalFunc.getJsonValueStr("Msg", obj_msg), true);


            } else if (msgTypeStr.equalsIgnoreCase("Notification")) {
                LocalNotification.dispatchLocalNotification(mContext, vTitle, true);

                final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                generateAlert.setCancelable(false);
//                    generateAlert.setSystemAlertWindow(true);
                generateAlert.setBtnClickList(btn_id -> doOperations());
                generateAlert.setContentMessage("", vTitle);
                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                generateAlert.showAlertBox();
            }
        } else {
            if ((messageStr.equalsIgnoreCase("TripCancelledByDriver") || messageStr.equalsIgnoreCase("DestinationAdded") || messageStr.equalsIgnoreCase("TripEnd")) && !Utils.IS_KIOSK_APP) {

                if (messageStr.equalsIgnoreCase("TripEnd") || messageStr.equalsIgnoreCase("TripCancelledByDriver")) {
                    generalFunc.storeData(Utils.ISWALLETBALNCECHANGE, "Yes");
                }
                if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {

                    String iDriverId = generalFunc.getJsonValueStr("iDriverId", obj_msg);
                    String iTripId = generalFunc.getJsonValueStr("iTripId", obj_msg);
                    String showTripFare = generalFunc.getJsonValueStr("ShowTripFare", obj_msg);

                    if (messageStr.equalsIgnoreCase("TripEnd") || showTripFare.equalsIgnoreCase("true")) {
                        showPubnubGeneralMessage(iTripId, vTitle, false, true, generalFunc);
                    } else {
                        generalFunc.showGeneralMessage("", vTitle);
                    }

                } else if (generalFunc.getJsonValueStr("eSystem", obj_msg).equalsIgnoreCase(Utils.eSystem_Type)) {
                    if (messageStr.equalsIgnoreCase("OrderConfirmByRestaurant") || messageStr.equalsIgnoreCase("OrderDeclineByRestaurant") || messageStr.equalsIgnoreCase("OrderPickedup") ||
                            messageStr.equalsIgnoreCase("OrderDelivered") || messageStr.equalsIgnoreCase("OrderCancelByAdmin")) {

                        if (messageStr.equalsIgnoreCase("OrderCancelByAdmin")) {
                            final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                            generateAlert.setCancelable(false);
                            generateAlert.setBtnClickList(btn_id -> MyApp.getInstance().restartWithGetDataApp());
                            generateAlert.setContentMessage("", vTitle);
                            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                            generateAlert.showAlertBox();
                        } else {
                            generalFunc.showGeneralMessage("", vTitle);
                        }
                    }
                } else {
                    final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                    generateAlert.setCancelable(false);
//                    generateAlert.setSystemAlertWindow(true);
                    generateAlert.setBtnClickList(btn_id -> MyApp.getInstance().restartWithGetDataApp());
                    generateAlert.setContentMessage("", vTitle);
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                    generateAlert.showAlertBox();
                }
                return;
            } else if (!Utils.IS_KIOSK_APP && (messageStr.equalsIgnoreCase("TripStarted") || messageStr.equalsIgnoreCase("DriverArrived"))) {
                generalFunc.showGeneralMessage("", vTitle);
            } else {
                //  String vTitle = generalFunc.getJsonValueStr("vTitle", obj_msg);
                if (messageStr.equalsIgnoreCase("OrderConfirmByRestaurant") || messageStr.equalsIgnoreCase("OrderDeclineByRestaurant") || messageStr.equalsIgnoreCase("OrderPickedup") ||
                        messageStr.equalsIgnoreCase("OrderDelivered") || messageStr.equalsIgnoreCase("OrderCancelByAdmin")) {
                    generalFunc.showGeneralMessage("", vTitle);
                }
            }
        }

        if (MyApp.getInstance().kioskBookNowActivity != null && (!eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) || (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) && messageStr.equalsIgnoreCase("CabRequestAccepted")))) {
            MyApp.getInstance().kioskBookNowActivity.pubNubMsgArrived(obj_msg.toString());
        }
    }

    private void doOperations() {
//        MyApp.getInstance().restartWithGetDataApp()
    }

    private void dispatchNotification(String message) {
        Context mLocContext = this.mContext;

        if (mLocContext == null && MyApp.getInstance() != null && MyApp.getInstance().getCurrentAct() == null) {
            mLocContext = MyApp.getInstance().getApplicationContext();
        }

        if (mLocContext != null) {
            GeneralFunctions generalFunc = new GeneralFunctions(mLocContext);

            if (!GeneralFunctions.isJsonObj(message)) {
                LocalNotification.dispatchLocalNotification(mLocContext, message, true);
                return;
            }

            JSONObject obj_msg = generalFunc.getJsonObject(message);

            String message_str = generalFunc.getJsonValueStr("Message", obj_msg);

            if (message_str.equals("")) {
                String msgType_str = generalFunc.getJsonValueStr("MsgType", obj_msg);

                switch (msgType_str) {
                    case "CHAT":
                        if (!Utils.IS_KIOSK_APP)
                            generalFunc.storeData("OPEN_CHAT", obj_msg.toString());
                        LocalNotification.dispatchLocalNotification(mLocContext, generalFunc.getJsonValueStr("Msg", obj_msg), false);
                        break;
                    case "Notification":
                        LocalNotification.dispatchLocalNotification(mLocContext, generalFunc.getJsonValueStr("vTitle", obj_msg), false);
                        break;
                }

            } else {
                String title_msg = generalFunc.getJsonValueStr("vTitle", obj_msg);
                switch (message) {

                    case "TripCancelledByDriver":
                    case "DriverArrived":
                    case "DestinationAdded":
                    case "TripStarted":
                    case "TripEnd":
                        if (!Utils.IS_KIOSK_APP)
                            LocalNotification.dispatchLocalNotification(mLocContext, title_msg, false);
                        break;
                    case "OrderDelivered":
                    case "OrderPickedup":
                    case "OrderConfirmByRestaurant":
                    case "OrderDeclineByRestaurant":
                    case "OrderCancelByAdmin":
                        LocalNotification.dispatchLocalNotification(MyApp.getInstance().getApplicationContext(), title_msg, false);
                        break;
                }
            }
        }
    }

    public void showPubnubGeneralMessage(final String iTripId, final String message, final boolean isrestart, final boolean isufxrate, GeneralFunctions generalFunc) {
        try {
            if (message != null && message.equals("SESSION_OUT")) {
                MyApp.getInstance().notifySessionTimeOut();
                Utils.runGC();
                return;
            }
            final GenerateAlertBox generateAlert = new GenerateAlertBox(MyApp.getInstance().getCurrentAct());
            generateAlert.setContentMessage("", message);
            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
            generateAlert.setBtnClickList(btn_id -> {
                generateAlert.closeAlertBox();

                if (isrestart) {
                    MyApp.getInstance().restartWithGetDataApp();
                }
            });
            generateAlert.showAlertBox();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTripStatusMsgExist(String msg, Context mContext, GeneralFunctions generalFun) {

        JSONObject obj_tmp = generalFun.getJsonObject(msg);

        if (obj_tmp != null) {

            String message = generalFun.getJsonValueStr("Message", obj_tmp);

            if (!message.equals("")) {
                String iTripId = "";
                if (generalFun.getJsonValue("eSystem", msg).equalsIgnoreCase(Utils.eSystem_Type)) {
                    iTripId = generalFun.getJsonValueStr("iOrderId", obj_tmp);
                } else {
                    iTripId = generalFun.getJsonValueStr("iTripId", obj_tmp);
                }

                //  String iTripDeliveryLocationId = generalFun.getJsonValueStr("iTripDeliveryLocationId", obj_tmp);
                if (!iTripId.equals("")) {
                    String vTitle = generalFun.getJsonValueStr("vTitle", obj_tmp);
                    String time = generalFun.getJsonValueStr("time", obj_tmp);
                    String key = Utils.TRIP_REQ_CODE_PREFIX_KEY + iTripId + "_" + message;

                    if (message.equals("DestinationAdded")) {

                        long newMsgTime = GeneralFunctions.parseLongValue(0, time);

                        String destKeyValueStr = GeneralFunctions.retrieveValue(key, mContext);
                        if (!destKeyValueStr.equals("")) {

                            long destKeyValue = GeneralFunctions.parseLongValue(0, destKeyValueStr);

                            if (newMsgTime > destKeyValue) {
                                generalFun.removeValue(key);
                            } else {
                                return true;
                            }
                        }
                    }

                    String data = generalFun.retrieveValue(key);

                    if (data.equals("")) {
                        if (message.equalsIgnoreCase("CabRequestAccepted") && Utils.IS_KIOSK_APP) {
                            LocalNotification.dispatchLocalNotification(mContext, vTitle, true);
                        } else if (!message.equalsIgnoreCase("TripRequestCancel") && !Utils.IS_KIOSK_APP) {

                            LocalNotification.dispatchLocalNotification(mContext, vTitle, true);
                        }
                        if (time.equals("")) {
                            generalFun.storeData(key, "" + System.currentTimeMillis());
                        } else {
                            generalFun.storeData(key, "" + time);
                        }
                        return false;
                    } else {
                        return true;
                    }
                }
            } else {
                String msgType = generalFun.getJsonValueStr("MsgType", obj_tmp);
                if (msgType != null) {
                    String key, data, tRandomValue = "";
                    switch (msgType) {
                        case "TripRequestCancel":
                            tRandomValue = generalFun.getJsonValueStr("iTripId", obj_tmp);
                            break;
                    }
                    if (Utils.checkText(tRandomValue)) {
                        key = Utils.TRIP_REQ_CODE_PREFIX_KEY + tRandomValue + "_" + msgType;
                        data = generalFun.retrieveValue(key);
                        generalFun.storeData(key, "" + System.currentTimeMillis());
                        return !data.equals("");
                    }
                }
            }
        }
        return false;
    }
}