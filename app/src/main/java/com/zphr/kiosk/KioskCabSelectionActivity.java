package com.zphr.kiosk;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.activity.ParentActivity;
import com.fragments.CabSelectionFragment;
import com.general.files.ActUtils;
import com.general.files.BounceAnimation;
import com.general.files.CreateAnimation;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.LoadAvailableCab;
import com.general.files.PolyLineAnimator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.model.Hotel;
import com.service.handler.AppService;
import com.service.model.EventInformation;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class KioskCabSelectionActivity extends ParentActivity implements GetLocationUpdates.LocationUpdates, GetAddressFromLocation.AddressFound {


    public String hotelProfileJson = "";
    public Location userLocation;
    public ArrayList<HashMap<String, String>> currentLoadedDriverList;
    public CabSelectionFragment cabSelectionFrag;
    public LoadAvailableCab loadAvailCabs;
    public Location pickUpLocation;
    public String selectedCabTypeId = "";
    public boolean isCashSelected = true;
    public String pickUpLocationAddress = "";
    public ArrayList<HashMap<String, String>> cabTypesArrList = new ArrayList<>();
    public boolean isUserLocbtnclik = false;
    public boolean ishandicap = false;
    public boolean isfemale = false;
    public String timeval = "";
    public boolean noCabAvail = false;
    public Location destLocation;
    public GenerateAlertBox noCabAvailAlertBox;

    public JSONObject obj_hotelProfile;
    public boolean isFirstZoomlevel = true;
    public boolean isMenuImageShow = true;
    public boolean isRental = false;
    public boolean iscubejekRental = false;
    public String eShowOnlyMoto = "";
    public boolean isFixFare = false;
    public boolean isCabSelectionPhase = true;
    public GetAddressFromLocation getAddressFromLocation;
    MTextView titleTxt;
    //GetLocationUpdates getLastLocation;
    GoogleMap gMap;
    boolean isFirstLocation = true;
    RelativeLayout dragView;
    ArrayList<HashMap<String, String>> cabTypeList;
    String DRIVER_REQUEST_METHOD = "All";
    SelectableRoundedImageView driverImgView;
    String eTripType = "";
    String required_str = "";
    String RideDeliveryType = "";
    InternetConnection intCheck;
    boolean schedulrefresh = false;
    //Noti
    LinearLayout rduTollbar;
    ImageView backImgView;
    public MTextView btn_proceed;
    private ImageView iv_hotelImg;

    ImageView iv_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kiosk_activity_main);


        cabSelectionFrag = null;

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        rduTollbar = (LinearLayout) findViewById(R.id.rduTollbar);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        iv_hotelImg = (ImageView) findViewById(R.id.iv_hotelImg);
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        backImgView.setOnClickListener(new setOnClickList());
        if (generalFunc.isRTLmode()) {
            backImgView.setScaleX(-1f);
        }


        hotelProfileJson = generalFunc.retrieveValue(Utils.HOTEL_PROFILE_JSON);
        obj_hotelProfile = generalFunc.getJsonObject(hotelProfileJson);

        intCheck = new InternetConnection(getActContext());


        if (Utils.IS_KIOSK_APP) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_YOUR_VEHICLE"));

            //android.view.Display display = ((android.view.WindowManager) getActContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            //String BANNER_IMAGE = Utils.getResizeImgURL(getActContext(), generalFunc.getJsonValueStr("BANNER_IMAGE", obj_hotelProfile), display.getWidth(), display.getHeight());
            /*new LoadImage.builder(LoadImage.bind(BANNER_IMAGE), iv_hotelImg)
                    .setErrorImagePath(Utils.checkText(BANNER_IMAGE) ? R.drawable.above_shadow : R.drawable.ic_hotelsample)
                    .setPlaceholderImagePath(Utils.checkText(BANNER_IMAGE) ? R.drawable.above_shadow : R.drawable.ic_hotelsample)
                    .build();*/


        }


        dragView = (RelativeLayout) findViewById(R.id.dragView);

        setGeneralData();
        setLabels();

        new CreateAnimation(dragView, getActContext(), R.anim.design_bottom_sheet_slide_in, 100, false).startAnimation();


        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        generalFunc.deleteTripStatusMessages();


        if (Utils.IS_KIOSK_APP) {

            btn_proceed = (MTextView) findViewById(R.id.btn_type2);
            btn_proceed.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_NEXT_TXT"));
            btn_proceed.setOnClickListener(new setOnClickList());


            new Handler().postDelayed(() -> {

                if (cabSelectionFrag != null) {
                    cabSelectionFrag.showLoader();
                }

                initLoadCab();
            }, 2500);


            String KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY = generalFunc.retrieveValue(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY);
            if (Utils.checkText(KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY)) {

                Gson gson = new Gson();
                String data1 = KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY;
                Hotel hotelDestDetails = gson.fromJson(data1, new TypeToken<Hotel>() {
                        }.getType()
                );

                double vDestLongitude = generalFunc.parseDoubleValue(0.00, hotelDestDetails.getvDestLongitude());
                double vDestLatitude = generalFunc.parseDoubleValue(0.00, hotelDestDetails.getvDestLatitude());

                if (vDestLongitude != 0 && vDestLongitude != 0.0 && vDestLatitude != 0 && vDestLatitude != 0.0) {
                    destLocation = new Location("");
                    destLocation.setLongitude(vDestLongitude);
                    destLocation.setLatitude(vDestLatitude);
                }
            }

            setTitleAsperStage();
        }

    }


    private void setTitleAsperStage() {

        isMenuImageShow = false;

        if (isCabSelectionPhase) {
            findViewById(R.id.cabSelectionArea).setVisibility(View.VISIBLE);
            rduTollbar.setVisibility(View.VISIBLE);
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_YOUR_VEHICLE"));
            addcabselectionfragment();
        }
    }

    public void addcabselectionfragment() {
        setRiderDefaultView();
    }

    public void setLabels() {
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        try {
            outState.putString("RESTART_STATE", "true");
            super.onSaveInstanceState(outState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGeneralData() {
        HashMap<String, String> storeData = new HashMap<>();
        storeData.put(Utils.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValueStr("MOBILE_VERIFICATION_ENABLE", obj_userProfile));
        String DRIVER_REQUEST_METHOD = generalFunc.getJsonValueStr("DRIVER_REQUEST_METHOD", obj_userProfile);

        this.DRIVER_REQUEST_METHOD = DRIVER_REQUEST_METHOD.equals("") ? "All" : DRIVER_REQUEST_METHOD;

        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValueStr("REFERRAL_SCHEME_ENABLE", obj_userProfile));
        storeData.put(Utils.WALLET_ENABLE, generalFunc.getJsonValueStr("WALLET_ENABLE", obj_userProfile));
        storeData.put(Utils.SMS_BODY_KEY, generalFunc.getJsonValueStr(Utils.SMS_BODY_KEY, obj_userProfile));

        generalFunc.storeData(storeData);

        String LOGO_IMAGE = generalFunc.getJsonValueStr("LOGO_IMAGE", obj_hotelProfile);

        LOGO_IMAGE = Utils.getResizeImgURL(getActContext(), LOGO_IMAGE, Utils.dpToPx(getActContext(), getActContext().getResources().getDimension(R.dimen._95sdp)), Utils.dpToPx(getActContext(), getActContext().getResources().getDimension(R.dimen._65sdp)));

        if (Utils.checkText(LOGO_IMAGE)) {
            iv_logo.setVisibility(View.VISIBLE);
            //new LoadImage.builder(LoadImage.bind(LOGO_IMAGE), iv_logo).setErrorImagePath(R.mipmap.ic_no_icon).setPlaceholderImagePath(R.mipmap.ic_no_icon).build();
        }


    }

    public void setUserLocImgBtnMargin(int margin) {
    }


    private void setRiderDefaultView() {
        if (cabSelectionFrag == null) {
            Bundle bundle = new Bundle();
            bundle.putString("RideDeliveryType", RideDeliveryType);
            cabSelectionFrag = new CabSelectionFragment();
            cabSelectionFrag.setArguments(bundle);

        }

        setCurrentType();

        try {
            super.onPostResume();
        } catch (Exception e) {
        }


        getSupportFragmentManager().beginTransaction().replace(R.id.dragView, cabSelectionFrag).commit();

    }

    private void setCurrentType() {

        if (cabSelectionFrag == null) {
            return;
        }

        cabSelectionFrag.currentCabGeneralType = Utils.CabGeneralType_Ride;

    }

    private void changeLable() {
        if (cabSelectionFrag != null) {
            cabSelectionFrag.setLabels(false);
        }
    }

    @Override
    public void onLocationUpdate(Location location) {

        if (location == null) {
            return;
        }

        if (getIntent().getStringExtra("latitude") != null && getIntent().getStringExtra("longitude") != null) {
            Location loc_ufx = new Location("gps");
            loc_ufx.setLatitude(GeneralFunctions.parseDoubleValue(0.0, getIntent().getStringExtra("latitude")));
            loc_ufx.setLongitude(GeneralFunctions.parseDoubleValue(0.0, getIntent().getStringExtra("longitude")));
            this.userLocation = loc_ufx;
        } else {
            this.userLocation = location;
        }

        if (isFirstLocation == true) {

            if (Utils.IS_KIOSK_APP && isCabSelectionPhase) {
                initLoadCab();

                isFirstLocation = false;
            }

        } else if (loadAvailCabs != null && (Utils.IS_KIOSK_APP && isCabSelectionPhase)) {
            loadAvailCabs.setPickUpLocation(pickUpLocation);
        }
    }


    public void initLoadCab() {

        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.KIOSK_HOTEL_ADDRESS_KEY, "");
        data.put(Utils.KIOSK_HOTEL_Lattitude_KEY, "");
        data.put(Utils.KIOSK_HOTEL_Longitude_KEY, "");
        data = generalFunc.retrieveValue(data);

        String hotelAddress = Utils.checkText(data.get(Utils.KIOSK_HOTEL_ADDRESS_KEY)) ? data.get(Utils.KIOSK_HOTEL_Lattitude_KEY) : "";
        double hotel_Lattitude = generalFunc.parseDoubleValue(0.00, data.get(Utils.KIOSK_HOTEL_Lattitude_KEY));
        double hotel_Longitude = generalFunc.parseDoubleValue(0.00, data.get(Utils.KIOSK_HOTEL_Longitude_KEY));
        if (Utils.checkText(hotelAddress) && hotel_Lattitude != 0.00 && hotel_Longitude != 0.00) {
            initLoadAvailcab(hotelAddress, hotel_Lattitude, hotel_Longitude, "");
            return;
        } else if (pickUpLocation == null) {
            //userLocation = getLastLocation.getLastLocation();
            Location temploc = new Location("PickupLoc");
            temploc.setLatitude(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("vAddressLat", obj_userProfile)));
            temploc.setLongitude(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("vAddressLong", obj_userProfile)));
            onLocationUpdate(temploc);
        }

        if (userLocation == null) {
            return;
        }

        setSourceAddress(userLocation.getLatitude(), userLocation.getLongitude());

    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {
        onAddressFound(address);
        HashMap<String, String> storeData = new HashMap<>();
        storeData.put(Utils.KIOSK_HOTEL_ADDRESS_KEY, address);
        storeData.put(Utils.KIOSK_HOTEL_Lattitude_KEY, "" + latitude);
        storeData.put(Utils.KIOSK_HOTEL_Longitude_KEY, "" + longitude);
        generalFunc.storeData(storeData);
        initLoadAvailcab(address, latitude, longitude, geocodeobject);
    }

    private void initLoadAvailcab(String address, double latitude, double longitude, String geocodeobject) {
        Location pickUpLoc = new Location("");
        pickUpLoc.setLatitude(latitude);
        pickUpLoc.setLongitude(longitude);

        pickUpLocation = pickUpLoc;

        if (loadAvailCabs == null) {
            loadAvailCabs = new LoadAvailableCab(getActContext(), generalFunc, getSelectedCabType(), pickUpLocation, null, obj_userProfile.toString());
            loadAvailCabs.setPickUpLocation(pickUpLoc);
            loadAvailCabs.pickUpAddress = address;
            loadAvailCabs.currentGeoCodeResult = geocodeobject;
            loadAvailCabs.checkAvailableCabs();
        } else {
            loadAvailCabs.setPickUpLocation(pickUpLoc);
            loadAvailCabs.pickUpAddress = address;
            loadAvailCabs.currentGeoCodeResult = geocodeobject;
        }
    }

    public void setSourceAddress(double latitude, double longitude) {
        try {
            getAddressFromLocation.setLocation(latitude, longitude);
            getAddressFromLocation.execute();
        } catch (Exception e) {

        }
    }

    public void setETA(String time) {
        timeval = time;
    }


    public String getCurrentCabGeneralType() {


        if (cabSelectionFrag != null) {
            return cabSelectionFrag.getCurrentCabGeneralType();
        } else if (!eTripType.trim().equals("")) {
            return eTripType;
        }

        return Utils.CabGeneralType_Ride;
    }

    public void setCabTypeList(ArrayList<HashMap<String, String>> cabTypeList) {
        this.cabTypeList = cabTypeList;
    }

    public void changeCabType(String selectedCabTypeId) {
        this.selectedCabTypeId = selectedCabTypeId;
        if (loadAvailCabs != null) {
            loadAvailCabs.setCabTypeId(this.selectedCabTypeId);
            loadAvailCabs.setPickUpLocation(pickUpLocation);
            loadAvailCabs.changeCabs();
        }
    }

    public String getSelectedCabTypeId() {

        return this.selectedCabTypeId;

    }

    public String getSelectedCabType() {

        return getCurrentCabGeneralType();

    }

    public void setPanelHeight(int value) {
        if (Utils.IS_KIOSK_APP) {
            return;
        }
    }

    public Location getPickUpLocation() {
        return this.pickUpLocation;
    }

    public String getPickUpLocationAddress() {
        return this.pickUpLocationAddress;
    }

    public void notifyCarSearching() {
        setETA("\n" + "--");
    }

    public void notifyNoCabs() {

        setETA("\n" + "--");
        setCurrentLoadedDriverList(new ArrayList<HashMap<String, String>>());

        if (cabSelectionFrag != null) {
            noCabAvail = false;
            changeLable();
        }

        changeLable();

    }

    public void notifyCabsAvailable() {
        if (cabSelectionFrag != null && loadAvailCabs != null && loadAvailCabs.listOfDrivers != null && loadAvailCabs.listOfDrivers.size() > 0) {
            if (cabSelectionFrag.isroutefound) {
                if (loadAvailCabs.isAvailableCab) {
                    if (!timeval.equalsIgnoreCase("\n" + "--")) {
                        noCabAvail = true;
                    }
                }
            }
        }

        if (cabSelectionFrag != null) {
            cabSelectionFrag.setLabels(false);
        }
    }


    public void onAddressFound(String address) {
        if (cabSelectionFrag != null) {
            notifyCabsAvailable();
        } else {
            if (isUserLocbtnclik && !isCabSelectionPhase) {
                isUserLocbtnclik = false;
            }
        }
    }


    public void setCurrentLoadedDriverList(ArrayList<HashMap<String, String>> currentLoadedDriverList) {
        this.currentLoadedDriverList = currentLoadedDriverList;
    }

    public ArrayList<String> getDriverLocationChannelList() {

        ArrayList<String> channels_update_loc = new ArrayList<>();

        if (currentLoadedDriverList != null) {

            for (int i = 0; i < currentLoadedDriverList.size(); i++) {
                channels_update_loc.add(Utils.pubNub_Update_Loc_Channel_Prefix + "" + (currentLoadedDriverList.get(i).get("driver_id")));
            }

        }
        return channels_update_loc;
    }

    public ArrayList<String> getDriverLocationChannelList(ArrayList<HashMap<String, String>> listData) {

        ArrayList<String> channels_update_loc = new ArrayList<>();

        if (listData != null) {

            for (int i = 0; i < listData.size(); i++) {
                channels_update_loc.add(Utils.pubNub_Update_Loc_Channel_Prefix + "" + (listData.get(i).get("driver_id")));
            }

        }
        return channels_update_loc;
    }


    public void unSubscribeCurrentDriverChannels() {
        if (currentLoadedDriverList != null) {

            AppService.getInstance().executeService(new EventInformation.EventInformationBuilder().setChanelList(getDriverLocationChannelList()).build(), AppService.Event.UNSUBSCRIBE);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (loadAvailCabs != null) {
            loadAvailCabs.onPauseCalled();
        }

        unSubscribeCurrentDriverChannels();
    }

    @Override
    protected void onResume() {
        super.onResume();

        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));


        if (!schedulrefresh) {
            if (loadAvailCabs != null) {

                loadAvailCabs.onResumeCalled();
            }
        }


        if (currentLoadedDriverList != null) {
            AppService.getInstance().executeService(new EventInformation.EventInformationBuilder().setChanelList(getDriverLocationChannelList()).build(), AppService.Event.SUBSCRIBE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {


            Utils.runGC();

        } catch (Exception e) {

        }

    }


    public void setDriverImgView(SelectableRoundedImageView driverImgView) {
        this.driverImgView = driverImgView;
    }


    public Marker getDriverMarkerOnPubNubMsg(String iDriverId, boolean isRemoveFromList) {

        if (loadAvailCabs != null) {
            ArrayList<Marker> currentDriverMarkerList = loadAvailCabs.getDriverMarkerList();

            if (currentDriverMarkerList != null) {
                for (int i = 0; i < currentDriverMarkerList.size(); i++) {
                    Marker marker = currentDriverMarkerList.get(i);

                    String driver_id = marker.getTitle().replace("DriverId", "");

                    if (driver_id.equals(iDriverId)) {

                        if (isRemoveFromList) {
                            loadAvailCabs.getDriverMarkerList().remove(i);
                        }

                        return marker;
                    }

                }
            }
        }


        return null;
    }

    @Override
    public void onBackPressed() {
        callBackEvent(false);
    }

    public void callBackEvent(boolean status) {
        try {


            if (cabSelectionFrag == null) {


            } else if (Utils.IS_KIOSK_APP && isCabSelectionPhase) {
                releaseCabSelectionInstances();
            }
            super.onBackPressed();
            overridePendingTransitionExit();

        } catch (Exception e) {
            Log.e("Exception", "::" + e.toString());
        }
    }

    private void releaseCabSelectionInstances() {


        if (loadAvailCabs != null) {
            loadAvailCabs.setTaskKilledValue(true);
        }

        if (cabSelectionFrag != null) {
            cabSelectionFrag.releaseResources();
            getSupportFragmentManager().beginTransaction().remove(cabSelectionFrag).commit();
            cabSelectionFrag = null;
        }

        if (PolyLineAnimator.getInstance() != null) {
            PolyLineAnimator.getInstance().stopRouteAnim();
        }

        if (gMap != null) {
            gMap.clear();
        }

        super.onBackPressed();
    }

    public Context getActContext() {
        return KioskCabSelectionActivity.this;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, 1, 0, "" + generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        menu.add(0, 2, 0, "" + generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {


        return super.onContextItemSelected(item);
    }


    public void buildNoCabMessage(String message, String positiveBtn) {

        if (noCabAvailAlertBox != null) {
            noCabAvailAlertBox.closeAlertBox();
            noCabAvailAlertBox = null;
        }

        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(true);
        generateAlert.setBtnClickList(btn_id -> generateAlert.closeAlertBox());
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);
        generateAlert.showAlertBox();

        noCabAvailAlertBox = generateAlert;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    // activity start & finish transition effect started

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public class setOnClickList implements View.OnClickListener, BounceAnimation.BounceAnimListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(getActContext());
            if (i == btn_proceed.getId()) {

                BounceAnimation.setBounceAnimation(getActContext(), btn_proceed);
                BounceAnimation.setBounceAnimListener(this);

            } else if (i == backImgView.getId()) {
                onBackPressed();
            }
        }


        @Override
        public void onAnimationFinished(View view) {
            if (view == btn_proceed) {
                if ((currentLoadedDriverList != null && currentLoadedDriverList.size() < 1) || currentLoadedDriverList == null || (cabTypeList != null && cabTypeList.size() < 1) || cabTypeList == null) {

                    buildNoCabMessage(generalFunc.retrieveLangLBl("", "LBL_NO_CARS_AVAIL_IN_TYPE"),
                            generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                    return;
                }

                if (cabSelectionFrag == null) {
                    return;
                }

               /* if (!Utils.checkText(cabSelectionFrag.distance) && !Utils.checkText(cabSelectionFrag.time)) {
                    return;
                }*/

                // LBL_SELECT_YOUR_VEHICLE


                Bundle bn = new Bundle();
                bn.putSerializable("selectedCabTypeDetail", cabTypeList.get(cabSelectionFrag.selpos));
                bn.putSerializable("isSkip", destLocation == null ? "true" : "false");
                bn.putString("distance", cabSelectionFrag.distance);
                bn.putString("time", cabSelectionFrag.time);
                new ActUtils(getActContext()).startActWithData(KioskBookNowActivity.class, bn);
            }
        }
    }

    // activity start & finish transition effect ended

}
