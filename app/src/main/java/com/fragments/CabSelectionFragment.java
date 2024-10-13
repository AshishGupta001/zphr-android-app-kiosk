package com.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.adapter.files.CabTypeAdapter;
import com.zphr.kiosk.FareBreakDownActivity;
import com.zphr.kiosk.KioskCabSelectionActivity;
import com.zphr.kiosk.R;
import com.general.files.GeneralFunctions;
import com.general.files.ActUtils;
import com.google.android.gms.maps.model.LatLng;
import com.service.handler.ApiHandler;
import com.service.handler.AppService;
import com.service.model.DataProvider;
import com.service.server.ServerTask;
import com.utils.Utils;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class CabSelectionFragment extends Fragment implements CabTypeAdapter.OnItemClickList, ViewTreeObserver.OnGlobalLayoutListener {


    static KioskCabSelectionActivity mainAct;
    static GeneralFunctions generalFunc;
    public int currentPanelDefaultStateHeight = 100;
    public String currentCabGeneralType = "";
    public CabTypeAdapter adapter;
    public ArrayList<HashMap<String, String>> cabTypeList;
    //    public int isSelcted = -1;
    public boolean isroutefound = false;
    public int selpos = 0;
    public View view = null;
    public boolean isSkip = false;
    public LatLng sourceLocation = null;
    public LatLng destLocation = null;
    public int fragmentWidth = 0;
    public int fragmentHeight = 0;
    RecyclerView carTypeRecyclerView;
    MTextView noServiceTxt;
    AVLoadingIndicatorView loaderView;
    ProgressBar mProgressBar;
    String userProfileJson = "";
    JSONObject obj_hotelProfile;
    String currency_sign = "";
    boolean isKilled = false;
    public String distance = "";
    public String time = "";
    String RideDeliveryType = "";
    int i = 0;
    int j = 0;
    ServerTask estimateFareTask;
    boolean isRouteFail = false;
    int height = 0;
    int width = 0;
    String required_str = "";
    boolean isCabsLoadedIsInProcess = true;
    boolean isFirstTime;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.fragment_new_cab_selection, container, false);
        mainAct = (KioskCabSelectionActivity) getActivity();

        generalFunc = mainAct.generalFunc;
        mainAct.btn_proceed.setVisibility(View.GONE);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        if (mainAct.isCabSelectionPhase) {
            height = displayMetrics.heightPixels;
        } else {
            height = displayMetrics.heightPixels - Utils.dpToPx(getActContext(), 300);
        }

        mProgressBar = (ProgressBar) view.findViewById(R.id.mProgressBar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(
                getActContext().getResources().getColor(R.color.appThemeColor_2), android.graphics.PorterDuff.Mode.SRC_IN);
        findRoute("--");
        RideDeliveryType = getArguments().getString("RideDeliveryType");

        carTypeRecyclerView = (RecyclerView) view.findViewById(R.id.carTypeRecyclerView);

        if (Utils.IS_KIOSK_APP) {
            carTypeRecyclerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        loaderView = (AVLoadingIndicatorView) view.findViewById(R.id.loaderView);

        noServiceTxt = (MTextView) view.findViewById(R.id.noServiceTxt);

        userProfileJson = mainAct.obj_userProfile.toString();
        obj_hotelProfile = mainAct.obj_hotelProfile;

        currency_sign = generalFunc.getJsonValue("CurrencySymbol", userProfileJson);


        isKilled = false;

        setLabels(true);


        configRideLaterBtnArea(false);

        addGlobalLayoutListner();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        addGlobalLayoutListner();
    }

    private void addGlobalLayoutListner() {

        if (getView() != null) {
            getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
        if (view != null) {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        if (getView() != null) {

            getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        } else if (view != null) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    public void showLoader() {
        try {
            loaderView.setVisibility(View.VISIBLE);
            closeNoServiceText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showNoServiceText() {
        noServiceTxt.setVisibility(View.VISIBLE);
    }

    public void closeNoServiceText() {
        noServiceTxt.setVisibility(View.GONE);
    }

    public void closeLoader() {
        try {
            loaderView.setVisibility(View.GONE);
            if (mainAct.cabTypesArrList.size() == 0) {
                showNoServiceText();
            } else {
                closeNoServiceText();
            }
        } catch (Exception e) {

        }
    }

    public void setUserProfileJson() {
        userProfileJson = mainAct.obj_userProfile.toString();
    }


    public void setLabels(boolean isCallGenerateType) {

        if (mainAct == null) {
            return;
        }

        if (generalFunc == null) {
            generalFunc = mainAct.generalFunc;
        }

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");

        if ((mainAct.currentLoadedDriverList != null && mainAct.currentLoadedDriverList.size() < 1) || mainAct.currentLoadedDriverList == null) {
            if (isCallGenerateType) {
                generateCarType();
            }
            return;

        }

        noServiceTxt.setText(generalFunc.retrieveLangLBl("service not available in this location", "LBL_NO_SERVICE_AVAILABLE_TXT"));


        if (isCallGenerateType) {
            generateCarType();
        }

    }


    public void releaseResources() {
        isKilled = true;
    }


    public String getCurrentCabGeneralType() {
        return this.currentCabGeneralType;
    }

    public void configRideLaterBtnArea(boolean isGone) {

            if (isGone == true) {
                mainAct.setPanelHeight(237);
                mainAct.setUserLocImgBtnMargin(105);
                return;
            }
            if (!generalFunc.getJsonValue("RIIDE_LATER", userProfileJson).equalsIgnoreCase("YES")) {
                mainAct.setUserLocImgBtnMargin(105);
                mainAct.setPanelHeight(237);
            } else {


                mainAct.setPanelHeight(237);
                currentPanelDefaultStateHeight = 237;
                mainAct.setUserLocImgBtnMargin(164);
            }

    }

    public void generateCarType() {
        if (getActContext() == null) {
            return;
        }

        if (cabTypeList == null) {
            cabTypeList = new ArrayList<>();
            if (adapter == null) {
                adapter = new CabTypeAdapter(getActContext(), cabTypeList, generalFunc);
                adapter.setSelectedVehicleTypeId(mainAct.getSelectedCabTypeId());
                if (mainAct.destLocation != null) {
                    adapter.isDestinationAdded(true);
                }
                carTypeRecyclerView.setAdapter(adapter);
                adapter.setOnItemClickList(this);
            } else {
                adapter.notifyDataSetChanged();
            }
        } else {
            cabTypeList.clear();
            mainAct.btn_proceed.setVisibility(View.VISIBLE);
        }



        for (int i = 0; i < mainAct.cabTypesArrList.size(); i++) {
            HashMap<String, String> map = new HashMap<>();
            String iVehicleTypeId = mainAct.cabTypesArrList.get(i).get("iVehicleTypeId");

            String vVehicleType = mainAct.cabTypesArrList.get(i).get("vVehicleType");
            String vRentalVehicleTypeName = mainAct.cabTypesArrList.get(i).get("vRentalVehicleTypeName");
            String fPricePerKM = mainAct.cabTypesArrList.get(i).get("fPricePerKM");
            String fPricePerMin = mainAct.cabTypesArrList.get(i).get("fPricePerMin");
            String iBaseFare = mainAct.cabTypesArrList.get(i).get("iBaseFare");
            String fCommision = mainAct.cabTypesArrList.get(i).get("fCommision");
            String iPersonSize = mainAct.cabTypesArrList.get(i).get("iPersonSize");
            String vLogo = mainAct.cabTypesArrList.get(i).get("vLogo");
            String vLogo1 = mainAct.cabTypesArrList.get(i).get("vLogo1");
            String eType = mainAct.cabTypesArrList.get(i).get("eType");

            String eRental = mainAct.cabTypesArrList.get(i).get("eRental");


            if (!eType.equalsIgnoreCase(currentCabGeneralType)) {
                continue;
            }
            map.put("iVehicleTypeId", iVehicleTypeId);
            map.put("vVehicleType", vVehicleType);
            map.put("vRentalVehicleTypeName", vRentalVehicleTypeName);
            map.put("fPricePerKM", fPricePerKM);
            map.put("fPricePerMin", fPricePerMin);
            map.put("iBaseFare", iBaseFare);
            map.put("fCommision", fCommision);
            map.put("iPersonSize", iPersonSize);
            map.put("vLogo", vLogo);
            map.put("vLogo1", vLogo1);

            if (generalFunc.getJsonValue("eUnit", userProfileJson).equals("KMs")) {
                map.put("eUnitTxt", generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));

            } else {
                map.put("eUnitTxt", generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT"));
            }


            map.put("eRental", "No");


            if (i == 0) {
                adapter.setSelectedVehicleTypeId(iVehicleTypeId);
            }
            cabTypeList.add(map);
            if (eRental != null && eRental.equalsIgnoreCase("Yes")) {
                HashMap<String, String> rentalmap = (HashMap<String, String>) map.clone();
                rentalmap.put("eRental", "Yes");
            }

        }

        mainAct.setCabTypeList(cabTypeList);
        adapter.notifyDataSetChanged();

        if (Utils.IS_KIOSK_APP && adapter != null) {
            findRoute("--");
        }

        if (cabTypeList.size() > 0) {
            isFirstTime = true;
            onItemClick(0, "");
        }
    }

    public void closeLoadernTxt() {
        loaderView.setVisibility(View.GONE);
        closeNoServiceText();

    }



    public Context getActContext() {
        return mainAct.getActContext();
    }

    @Override
    public void onItemClick(int position, String selected) {


        String iVehicleTypeId = cabTypeList.get(position).get("iVehicleTypeId");
        selpos = position;

        if (!iVehicleTypeId.equals(mainAct.getSelectedCabTypeId())) {
            mainAct.selectedCabTypeId = iVehicleTypeId;
            adapter.setSelectedVehicleTypeId(iVehicleTypeId);
            adapter.notifyDataSetChanged();
            mainAct.changeCabType(iVehicleTypeId);

            if (cabTypeList.get(position).get("eFlatTrip") != null &&
                    (!cabTypeList.get(position).get("eFlatTrip").equalsIgnoreCase(""))
                    && cabTypeList.get(position).get("eFlatTrip").equalsIgnoreCase("Yes")) {
                mainAct.isFixFare = true;
            } else {
                mainAct.isFixFare = false;
            }
        } else if (selected.equalsIgnoreCase("info")) {
            if (Utils.IS_KIOSK_APP) {

                Bundle bn = new Bundle();
                bn.putString("SelectedCar", cabTypeList.get(position).get("iVehicleTypeId"));
                bn.putString("iUserId", generalFunc.getMemberId());
                bn.putString("distance", distance);
                bn.putString("time", time);
                bn.putString("PromoCode", "");
                if (cabTypeList.get(position).get("eRental").equals("yes")) {
                    bn.putString("vVehicleType", cabTypeList.get(position).get("vRentalVehicleTypeName"));
                } else {
                    bn.putString("vVehicleType", cabTypeList.get(position).get("vVehicleType"));
                }
                bn.putBoolean("isSkip", isSkip);
                if (mainAct.getPickUpLocation() != null) {
                    bn.putString("picupLat", mainAct.getPickUpLocation().getLatitude() + "");
                    bn.putString("pickUpLong", mainAct.getPickUpLocation().getLongitude() + "");
                }
                if (mainAct.destLocation != null) {
                    bn.putString("destLat", mainAct.destLocation.getLatitude() + "");
                    bn.putString("destLong", mainAct.destLocation.getLongitude() + "");
                    bn.putBoolean("isSkip", false);
                } else {
                    bn.putString("destLat", "");
                    bn.putString("destLong", "");
                    bn.putBoolean("isSkip", true);

                }
                if (mainAct.isFixFare) {
                    bn.putBoolean("isFixFare", true);
                } else {
                    bn.putBoolean("isFixFare", false);
                }

                new ActUtils(getActContext()).startActWithData(FareBreakDownActivity.class, bn);
            }

        }


    }

    public void findRoute(String etaVal) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (adapter == null) {
            return;
        }

        try {


            if (mainAct.destLocation != null) {
                isSkip = true;
                hashMap.put("d_latitude", mainAct.destLocation.getLatitude() + "");
                hashMap.put("d_longitude", mainAct.destLocation.getLongitude() + "");
            } else {
                isSkip = false;
                hashMap.put("d_latitude", mainAct.getPickUpLocation().getLatitude() + "");
                hashMap.put("d_longitude", mainAct.getPickUpLocation().getLongitude() + "");
            }

            if (Utils.IS_KIOSK_APP) {
                isCabsLoadedIsInProcess = true;
            } else {
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);
            }

            hashMap.put("s_latitude", mainAct.getPickUpLocation().getLatitude() + "");
            hashMap.put("s_longitude", mainAct.getPickUpLocation().getLongitude() + "");

            AppService.getInstance().executeService(mainAct.getActContext(), new DataProvider.DataProviderBuilder(hashMap.get("s_latitude"), hashMap.get("s_longitude")).setDestLatitude(hashMap.get("d_latitude")).setDestLongitude(hashMap.get("d_longitude")).setWayPoints(new JSONArray()).build(), AppService.Service.DIRECTION, new AppService.ServiceDelegate() {
                @Override
                public void onResult(HashMap<String, Object> data) {
                    if (!Utils.IS_KIOSK_APP) {
                        mProgressBar.setIndeterminate(false);
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }

                    if (data.get("RESPONSE_TYPE") != null && data.get("RESPONSE_TYPE").toString().equalsIgnoreCase("FAIL")) {
                        isRouteFail = true;
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_DEST_ROUTE_NOT_FOUND"));
                        return;
                    }
                    responseString = data.get("ROUTES").toString();

                    isRouteFail = false;
                    if (responseString != null && !responseString.equalsIgnoreCase("") && data.get("DISTANCE") == null) {
                        if (!Utils.IS_KIOSK_APP) {
                            mProgressBar.setIndeterminate(false);
                            mProgressBar.setVisibility(View.INVISIBLE);

                        }
                        JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                        if (obj_routes != null && obj_routes.length() > 0) {
                            JSONObject obj_legs = generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0);


                            distance = "" + (generalFunc.parseDoubleValue(0, generalFunc.getJsonValue("value",
                                    generalFunc.getJsonValue("distance", obj_legs.toString()).toString())));

                            time = "" + (generalFunc.parseDoubleValue(0, generalFunc.getJsonValue("value",
                                    generalFunc.getJsonValue("duration", obj_legs.toString()).toString())));

                            sourceLocation = new LatLng(generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("lat", generalFunc.getJsonValue("start_location", obj_legs.toString()))),
                                    generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("lng", generalFunc.getJsonValue("start_location", obj_legs.toString()))));

                            destLocation = new LatLng(generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("lat", generalFunc.getJsonValue("end_location", obj_legs.toString()))),
                                    generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("lng", generalFunc.getJsonValue("end_location", obj_legs.toString()))));


                            if (getActivity() != null) {
                                estimateFare(distance, time, isSkip);
                            }
                        }
                    } else {
                        distance = data.get("DISTANCE").toString();
                        time = data.get("DURATION").toString();

                        sourceLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, hashMap.get("s_latitude")), GeneralFunctions.parseDoubleValue(0.0, hashMap.get("s_longitude"))
                        );

                        destLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, hashMap.get("d_latitude")), GeneralFunctions.parseDoubleValue(0.0, hashMap.get("d_longitude"))
                        );


                        if (getActivity() != null) {
                            estimateFare(distance, time, isSkip);
                        }

                    }


                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAvailableCarTypesIds() {
        String carTypesIds = "";
        for (int i = 0; i < mainAct.cabTypesArrList.size(); i++) {
            String iVehicleTypeId = mainAct.cabTypesArrList.get(i).get("iVehicleTypeId");

            carTypesIds = carTypesIds.equals("") ? iVehicleTypeId : (carTypesIds + "," + iVehicleTypeId);
        }
        return carTypesIds;
    }

    public void estimateFare(final String distance, String time, boolean isSkip) {

        if (estimateFareTask != null) {
            estimateFareTask.cancel(true);
            estimateFareTask = null;
        }
        if (distance == null && time == null) {


        } else {
            if (mainAct.loadAvailCabs != null) {
                if (mainAct.loadAvailCabs.isAvailableCab) {
                    isroutefound = true;
                    if (!mainAct.timeval.equalsIgnoreCase("\n" + "--")) {
                        mainAct.noCabAvail = false;
                    }
                }
            }

        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "estimateFareNew");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("SelectedCarTypeID", getAvailableCarTypesIds());
        if (distance != null && time != null) {
            parameters.put("distance", distance);
            parameters.put("time", time);
        }
        parameters.put("SelectedCar", mainAct.getSelectedCabTypeId());
        parameters.put("PromoCode", "");

        if (mainAct.destLocation != null) {
            parameters.put("DestLatitude", "" + destLocation.latitude);
            parameters.put("DestLongitude", "" + destLocation.longitude);
        }

        if (mainAct.getPickUpLocation() != null) {
            parameters.put("StartLatitude", "" + mainAct.getPickUpLocation().getLatitude());
            parameters.put("EndLongitude", "" + mainAct.getPickUpLocation().getLongitude());
        }


        estimateFareTask = ApiHandler.execute(getActContext(), parameters,
                responseString -> {

                    if (responseString != null && !responseString.equals("")) {


                        boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                        if (isDataAvail == true) {

                            JSONArray vehicleTypesArr = generalFunc.getJsonArray(Utils.message_str, responseString);
                            for (int i = 0; i < vehicleTypesArr.length(); i++) {

                                JSONObject obj_temp = generalFunc.getJsonObject(vehicleTypesArr, i);

                                if (distance != null) {

                                    String type = mainAct.getCurrentCabGeneralType();
                                    if (type.equalsIgnoreCase("rental")) {
                                        type = Utils.CabGeneralType_Ride;
                                    }
                                    if (generalFunc.getJsonValue("eType", obj_temp.toString()).
                                            contains(type)) {


                                        if (cabTypeList != null) {
                                            for (int k = 0; k < cabTypeList.size(); k++) {
                                                HashMap<String, String> map = cabTypeList.get(k);

                                                if (/*map.get("vVehicleType").equalsIgnoreCase(generalFunc.getJsonValue("vVehicleType", obj_temp.toString()))
                                                && */map.get("iVehicleTypeId").equalsIgnoreCase(generalFunc.getJsonValue("iVehicleTypeId", obj_temp.toString()))) {

                                                    String totalfare = "";


                                                        if (map.get("eRental").equalsIgnoreCase("Yes") && mainAct.iscubejekRental) {
                                                            totalfare = generalFunc.getJsonValue("eRental_total_fare", obj_temp.toString());
                                                        } else {
                                                            totalfare = generalFunc.getJsonValue(isSkip == false || mainAct.destLocation == null ? "fPricePerKMKiosk" : "total_fare", obj_temp.toString());

                                                        }


                                                    if (!totalfare.equals("") && totalfare != null) {
                                                        map.put("total_fare", totalfare);
                                                        map.put("eFlatTrip", generalFunc.getJsonValue("eFlatTrip", obj_temp.toString()));
                                                        cabTypeList.set(k, map);
                                                    } else {
                                                        map.put("eFlatTrip", generalFunc.getJsonValue("eFlatTrip", obj_temp.toString()));
                                                        cabTypeList.set(k, map);
                                                    }
                                                } else {

                                                }

                                            }
                                        }


                                    }
                                } else {


                                    if (generalFunc.getJsonValue("eType", obj_temp.toString()).equalsIgnoreCase(mainAct.getCurrentCabGeneralType())) {

                                        for (int k = 0; k < cabTypeList.size(); k++) {
                                            HashMap<String, String> map = cabTypeList.get(k);

                                            if (/*map.get("vVehicleType").equalsIgnoreCase(generalFunc.getJsonValue("vVehicleType", obj_temp.toString()))
                                            &&*/ map.get("iVehicleTypeId").equalsIgnoreCase(generalFunc.getJsonValue("iVehicleTypeId", obj_temp.toString()))) {
                                                map.put("total_fare", "");
                                                cabTypeList.set(k, map);

//                                            Utils.printELog("cabTypeList", ":: " + cabTypeList);
                                            }

                                        }
                                    }
                                }

                            }

                            isCabsLoadedIsInProcess = false;
                            adapter.notifyDataSetChanged();

                        }
                    }
                });

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        releseInstances();
    }

    private void releseInstances() {
        Utils.hideKeyboard(getActContext());
        if (estimateFareTask != null) {
            estimateFareTask.cancel(true);
            estimateFareTask = null;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        releseInstances();
    }

    @Override
    public void onGlobalLayout() {
        boolean heightChanged = false;
        if (getView() != null || view != null) {
            if (getView() != null) {

                if (getView().getHeight() != 0 && getView().getHeight() != fragmentHeight) {
                    heightChanged = true;
                }
                fragmentWidth = getView().getWidth();
                fragmentHeight = getView().getHeight();
            } else if (view != null) {

                if (view.getHeight() != 0 && view.getHeight() != fragmentHeight) {
                    heightChanged = true;
                }

                fragmentWidth = view.getWidth();
                fragmentHeight = view.getHeight();
            }


            if (heightChanged && fragmentWidth != 0 && fragmentHeight != 0) {
                mainAct.setPanelHeight(fragmentHeight);
            }
        }
    }


    String responseString;


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(getActContext());
        }
    }


}


