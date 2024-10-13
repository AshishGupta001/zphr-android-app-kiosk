package com.general.files;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

import com.zphr.kiosk.KioskBookNowActivity;
import com.zphr.kiosk.KioskCabSelectionActivity;
import com.zphr.kiosk.KioskLandingScreenActivity;
import com.zphr.kiosk.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.service.handler.ApiHandler;
import com.service.handler.AppService;
import com.service.model.EventInformation;
import com.service.server.AppClient;
import com.service.server.ServerTask;
import com.utils.Utils;
import com.view.SelectableRoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Admin on 05-07-2016.
 */
public class LoadAvailableCab implements RecurringTask.OnTaskRunCalled {
    public ArrayList<HashMap<String, String>> listOfDrivers;
    public String pickUpAddress = "";
    public String currentGeoCodeResult = "";
    public String sortby = "";
    public boolean isAvailableCab = false;
    public String selectProviderId = "";
    public ArrayList<Marker> driverMarkerList;
    Context mContext;
    GeneralFunctions generalFunc;
    String selectedCabTypeId = "";
    //    Location pickUpLocation;
    GoogleMap gMapView;
    View parentView;
    ServerTask currentWebTask;
    KioskCabSelectionActivity mainAct;
    KioskBookNowActivity kioskBookNowAct;
    KioskLandingScreenActivity landingScreenAct;
    String userProfileJson;
    int RESTRICTION_KM_NEAREST_TAXI = 4;
    int ONLINE_DRIVER_LIST_UPDATE_TIME_INTERVAL = 1 * 60 * 1000;
    int DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = 3;
    RecurringTask updateDriverListTask;
    Dialog dialog = null;
    boolean isTaskKilled = false;
    boolean isSessionOut = false;


    public LoadAvailableCab(Context mContext, GeneralFunctions generalFunc, String selectedCabTypeId, Location pickUpLocation, GoogleMap gMapView, String userProfileJson) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;
        this.selectedCabTypeId = selectedCabTypeId;
        this.gMapView = gMapView;
        this.userProfileJson = userProfileJson;
        /*if (mContext instanceof MainActivity) {
            mainAct = (MainActivity) mContext;
            parentView = generalFunc.getCurrentView(mainAct);
        }*/

        if (mContext instanceof KioskCabSelectionActivity) {
            mainAct = (KioskCabSelectionActivity) mContext;
            parentView = generalFunc.getCurrentView(mainAct);
        }

        if (mContext instanceof KioskBookNowActivity) {
            kioskBookNowAct = (KioskBookNowActivity) mContext;
            parentView = generalFunc.getCurrentView(kioskBookNowAct);
        }

        if (mContext instanceof KioskLandingScreenActivity) {
            landingScreenAct = (KioskLandingScreenActivity) mContext;
            parentView = generalFunc.getCurrentView(landingScreenAct);
        }

        listOfDrivers = new ArrayList<>();
        driverMarkerList = new ArrayList<>();

        RESTRICTION_KM_NEAREST_TAXI = generalFunc.parseIntegerValue(4, generalFunc.getJsonValue("RESTRICTION_KM_NEAREST_TAXI", userProfileJson));
        ONLINE_DRIVER_LIST_UPDATE_TIME_INTERVAL = (generalFunc.parseIntegerValue(1, generalFunc.getJsonValue("ONLINE_DRIVER_LIST_UPDATE_TIME_INTERVAL", userProfileJson))) * 60 * 1000;
        DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = generalFunc.parseIntegerValue(3, generalFunc.getJsonValue("DRIVER_ARRIVED_MIN_TIME_PER_MINUTE", userProfileJson));

    }

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public void setPickUpLocation(Location pickUpLocation) {
//        this.pickUpLocation = pickUpLocation;
    }

    public void setCabTypeId(String selectedCabTypeId) {
        this.selectedCabTypeId = selectedCabTypeId;
    }

    public void changeCabs() {

        if (driverMarkerList.size() > 0) {
            filterDrivers(true);
        } else {
            checkAvailableCabs();
        }
    }

    public void checkAvailableCabs() {

//        if (ConfigPubNub.getInstance().isSessionout) {
//            return;
//        }

        if (mainAct != null && mainAct.pickUpLocation == null) {
            return;
        } else if (landingScreenAct != null && landingScreenAct.pickUpLocation == null) {
            return;
        } else if (kioskBookNowAct != null && kioskBookNowAct.pickUpLocation == null) {
            return;
        }

        if (gMapView == null && mainAct != null && !mainAct.isCabSelectionPhase) {
            return;
        } else if (gMapView == null && kioskBookNowAct != null) {
            return;
        }

        if (updateDriverListTask == null) {
            updateDriverListTask = new RecurringTask(ONLINE_DRIVER_LIST_UPDATE_TIME_INTERVAL);
            onResumeCalled();
            updateDriverListTask.setTaskRunListener(this);
        }

        if (currentWebTask != null) {
            currentWebTask.cancel(true);
            currentWebTask = null;
        }

        if (mainAct != null) {
            mainAct.notifyCarSearching();
        } else if (landingScreenAct != null) {
//            Utils.printLog("kiosk", "lowestTime 4");
            landingScreenAct.setETA("--", "");
        } else if (kioskBookNowAct != null) {
//            Utils.printLog("kiosk", "book now lowestTime 4");
            kioskBookNowAct.notifyCarSearching();
        }

        if (listOfDrivers != null) {
            if (listOfDrivers.size() > 0) {
                listOfDrivers.clear();
            }
        }
        if (mainAct != null && mainAct.cabSelectionFrag != null) {
            mainAct.cabSelectionFrag.showLoader();
        }
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadAvailableCab");

        if (mainAct != null) {
            parameters.put("PassengerLat", "" + mainAct.pickUpLocation.getLatitude());
            parameters.put("PassengerLon", "" + mainAct.pickUpLocation.getLongitude());
            parameters.put("SelectedCabType", mainAct.getSelectedCabType());

        } else if (landingScreenAct != null) {
            parameters.put("PassengerLat", "" + landingScreenAct.pickUpLocation.getLatitude());
            parameters.put("PassengerLon", "" + landingScreenAct.pickUpLocation.getLongitude());
            parameters.put("scheduleDate", "");
            parameters.put("SelectedCabType", landingScreenAct.selectedCabType);

        } else if (kioskBookNowAct != null) {
            parameters.put("PassengerLat", "" + kioskBookNowAct.pickUpLocation.getLatitude());
            parameters.put("PassengerLon", "" + kioskBookNowAct.pickUpLocation.getLongitude());
            parameters.put("scheduleDate", "");
            parameters.put("SelectedCabType", kioskBookNowAct.getSelectedCabType());

        }

        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("PickUpAddress", pickUpAddress);

//        parameters.put("SelectedCabType", mainAct.getSelectedCabTypeId());

        parameters.put("sortby", sortby);

        if (mainAct != null && !mainAct.eShowOnlyMoto.equalsIgnoreCase("")) {
            parameters.put("eShowOnlyMoto", mainAct.eShowOnlyMoto);
        }


        if (mainAct != null) {
            parameters.put("eType", mainAct.getCurrentCabGeneralType());
        } else if (landingScreenAct != null) {
            parameters.put("eType", landingScreenAct.selectedCabType);
        } else if (kioskBookNowAct != null) {
            parameters.put("eType", kioskBookNowAct.getCurrentCabGeneralType());
        }


        if (mainAct != null) {
            if (mainAct.iscubejekRental) {
                parameters.put("eRental", "Yes");

            }
            if (mainAct.cabSelectionFrag != null) {
            } else {

                parameters.put("iVehicleTypeId", mainAct.getSelectedCabTypeId());


            }
        }


        this.currentWebTask = ApiHandler.execute(mContext, parameters, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                if (Utils.checkText(responseString) && generalFunc.getJsonValue(Utils.message_str, responseString).equals("SESSION_OUT")) {
                    isSessionOut = true;
                    if (currentWebTask != null) {
                        currentWebTask.cancel(true);
                        currentWebTask = null;
                    }
                    setTaskKilledValue(true);
                    updateDriverListTask.setTaskRunListener(null);
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    return;
                }

                JSONArray vehicleTypesArr = generalFunc.getJsonArray("VehicleTypes", responseString);
                ArrayList<HashMap<String, String>> tempCabTypesArrList = new ArrayList<>();

                if (vehicleTypesArr != null) {
                    for (int i = 0; i < vehicleTypesArr.length(); i++) {
                        JSONObject tempObj = generalFunc.getJsonObject(vehicleTypesArr, i);
                        String type = Utils.CabGeneralType_Ride;
                        if (mainAct != null) {
                            type = mainAct.getCurrentCabGeneralType();
                        } else if (kioskBookNowAct != null) {
                            type = kioskBookNowAct.getCurrentCabGeneralType();
                        } else if (landingScreenAct != null) {
                            type = landingScreenAct.selectedCabType;
                        }

                        if (type.equalsIgnoreCase("rental")) {
                            type = Utils.CabGeneralType_Ride;
                        }

                        if (generalFunc.getJsonValue("eType", tempObj.toString()).equals(type)) {

                            Gson gson = AppClient.getGSONBuilder();
                            HashMap<String, String> dataMap = gson.fromJson(
                                    tempObj.toString(), new TypeToken<HashMap<String, Object>>() {
                                    }.getType()
                            );
                            tempCabTypesArrList.add(dataMap);
                        }
                    }
                }

                if (mainAct != null) {
                    boolean isCarTypeChanged = isCarTypesArrChanged(tempCabTypesArrList);

                    if (isCarTypeChanged) {

                        mainAct.cabTypesArrList.clear();
                        mainAct.cabTypesArrList.addAll(tempCabTypesArrList);
                        mainAct.selectedCabTypeId = getFirstCarTypeID();
                        selectedCabTypeId = getFirstCarTypeID();

                        if (mainAct.cabSelectionFrag != null) {
                            mainAct.cabSelectionFrag.generateCarType();
                        }
                    }

                    if (mainAct.cabTypesArrList.size() > 0) {
                        mainAct.cabTypesArrList.clear();
                        mainAct.cabTypesArrList.addAll(tempCabTypesArrList);
                    }

                    if (mainAct.cabTypesArrList.size() == 0) {
                        mainAct.cabTypesArrList.addAll(tempCabTypesArrList);
                    }

                    if (mainAct.cabSelectionFrag != null && mainAct.cabTypesArrList != null && mainAct.cabTypesArrList.size() > 0) {
                        mainAct.cabSelectionFrag.closeLoadernTxt();
                    } else {
                        if (mainAct.cabSelectionFrag != null && mainAct.cabTypesArrList != null) {
                            mainAct.cabSelectionFrag.closeLoader();
                        }
                    }
                }

                JSONArray availCabArr = generalFunc.getJsonArray("AvailableCabList", responseString);

                if (availCabArr != null) {
                    for (int i = 0; i < availCabArr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(availCabArr, i);

                        JSONObject carDetailsJson = generalFunc.getJsonObject("DriverCarDetails", obj_temp);
                        HashMap<String, String> driverDataMap = new HashMap<String, String>();
                        driverDataMap.put("driver_id", generalFunc.getJsonValueStr("iDriverId", obj_temp));
                        driverDataMap.put("Name", generalFunc.getJsonValueStr("vName", obj_temp));
                        driverDataMap.put("eIsFeatured", generalFunc.getJsonValueStr("eIsFeatured", obj_temp));
                        driverDataMap.put("LastName", generalFunc.getJsonValueStr("vLastName", obj_temp));
                        driverDataMap.put("Latitude", generalFunc.getJsonValueStr("vLatitude", obj_temp));
                        driverDataMap.put("Longitude", generalFunc.getJsonValueStr("vLongitude", obj_temp));
                        driverDataMap.put("GCMID", generalFunc.getJsonValueStr("iGcmRegId", obj_temp));
                        driverDataMap.put("iAppVersion", generalFunc.getJsonValueStr("iAppVersion", obj_temp));
                        driverDataMap.put("driver_img", generalFunc.getJsonValueStr("vImage", obj_temp));
                        driverDataMap.put("average_rating", generalFunc.getJsonValueStr("vAvgRating", obj_temp));
                        driverDataMap.put("DIST_TO_PICKUP_INT", generalFunc.getJsonValueStr("distance", obj_temp));
                        driverDataMap.put("vPhone_driver", generalFunc.getJsonValueStr("vPhone", obj_temp));
                        driverDataMap.put("vPhoneCode_driver", generalFunc.getJsonValueStr("vCode", obj_temp));
                        driverDataMap.put("tProfileDescription", generalFunc.getJsonValueStr("tProfileDescription", obj_temp));
                        driverDataMap.put("ACCEPT_CASH_TRIPS", generalFunc.getJsonValueStr("ACCEPT_CASH_TRIPS", obj_temp));
                        driverDataMap.put("vWorkLocationRadius", generalFunc.getJsonValueStr("vWorkLocationRadius", obj_temp));
                        driverDataMap.put("PROVIDER_RADIUS", generalFunc.getJsonValueStr("vWorkLocationRadius", obj_temp));


                        driverDataMap.put("DriverGender", generalFunc.getJsonValueStr("eGender", obj_temp));
                        driverDataMap.put("eFemaleOnlyReqAccept", generalFunc.getJsonValueStr("eFemaleOnlyReqAccept", obj_temp));


                        driverDataMap.put("eHandiCapAccessibility", generalFunc.getJsonValueStr("eHandiCapAccessibility", carDetailsJson));
                        driverDataMap.put("vCarType", generalFunc.getJsonValueStr("vCarType", carDetailsJson));
                        driverDataMap.put("vColour", generalFunc.getJsonValueStr("vColour", carDetailsJson));
                        driverDataMap.put("vLicencePlate", generalFunc.getJsonValueStr("vLicencePlate", carDetailsJson));
                        driverDataMap.put("make_title", generalFunc.getJsonValueStr("make_title", carDetailsJson));
                        driverDataMap.put("model_title", generalFunc.getJsonValueStr("model_title", carDetailsJson));
                        driverDataMap.put("fAmount", generalFunc.getJsonValueStr("fAmount", carDetailsJson));
                        driverDataMap.put("eRental", generalFunc.getJsonValueStr("vRentalCarType", carDetailsJson));


                        driverDataMap.put("vCurrencySymbol", generalFunc.getJsonValueStr("vCurrencySymbol", carDetailsJson));

                        driverDataMap.put("PROVIDER_RATING_COUNT", generalFunc.getJsonValue("PROVIDER_RATING_COUNT", obj_temp.toString()));
                        listOfDrivers.add(driverDataMap);
                    }

                }


                if (availCabArr == null || availCabArr.length() == 0) {
                    removeDriversFromMap(true);
                    if (mainAct != null) {
                        mainAct.notifyNoCabs();
                    } else if (kioskBookNowAct != null) {
//                        Utils.printLog("kiosk", "lowestTime 5");
                        kioskBookNowAct.notifyNoCabs();
                    } else if (landingScreenAct != null) {
//                        Utils.printLog("kiosk", "lowestTime 5");
                        landingScreenAct.setETA("--", "");
                    }
                } else {
                    filterDrivers(false);
                }


            } else {
                removeDriversFromMap(true);
                if (parentView != null) {
                    generalFunc.showMessage(parentView, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }

                if (mainAct != null) {
                    mainAct.notifyNoCabs();
                } else if (kioskBookNowAct != null) {
//                    Utils.printLog("kiosk", "lowestTime 6");
                    kioskBookNowAct.notifyNoCabs();
                } else if (landingScreenAct != null) {
//                    Utils.printLog("kiosk", "lowestTime 6");
                    landingScreenAct.setETA("--", "");
                }
            }

        });

    }

    public boolean isCarTypesArrChanged(ArrayList<HashMap<String, String>> carTypeList) {
        if (mainAct != null) {
            if (mainAct.cabTypesArrList.size() != carTypeList.size()) {
                return true;
            }

            for (int i = 0; i < carTypeList.size(); i++) {
                String iVehicleTypeId = mainAct.cabTypesArrList.get(i).get("iVehicleTypeId");
                String newVehicleTypeId = carTypeList.get(i).get("iVehicleTypeId");

                if (!iVehicleTypeId.equals(newVehicleTypeId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getFirstCarTypeID() {

        if (mainAct != null) {

            for (int i = 0; i < mainAct.cabTypesArrList.size(); i++) {
                String iVehicleTypeId = mainAct.cabTypesArrList.get(i).get("iVehicleTypeId");

                return iVehicleTypeId;
            }
        }

        return "";
    }

    public void setTaskKilledValue(boolean isTaskKilled) {
        this.isTaskKilled = isTaskKilled;

        if (isTaskKilled == true) {
            onPauseCalled();
        }
    }

    public void removeDriversFromMap(boolean isUnSubscribeAll) {
        if (driverMarkerList.size() > 0) {
            ArrayList<Marker> tempDriverMarkerList = new ArrayList<>();
            tempDriverMarkerList.addAll(driverMarkerList);
            for (int i = 0; i < tempDriverMarkerList.size(); i++) {
                Marker marker_temp = driverMarkerList.get(0);
                marker_temp.remove();
                driverMarkerList.remove(0);

            }
        }

        if (mainAct != null && isUnSubscribeAll == true) {

            AppService.getInstance().executeService(new EventInformation.EventInformationBuilder().setChanelList(mainAct.getDriverLocationChannelList()).build(), AppService.Event.UNSUBSCRIBE);
        } else if (landingScreenAct != null && isUnSubscribeAll == true) {


            AppService.getInstance().executeService(new EventInformation.EventInformationBuilder().setChanelList(landingScreenAct.getDriverLocationChannelList()).build(), AppService.Event.UNSUBSCRIBE);
        } else if (kioskBookNowAct != null && isUnSubscribeAll == true) {
            AppService.getInstance().executeService(new EventInformation.EventInformationBuilder().setChanelList(kioskBookNowAct.getDriverLocationChannelList()).build(), AppService.Event.UNSUBSCRIBE);
        }
    }


    public ArrayList<Marker> getDriverMarkerList() {
        return this.driverMarkerList;
    }

    public void filterDrivers(boolean isCheckAgain) {

        if (mainAct != null && mainAct.pickUpLocation == null) {
            generalFunc.restartApp();
            return;
        } else if (landingScreenAct != null && landingScreenAct.pickUpLocation == null) {
            generalFunc.restartApp();
            return;
        } else if (kioskBookNowAct != null && kioskBookNowAct.pickUpLocation == null) {
            generalFunc.restartApp();
            return;
        }

        if (gMapView == null && mainAct != null && !mainAct.isCabSelectionPhase) {
            return;
        }

        double lowestKM = 0.0;
        boolean isFirst_lowestKM = true;

        ArrayList<HashMap<String, String>> currentLoadedDrivers = new ArrayList<>();

        ArrayList<Marker> driverMarkerList_temp = new ArrayList<>();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < listOfDrivers.size(); i++) {
            HashMap<String, String> driverData = listOfDrivers.get(i);

            String driverName = driverData.get("Name");
            String[] vCarType = driverData.get("vCarType").split(",");

            boolean isCarSelected = Arrays.asList(vCarType).contains(selectedCabTypeId);

            String eHandiCapAccessibility = driverData.get("eHandiCapAccessibility");
            String eFemaleOnlyReqAccept = driverData.get("eFemaleOnlyReqAccept");
            String DriverGender = driverData.get("DriverGender");

            boolean isCarRental = true;

            if (mainAct != null && (mainAct.isRental || mainAct.iscubejekRental)) {
                String[] vRentalCarType = driverData.get("eRental").split(",");
                if (vRentalCarType != null && vRentalCarType.length > 0) {
                    isCarRental = Arrays.asList(vRentalCarType).contains(selectedCabTypeId);
                    isCarSelected = Arrays.asList(vCarType).contains(selectedCabTypeId);
                }
            }

            if (landingScreenAct != null) {
                boolean isDriverContinue = !isCarRental /*|| (!isCarSelected)*/ ||
                        (landingScreenAct.ishandicap && !eHandiCapAccessibility.equalsIgnoreCase("yes") && landingScreenAct.selectedCabType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) ||
                        (eFemaleOnlyReqAccept.equalsIgnoreCase("yes") && generalFunc.getJsonValue("eGender", landingScreenAct.userProfileJson).equalsIgnoreCase("Male") && landingScreenAct.selectedCabType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) ||
                        (landingScreenAct.isfemale && !DriverGender.equalsIgnoreCase("FeMale") && landingScreenAct.selectedCabType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) || (driverData.get("ACCEPT_CASH_TRIPS").equalsIgnoreCase("No") && landingScreenAct.isCashSelected && !landingScreenAct.selectedCabType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) ||
                        (landingScreenAct.selectedCabType.equalsIgnoreCase(Utils.CabGeneralType_UberX) && driverData.get("ACCEPT_CASH_TRIPS").equalsIgnoreCase("No") && landingScreenAct.isCashSelected && generalFunc.getJsonValueStr("APP_PAYMENT_MODE", landingScreenAct.obj_userProfile).equalsIgnoreCase("CASH"))
                        || (!selectProviderId.equals("") && !selectProviderId.equals(driverData.get("driver_id")));
                if (isDriverContinue) {
                    continue;
                }
            } else if (kioskBookNowAct != null) {
                boolean isDriverContinue = !isCarRental || (!isCarSelected) ||
                        (kioskBookNowAct.ishandicap && !eHandiCapAccessibility.equalsIgnoreCase("yes") && kioskBookNowAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) ||
                        (eFemaleOnlyReqAccept.equalsIgnoreCase("yes") && generalFunc.getJsonValue("eGender", kioskBookNowAct.obj_userProfile.toString()).equalsIgnoreCase("Male") && kioskBookNowAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) ||
                        (kioskBookNowAct.isfemale && !DriverGender.equalsIgnoreCase("FeMale") && kioskBookNowAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) || (driverData.get("ACCEPT_CASH_TRIPS").equalsIgnoreCase("No") && kioskBookNowAct.isCashSelected && !kioskBookNowAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX)) ||
                        (kioskBookNowAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX) && driverData.get("ACCEPT_CASH_TRIPS").equalsIgnoreCase("No") && kioskBookNowAct.isCashSelected == true && generalFunc.getJsonValueStr("APP_PAYMENT_MODE", kioskBookNowAct.obj_userProfile).equalsIgnoreCase("CASH"))
                        || (!selectProviderId.equals("") && !selectProviderId.equals(driverData.get("driver_id")));

                if (isDriverContinue) {
                    continue;
                }
            } else {
                boolean isDriverContinue = !isCarRental || (!isCarSelected) ||
                        (mainAct.ishandicap == true && !eHandiCapAccessibility.equalsIgnoreCase("yes") && mainAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) ||
                        (eFemaleOnlyReqAccept.equalsIgnoreCase("yes") && generalFunc.getJsonValue("eGender", mainAct.obj_userProfile.toString()).equalsIgnoreCase("Male") && mainAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) ||
                        (mainAct.isfemale && !DriverGender.equalsIgnoreCase("FeMale") && mainAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) || (driverData.get("ACCEPT_CASH_TRIPS").equalsIgnoreCase("No") && mainAct.isCashSelected && !mainAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX)) ||
                        (mainAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX) && driverData.get("ACCEPT_CASH_TRIPS").equalsIgnoreCase("No") && mainAct.isCashSelected && generalFunc.getJsonValueStr("APP_PAYMENT_MODE", mainAct.obj_userProfile).equalsIgnoreCase("CASH"))
                        || (!selectProviderId.equals("") && !selectProviderId.equals(driverData.get("driver_id")));
                if (isDriverContinue) {
                    continue;
                }
            }

            double driverLocLatitude = generalFunc.parseDoubleValue(0.0, driverData.get("Latitude"));
            double driverLocLongitude = generalFunc.parseDoubleValue(0.0, driverData.get("Longitude"));

            if (mainAct != null && mainAct.pickUpLocation == null) {
                return;
            } else if (landingScreenAct != null && landingScreenAct.pickUpLocation == null) {
                return;
            } else if (kioskBookNowAct != null && kioskBookNowAct.pickUpLocation == null) {
                return;
            }
            double distance = 0.00;
            if (mainAct != null) {
                distance = Utils.CalculationByLocation(mainAct.pickUpLocation.getLatitude(), mainAct.pickUpLocation.getLongitude(), driverLocLatitude, driverLocLongitude, "");
            } else if (kioskBookNowAct != null) {
                distance = Utils.CalculationByLocation(kioskBookNowAct.pickUpLocation.getLatitude(), kioskBookNowAct.pickUpLocation.getLongitude(), driverLocLatitude, driverLocLongitude, "");
            } else if (landingScreenAct != null) {
                distance = Utils.CalculationByLocation(landingScreenAct.pickUpLocation.getLatitude(), landingScreenAct.pickUpLocation.getLongitude(), driverLocLatitude, driverLocLongitude, "");
            }


            if (isFirst_lowestKM == true) {
                lowestKM = distance;
                isFirst_lowestKM = false;
            } else {
                if (distance < lowestKM) {
                    lowestKM = distance;
                }
            }

            float PROVIDER_RADIUS_int = GeneralFunctions.parseFloatValue(-1, driverData.get("PROVIDER_RADIUS"));

            if (mainAct != null && ((PROVIDER_RADIUS_int != -1 && distance < PROVIDER_RADIUS_int && mainAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX)) || (distance < RESTRICTION_KM_NEAREST_TAXI && !mainAct.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX)))) {
                driverData.put("DIST_TO_PICKUP", "" + distance);


                driverData.put("DIST_TO_PICKUP_INT", "" + String.format("%.2f", (float) distance));

                if (generalFunc.getJsonValue("eUnit", userProfileJson).equals("KMs")) {
                    driverData.put("DIST_TO_PICKUP_INT_ROW", String.format("%.2f", (float) distance) + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " + generalFunc.retrieveLangLBl("", "LBL_AWAY"));
                    driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));

                } else {
                    driverData.put("DIST_TO_PICKUP_INT_ROW", String.format("%.2f", (float) distance) + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " + generalFunc.retrieveLangLBl("", "LBL_AWAY"));
                    driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT"));

                }


                driverData.put("LBL_BTN_REQUEST_PICKUP_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_BTN_REQUEST_PICKUP_TXT"));
                driverData.put("LBL_SEND_REQUEST", "" + generalFunc.retrieveLangLBl("", "LBL_SEND_REQ"));
                driverData.put("LBL_MORE_INFO_TXT", "" + generalFunc.retrieveLangLBl("More info", "LBL_MORE_INFO"));
                driverData.put("LBL_AWAY", "" + generalFunc.retrieveLangLBl("away", "LBL_AWAY"));
                // driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));
                currentLoadedDrivers.add(driverData);

                Marker driverMarker_temp = mainAct.getDriverMarkerOnPubNubMsg(driverData.get("driver_id"), true);

                if (driverMarker_temp != null) {
                    driverMarker_temp.remove();
                }

                if (mainAct != null && !mainAct.isCabSelectionPhase) {
                    builder.include(new LatLng(driverLocLatitude, driverLocLongitude));
                    Marker driverMarker = drawMarker(new LatLng(driverLocLatitude, driverLocLongitude), driverName, driverData);
                    driverMarkerList_temp.add(driverMarker);
                }
            } else if (landingScreenAct != null && ((PROVIDER_RADIUS_int != -1 && distance < PROVIDER_RADIUS_int && landingScreenAct.selectedCabType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) || (distance < RESTRICTION_KM_NEAREST_TAXI && !landingScreenAct.selectedCabType.equalsIgnoreCase(Utils.CabGeneralType_UberX)))) {
                driverData.put("DIST_TO_PICKUP", "" + distance);

                driverData.put("DIST_TO_PICKUP_INT", "" + String.format("%.2f", (float) distance));

                if (generalFunc.getJsonValue("eUnit", userProfileJson).equals("KMs")) {
                    driverData.put("DIST_TO_PICKUP_INT_ROW", String.format("%.2f", (float) distance) + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " + generalFunc.retrieveLangLBl("", "LBL_AWAY"));
                    driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));

                } else {
                    driverData.put("DIST_TO_PICKUP_INT_ROW", String.format("%.2f", (float) distance) + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " + generalFunc.retrieveLangLBl("", "LBL_AWAY"));
                    driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT"));

                }


                driverData.put("LBL_BTN_REQUEST_PICKUP_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_BTN_REQUEST_PICKUP_TXT"));
                driverData.put("LBL_SEND_REQUEST", "" + generalFunc.retrieveLangLBl("", "LBL_SEND_REQ"));
                driverData.put("LBL_MORE_INFO_TXT", "" + generalFunc.retrieveLangLBl("More info", "LBL_MORE_INFO"));
                driverData.put("LBL_AWAY", "" + generalFunc.retrieveLangLBl("away", "LBL_AWAY"));
                // driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));
                currentLoadedDrivers.add(driverData);

            } else if (kioskBookNowAct != null && ((PROVIDER_RADIUS_int != -1 && distance < PROVIDER_RADIUS_int && kioskBookNowAct.getSelectedCabType().equalsIgnoreCase(Utils.CabGeneralType_UberX)) || (distance < RESTRICTION_KM_NEAREST_TAXI && !kioskBookNowAct.getSelectedCabType().equalsIgnoreCase(Utils.CabGeneralType_UberX)))) {
                driverData.put("DIST_TO_PICKUP", "" + distance);


                driverData.put("DIST_TO_PICKUP_INT", "" + String.format("%.2f", (float) distance));

                if (generalFunc.getJsonValue("eUnit", userProfileJson).equals("KMs")) {
                    driverData.put("DIST_TO_PICKUP_INT_ROW", String.format("%.2f", (float) distance) + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " + generalFunc.retrieveLangLBl("", "LBL_AWAY"));
                    driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));

                } else {
                    driverData.put("DIST_TO_PICKUP_INT_ROW", String.format("%.2f", (float) distance) + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " + generalFunc.retrieveLangLBl("", "LBL_AWAY"));
                    driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT"));

                }


                driverData.put("LBL_BTN_REQUEST_PICKUP_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_BTN_REQUEST_PICKUP_TXT"));
                driverData.put("LBL_SEND_REQUEST", "" + generalFunc.retrieveLangLBl("", "LBL_SEND_REQ"));
                driverData.put("LBL_MORE_INFO_TXT", "" + generalFunc.retrieveLangLBl("More info", "LBL_MORE_INFO"));
                driverData.put("LBL_AWAY", "" + generalFunc.retrieveLangLBl("away", "LBL_AWAY"));
                // driverData.put("LBL_KM_DISTANCE_TXT", "" + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));
                currentLoadedDrivers.add(driverData);

                Marker driverMarker_temp = kioskBookNowAct.getDriverMarkerOnPubNubMsg(driverData.get("driver_id"), true);

                if (driverMarker_temp != null) {
                    driverMarker_temp.remove();
                }
            }


            if (mainAct != null && mainAct.pickUpLocation != null && !mainAct.isCabSelectionPhase) {
                builder.include(new LatLng(mainAct.pickUpLocation.getLatitude(), mainAct.pickUpLocation.getLongitude()));
            }
        }

        removeDriversFromMap(false);


        driverMarkerList.addAll(driverMarkerList_temp);


        if (mainAct != null || landingScreenAct != null || kioskBookNowAct != null) {

            if (lowestKM > 1.5 && mainAct != null && !mainAct.isCabSelectionPhase) {
                if (mainAct.isFirstZoomlevel) {
                    try {
                        mainAct.isFirstZoomlevel = false;
                        gMapView.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));

                    } catch (Exception e) {

                    }
                }

            } else if (lowestKM > 1.5 && kioskBookNowAct != null) {
                if (mainAct != null && mainAct.isFirstZoomlevel) {
                    try {
                        mainAct.isFirstZoomlevel = false;
                        gMapView.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));

                    } catch (Exception e) {

                    }
                }

            }

            int lowestTime = ((int) (lowestKM * DRIVER_ARRIVED_MIN_TIME_PER_MINUTE));

            if (lowestTime < 1) {
                lowestTime = 1;
            }

            isAvailableCab = true;
            if (mainAct != null) {
                mainAct.setETA("" + lowestTime + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));
            } else if (kioskBookNowAct != null) {
//                Utils.printLog("kiosk", "lowestTime 1" + lowestTime);
                kioskBookNowAct.setETA("" + lowestTime + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));
            } else if (landingScreenAct != null) {
//                Utils.printLog("kiosk", "lowestTime 1" + lowestTime);
                landingScreenAct.setETA("" + lowestTime, generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));
            }
        }

        if (mainAct != null || landingScreenAct != null || kioskBookNowAct != null) {

            ArrayList<String> unSubscribeChannelList = new ArrayList<>();
            ArrayList<String> subscribeChannelList = new ArrayList<>();
            ArrayList<String> currentDriverChannelsList = new ArrayList<>();
            ArrayList<String> newDriverChannelsList = new ArrayList<>();
            if (landingScreenAct != null) {
                currentDriverChannelsList = landingScreenAct.getDriverLocationChannelList();
                newDriverChannelsList = landingScreenAct.getDriverLocationChannelList(currentLoadedDrivers);
            } else if (kioskBookNowAct != null) {
                currentDriverChannelsList = kioskBookNowAct.getDriverLocationChannelList();
                newDriverChannelsList = kioskBookNowAct.getDriverLocationChannelList(currentLoadedDrivers);
            } else {
                currentDriverChannelsList = mainAct.getDriverLocationChannelList();
                newDriverChannelsList = mainAct.getDriverLocationChannelList(currentLoadedDrivers);
            }

            for (int i = 0; i < currentDriverChannelsList.size(); i++) {
                String channel_name = currentDriverChannelsList.get(i);
                if (!newDriverChannelsList.contains(channel_name)) {
                    unSubscribeChannelList.add(channel_name);
                }
            }

            for (int i = 0; i < newDriverChannelsList.size(); i++) {
                String channel_name = newDriverChannelsList.get(i);
                if (!currentDriverChannelsList.contains(channel_name)) {
                    subscribeChannelList.add(channel_name);
                }
            }

            if (mainAct != null) {
                mainAct.setCurrentLoadedDriverList(currentLoadedDrivers);
            } else if (kioskBookNowAct != null) {
                kioskBookNowAct.setCurrentLoadedDriverList(currentLoadedDrivers);
            } else if (landingScreenAct != null) {
                landingScreenAct.setCurrentLoadedDriverList(currentLoadedDrivers);
            }


            AppService.getInstance().executeService(new EventInformation.EventInformationBuilder().setChanelList(subscribeChannelList).build(), AppService.Event.SUBSCRIBE);
            AppService.getInstance().executeService(new EventInformation.EventInformationBuilder().setChanelList(unSubscribeChannelList).build(), AppService.Event.UNSUBSCRIBE);
        }

        if (currentLoadedDrivers.size() == 0) {
            if (mainAct != null) {
                mainAct.notifyNoCabs();
            } else if (kioskBookNowAct != null) {
                kioskBookNowAct.notifyNoCabs();
            } else if (landingScreenAct != null) {
//                Utils.printLog("kiosk", "lowestTime 2");
                landingScreenAct.setETA("--", "");
            }

            if (isCheckAgain == true) {
                checkAvailableCabs();
            }
        } else {

            if (mainAct != null) {
                mainAct.notifyCabsAvailable();
            } else if (landingScreenAct != null) {
                landingScreenAct.notifyCabsAvailable();
            }
        }
    }

    public Marker drawMarker(LatLng point, String Name, HashMap<String, String> driverData) {

        MarkerOptions markerOptions = new MarkerOptions();
        String eIconType = generalFunc.getSelectedCarTypeData(selectedCabTypeId, mainAct.cabTypesArrList, "eIconType");

        int iconId = R.mipmap.car_driver;
        if (eIconType.equalsIgnoreCase("Bike")) {
            iconId = R.mipmap.car_driver_1;
        } else if (eIconType.equalsIgnoreCase("Cycle")) {
            iconId = R.mipmap.car_driver_2;
        } else if (eIconType.equalsIgnoreCase("Truck")) {
            iconId = R.mipmap.car_driver_4;
        }

        SelectableRoundedImageView providerImgView = null;
        View marker_view = null;


        if (mainAct != null) {

            markerOptions.position(point).title("DriverId" + driverData.get("driver_id")).icon(BitmapDescriptorFactory.fromResource(iconId))
                    .anchor(0.5f, 0.5f).flat(true);

        } else {
            markerOptions.position(point).title("DriverId" + driverData.get("driver_id")).icon(BitmapDescriptorFactory.fromResource(iconId))
                    .anchor(0.5f, 0.5f).flat(true);
        }


        // Adding marker on the Google Map
        final Marker marker = gMapView.addMarker(markerOptions);
        marker.setRotation(0);
        marker.setVisible(true);


        return marker;
    }

    public void onPauseCalled() {

        if (updateDriverListTask != null) {
            updateDriverListTask.stopRepeatingTask();
        }
    }

    public void onResumeCalled() {
        if (updateDriverListTask != null && isTaskKilled == false) {
            updateDriverListTask.startRepeatingTask();
        }
    }

    @Override
    public void onTaskRun() {
        checkAvailableCabs();
    }

    public void closeDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

//                              OVER MARKERS AND DRIVER DETAILS
//    ======================================================================================================

}