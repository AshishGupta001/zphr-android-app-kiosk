package com.zphr.kiosk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.activity.ParentActivity;
import com.dialogs.RequestNearestCab;
import com.fragments.CabSelectionFragment;
import com.fragments.MainHeaderFragment;
import com.general.files.ActUtils;
import com.general.files.BounceAnimation;
import com.general.files.CreateAnimation;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.HashMapComparator;
import com.general.files.InternetConnection;
import com.general.files.LoadAvailableCab;
import com.general.files.LocalNotification;
import com.general.files.MyApp;
import com.general.files.PolyLineAnimator;
import com.general.files.RecurringTask;
import com.general.files.SlideAnimationUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;
import com.model.Hotel;
import com.service.handler.ApiHandler;
import com.service.handler.AppService;
import com.service.model.EventInformation;
import com.service.server.ServerTask;
import com.utils.CommonUtilities;
import com.utils.LoadImage;
import com.utils.MarkerAnim;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class KioskBookNowActivity extends ParentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GetLocationUpdates.LocationUpdates, GetAddressFromLocation.AddressFound, /*profileDelegate, */BounceAnimation.BounceAnimListener {


    public String currentGeoCodeObject = "";
    public ImageView userLocBtnImgView;
    public Location userLocation;
    public ArrayList<HashMap<String, String>> currentLoadedDriverList;
    public CabSelectionFragment cabSelectionFrag;
    public LoadAvailableCab loadAvailCabs;
    public Location pickUpLocation;
    public String selectedCabTypeId = "";
    public HashMap<String, String> selectedCabTypeDetail = new HashMap<>();
    public boolean isDestinationAdded = false;
    public String destLocLatitude = "";
    public String destLocLongitude = "";
    public String destAddress = "";
    public boolean isCashSelected = false;
    public String pickUpLocationAddress = "";

    public AVLoadingIndicatorView loaderView;
    public ArrayList<HashMap<String, String>> cabTypesArrList = new ArrayList<>();
    public boolean isUserLocbtnclik = false;
    public String tempPickupGeoCode = "";
    public String tempDestGeoCode = "";
    public boolean isUfx = false;
    public String uberXAddress = "";
    public double uberXlat = 0.0;
    public double uberXlong = 0.0;
    public boolean ishandicap = false;
    public boolean isfemale = false;
    public String timeval = "";
    public RequestNearestCab requestNearestCab;
    public boolean isDestinationMode = false;
    public String bookingtype = "";
    public boolean noCabAvail = false;
    public Location destLocation;
    public boolean isDriverAssigned = false;
    public GenerateAlertBox noCabAvailAlertBox;
    public JSONObject obj_userProfile;
    public String SelectDate = "";
    public boolean isFirstTime = true;
    public SupportMapFragment map;
    public MainHeaderFragment mainHeaderFrag;
    public HashMap<String, String> driverAssignedData;
    public String assignedDriverId = "";
    public String assignedTripId = "";
    public String cabRquestType = Utils.CabReqType_Now; // Later OR Now
    public boolean isTripStarted = false;
    public RelativeLayout rootRelView;
    public boolean isMenuImageShow = true;
    public boolean isRental = false;
    public double pickUp_tmpLatitude = 0.0;
    public double pickUp_tmpLongitude = 0.0;
    public String pickUp_tmpAddress = "";
    public String selectedSortValue = "";
    public boolean isFixFare = false;
    public GetAddressFromLocation getAddressFromLocation;
    MTextView titleTxt;
    //GetLocationUpdates getLastLocation;
    GoogleMap gMap;
    boolean isFirstLocation = true;
    RelativeLayout dragView;
    LinearLayout mainArea;
    FrameLayout mainContent;
    ArrayList<HashMap<String, String>> cabTypeList;
    String DRIVER_REQUEST_METHOD = "All";
    SelectableRoundedImageView driverImgView;
    RecurringTask allCabRequestTask;
    SendNotificationsToDriverByDist sendNotificationToDriverByDist;
    Intent deliveryData;
    String eTripType = "";
    androidx.appcompat.app.AlertDialog alertDialog_surgeConfirm;
    String required_str = "";
    String tripId = "";
    String RideDeliveryType = "";
    double tollamount = 0.0;
    String tollcurrancy = "";
    boolean isrideschedule = false;
    boolean isreqnow = false;
    ImageView prefBtnImageView;
    androidx.appcompat.app.AlertDialog pref_dialog;
    androidx.appcompat.app.AlertDialog tolltax_dialog;
    boolean isTollCostdilaogshow = false;
    boolean istollIgnore = false;
    boolean isnotification = false;
    boolean isdelivernow = false;
    boolean isdeliverlater = false;
    boolean isTripEnded = false;
    InternetConnection intCheck;
    boolean isufxpayment = false;
    String appliedPromoCode = "";
    String userComment = "";
    boolean schedulrefresh = false;
    String iCabBookingId = "";
    boolean isRebooking = false;
    String type = "";
    //Noti
    boolean isufxbackview = false;
    String payableAmount = "";
    boolean isTripActive = false;
    LinearLayout rduTollbar;
    ImageView backImgView;
    LinearLayout infoimage;
    GenerateAlertBox reqSentErrorDialog = null;
    String eWalletDebitAllow = "No";
    boolean isWalletPopupFirst = false;
    boolean isFirst = true;
    boolean isIinitializeViewsCall = false;
    String selectedTime = "";
    MTextView btn_book_now_type2;
    private String tripStatus = "";
    private String currentTripId = "";
    private boolean isAddressEnable;
    private MTextView cabTypeNumText, fareNumText;
    private MTextView cabTypeNameTxt, cabPersonCapacityTxt, cabTypeTitleTxt;
    private MTextView fareSubTitleTxt, fareValueTxt, fareTitleTxt;
    private ImageView carTypeImgView;
    private ImageView fareTypeImgView;

    private String distance = "";
    private String time = "";
    private String isSkip = "";

    /** Addon : payment gateway */
    private static final int WEBVIEWPAYMENT = 001;

    public void initPaymentWebView(){
        Bundle bn = new Bundle();
        //String url = generalFunc.getJsonValue("PAYMENT_MODE_URL", obj_userProfile) + "&eType=" + getCurrentCabGeneralType();
        String url = generalFunc.getJsonValue("PAYMENT_KIOSK_URL", obj_userProfile) + "&eType=" + getCurrentCabGeneralType();
        url = url + "&tSessionId=" + (generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY));
        url = url + "&GeneralUserType=" + Utils.app_type;
        url = url + "&GeneralMemberId=" + generalFunc.getMemberId();
        url = url + "&ePaymentOption=" + "Card";
        url = url + "&vPayMethod=" + "Instant";
        url = url + "&SYSTEM_TYPE=" + "APP";
        url = url + "&PAGE_TYPE=" + "PAYMENT_LIST";
        url = url + "&vCurrentTime=" + generalFunc.getCurrentDateHourMin();
        bn.putString("url", url);
        bn.putBoolean("handleResponse", true);
        bn.putBoolean("isBack", false);
        bn.putString("CouponCode", ""/*generalFunc.getJsonValue("PromoCode", getProfilePaymentModel.getProfileInfo()).toString()*/);
        bn.putString("eType", getCurrentCabGeneralType());
        bn.putBoolean("eFly", false/*mainAct.eFly*/);

        if (pickUpLocation != null) {
            bn.putString("vSourceLatitude", pickUpLocation.getLatitude() + "");
            bn.putString("vSourceLongitude", pickUpLocation.getLongitude() + "");
        }
        if (destLocation != null) {
            bn.putString("vDestLatitude", destLocation.getLatitude() + "");
            bn.putString("vDestLongitude", destLocation.getLongitude() + "");
            bn.putBoolean("isSkip", false);
        } else {
            bn.putString("vDestLatitude", "");
            bn.putString("vDestLongitude", "");
            bn.putBoolean("isSkip", true);
        }
        bn.putString("eTakeAway", "No");
        new ActUtils(getActContext()).startActForResult(PaymentWebviewActivity.class, bn, WEBVIEWPAYMENT);
    }

    @Override
    public void onAnimationFinished(View view) {
        if (view == btn_book_now_type2) {
            if (getDestinationStatus() == false) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please add your destination location " + "to deliver your package.", "LBL_ADD_DEST_MSG"));
                return;
            }
            if (mainHeaderFrag != null && mainHeaderFrag.isCabsLoadedIsInProcess) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Route not found", "LBL_DEST_ROUTE_NOT_FOUND"));
                return;
            }
            if ((currentLoadedDriverList != null && currentLoadedDriverList.size() < 1) || currentLoadedDriverList == null) {

                buildNoCabMessage(generalFunc.retrieveLangLBl("", "LBL_NO_CARS_AVAIL_IN_TYPE"),
                        generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                return;
            }
            if (mainHeaderFrag.isRouteFail) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Route not found", "LBL_DEST_ROUTE_NOT_FOUND"));
                return;
            }
            setCabReqType(Utils.CabReqType_Now);
            if (cabRquestType.equalsIgnoreCase(Utils.CabReqType_Now)) {
                continuePickUpProcess();
            } else {
                checkSurgePrice(selectedTime, deliveryData);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kiosk_layout_booking);

        cabSelectionFrag = null;
        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        rootRelView = (RelativeLayout) findViewById(R.id.rootRelView);

        isTripActive = getIntent().getBooleanExtra("isTripActive", false);
        rduTollbar = (LinearLayout) findViewById(R.id.rduTollbar);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        infoimage = (LinearLayout) findViewById(R.id.infoimage);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        prefBtnImageView = (ImageView) findViewById(R.id.prefBtnImageView);
        backImgView.setOnClickListener(new setOnClickList());

        selectedSortValue = generalFunc.retrieveLangLBl("", "LBL_FEATURED_TXT");

        fareNumText = (MTextView) findViewById(R.id.fareNumText);
        fareValueTxt = (MTextView) findViewById(R.id.fareValueTxt);
        fareTitleTxt = (MTextView) findViewById(R.id.fareTitleTxt);
        fareSubTitleTxt = (MTextView) findViewById(R.id.fareSubTitleTxt);
        cabTypeNumText = (MTextView) findViewById(R.id.cabTypeNumText);
        cabTypeTitleTxt = (MTextView) findViewById(R.id.cabTypeTitleTxt);
        cabTypeNameTxt = (MTextView) findViewById(R.id.cabTypeNameTxt);
        cabPersonCapacityTxt = (MTextView) findViewById(R.id.cabPersonCapacityTxt);

        carTypeImgView = (ImageView) findViewById(R.id.carTypeImgView);
        fareTypeImgView = (ImageView) findViewById(R.id.fareTypeImgView);


        if (getIntent().hasExtra("selectedCabTypeDetail")) {
            selectedCabTypeDetail = (HashMap<String, String>) getIntent().getSerializableExtra("selectedCabTypeDetail");
        }

        distance = getIntent().getStringExtra("distance");
        time = getIntent().getStringExtra("time");
        isSkip = getIntent().getStringExtra("isSkip");

        if (getIntent().getStringExtra("iCabBookingId") != null) {
            iCabBookingId = getIntent().getStringExtra("iCabBookingId");
        }

        if (getIntent().getStringExtra("type") != null) {
            type = getIntent().getStringExtra("type");
            bookingtype = getIntent().getStringExtra("type");
        }


        isRebooking = getIntent().getBooleanExtra("isRebooking", false);
        intCheck = new InternetConnection(getActContext());
        isufxpayment = getIntent().getBooleanExtra("isufxpayment", false);
        isUfx = getIntent().getBooleanExtra("isufx", false);
        isnotification = getIntent().getBooleanExtra("isnotification", false);
        RideDeliveryType = Utils.CabGeneralType_Ride;
        btn_book_now_type2 = (MTextView) findViewById(R.id.btn_book_now_type2);
        btn_book_now_type2.setOnClickListener(new setOnClickList());
        if (Utils.IS_KIOSK_APP) {
            setTitleAsperStage();
        }
        if (getIntent().hasExtra("tripId")) {
            tripId = getIntent().getStringExtra("tripId");
        }

        String TripDetails = generalFunc.getJsonValueStr("TripDetails", obj_userProfile);

        if (TripDetails != null && !TripDetails.equals("")) {
            tripId = generalFunc.getJsonValue("iTripId", TripDetails);
        }

        mainContent = (FrameLayout) findViewById(R.id.mainContent);
        userLocBtnImgView = (ImageView) findViewById(R.id.userLocBtnImgView);

        if (!isUfx) {
            mainContent.setVisibility(View.VISIBLE);
            userLocBtnImgView.setVisibility(View.VISIBLE);
        } else {
            prefBtnImageView.setVisibility(View.GONE);
        }

        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        userLocBtnImgView = (ImageView) findViewById(R.id.userLocBtnImgView);
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        //sliding_layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        dragView = (RelativeLayout) findViewById(R.id.dragView);
        mainArea = (LinearLayout) findViewById(R.id.mainArea);
        mainContent = (FrameLayout) findViewById(R.id.mainContent);

        prefBtnImageView.setOnClickListener(new setOnClickList());
        infoimage.setOnClickListener(new setOnClickList());

        map.getMapAsync(KioskBookNowActivity.this);


        setGeneralData();
        setLabels();

        new CreateAnimation(dragView, getActContext(), R.anim.design_bottom_sheet_slide_in, 100, false).startAnimation();


        userLocBtnImgView.setOnClickListener(new setOnClickList());

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                releaseScheduleNotificationTask();
                generalFunc.restartApp();
            }
        }

        generalFunc.deleteTripStatusMessages();

    }


    private void setTitleAsperStage() {

        findViewById(R.id.bookingDetailArea).setVisibility(View.VISIBLE);
        SlideAnimationUtil.slideInFromLeft(getActContext(), findViewById(R.id.bookingDetailArea));

        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.KIOSK_HOTEL_ADDRESS_KEY, "");
        data.put(Utils.KIOSK_HOTEL_Lattitude_KEY, "");
        data.put(Utils.KIOSK_HOTEL_Longitude_KEY, "");
        data = generalFunc.retrieveValue(data);

        String hotelAddress = Utils.checkText(data.get(Utils.KIOSK_HOTEL_ADDRESS_KEY)) ? data.get(Utils.KIOSK_HOTEL_ADDRESS_KEY) : "";
        double hotel_Lattitude = generalFunc.parseDoubleValue(0.00, data.get(Utils.KIOSK_HOTEL_Lattitude_KEY));
        double hotel_Longitude = generalFunc.parseDoubleValue(0.00, data.get(Utils.KIOSK_HOTEL_Longitude_KEY));

        Location location = new Location("gps");
        location.setLatitude(hotel_Lattitude);
        location.setLongitude(hotel_Longitude);

        pickUpLocation = location;
        pickUpLocationAddress = hotelAddress;


        getDestinationLocation();

        isMenuImageShow = false;
        findViewById(R.id.mainArea).setVisibility(View.VISIBLE);
        rduTollbar.setVisibility(View.VISIBLE);
        titleTxt.setText(generalFunc.retrieveLangLBl("Book A Taxi Now", "LBL_BOOK_TAXI_NOW"));
        ((MTextView) findViewById(R.id.actualTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_ACTUAL_FARE_VARY_TXT"));
        btn_book_now_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BOOK_NOW"));

        cabTypeNumText.setText("1");
        fareNumText.setText("2");

        new CreateRoundedView(getActContext().getResources().getColor(R.color.appThemeColor_1), Utils.dipToPixels(getActContext(), 40), 0,
                getActContext().getResources().getColor(R.color.appThemeColor_1), cabTypeNumText);

        new CreateRoundedView(getActContext().getResources().getColor(R.color.appThemeColor_1), Utils.dipToPixels(getActContext(), 40), 0,
                getActContext().getResources().getColor(R.color.appThemeColor_1), fareNumText);

        if (selectedCabTypeDetail.size() > 0) {
            selectedCabTypeId = selectedCabTypeDetail.get("iVehicleTypeId");
            cabTypeNameTxt.setText(selectedCabTypeDetail.get("vVehicleType"));
            cabTypeTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CAB_TYPE_HEADER_TXT"));
            cabPersonCapacityTxt.setText(generalFunc.convertNumberWithRTL(selectedCabTypeDetail.get("iPersonSize")) + " " + generalFunc.retrieveLangLBl("", "LBL_PEOPLE_TXT"));

            String vehicleIconPath = CommonUtilities.SERVER_URL + "webimages/icons/VehicleType/";
            String imgName = generalFunc.getImageName(selectedCabTypeDetail.get("vLogo1"), getActContext());
            String imgUrl = vehicleIconPath + selectedCabTypeDetail.get("iVehicleTypeId") + "/android/" + imgName;

            new LoadImage.builder(LoadImage.bind(imgUrl), carTypeImgView).build();

            fareTypeImgView.setImageResource(R.drawable.ic_wallet);
            fareTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FARE_DETAILS"));
            fareValueTxt.setText(isSkip.equalsIgnoreCase("false") ? generalFunc.convertNumberWithRTL(selectedCabTypeDetail.get("total_fare")) : "--");
            fareSubTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ESTIMATED_FARE"));
        }

    }


    public void showView(final View v) {
        v.startAnimation(AnimationUtils.loadAnimation(
                getActContext(), R.anim.slide_from_right
        ));
    }


    private void initLoadAvailcab(String address, double latitude, double longitude, String geocodeobject, String selectedCabTypeId) {

        Location pickUpLoc = new Location("");
        pickUpLoc.setLatitude(latitude);
        pickUpLoc.setLongitude(longitude);
        if (mainHeaderFrag != null) {
            mainHeaderFrag.configDestinationMode(false);
            mainHeaderFrag.setExistingPickUpAddress(address, pickUpLocation);
            mainHeaderFrag.onAddressFound(address, latitude, longitude, geocodeobject);
        }

        if (loadAvailCabs == null) {
            loadAvailCabs = new LoadAvailableCab(getActContext(), generalFunc, selectedCabTypeId, pickUpLocation, gMap, obj_userProfile.toString());
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


    private void getDestinationLocation() {
        String KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY = generalFunc.retrieveValue(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY);
        if (Utils.checkText(KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY)) {

            Gson gson = new Gson();
            String data1 = KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY;
            Hotel hotelDestDetails = gson.fromJson(data1, new TypeToken<Hotel>() {
                    }.getType()
            );

            String destAddress = Utils.checkText(hotelDestDetails.gettDestAddress()) ? hotelDestDetails.gettDestAddress() : "";
            setDestinationPoint(hotelDestDetails.getvDestLatitude(), hotelDestDetails.getvDestLongitude(), destAddress, true);

        }
    }

    public void addcabselectionfragment() {
        if (mainHeaderFrag != null) {
            mainHeaderFrag.addAddressFinder();
        }
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
    }

    public MainHeaderFragment getMainHeaderFrag() {
        return mainHeaderFrag;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        (findViewById(R.id.LoadingMapProgressBar)).setVisibility(View.GONE);


        if (googleMap == null) {
            return;
        }

        this.gMap = googleMap;

        if (isUfx) {
            if (getIntent().getStringExtra("SelectDate") != null) {
                SelectDate = getIntent().getStringExtra("SelectDate");
            }
            if (pickUpLocation == null) {
                Location temploc = new Location("PickupLoc");
                if (getIntent().getStringExtra("latitude") != null) {
                    temploc.setLatitude(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("latitude")));
                    temploc.setLongitude(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("longitude")));
                    onLocationUpdate(temploc);
                }
            }
        } else if (Utils.IS_KIOSK_APP && pickUpLocation != null) {
            animateToLocation(getPickUpLocation().getLatitude(), getPickUpLocation().getLongitude());
            initLoadAvailcab(pickUpLocationAddress, pickUpLocation.getLatitude(), pickUpLocation.getLongitude(), "", selectedCabTypeDetail.get("iVehicleTypeId"));
        }

        if (generalFunc.checkLocationPermission(true) == true) {
            getMap().setMyLocationEnabled(true);
            getMap().getUiSettings().setTiltGesturesEnabled(false);
            getMap().getUiSettings().setCompassEnabled(false);
            getMap().getUiSettings().setMyLocationButtonEnabled(false);
        }

        if (isUfx) {
            if (isFirst) {
                isFirst = false;
                initializeLoadCab();
            }
        }


        if (!Utils.IS_KIOSK_APP) {

            String vTripStatus = generalFunc.getJsonValueStr("vTripStatus", obj_userProfile);

            if (vTripStatus != null && (vTripStatus.equals("Active") || vTripStatus.equals("On Going Trip"))) {
                getMap().setMyLocationEnabled(false);
                String tripDetailJson = generalFunc.getJsonValueStr("TripDetails", obj_userProfile);

                if (tripDetailJson != null && !tripDetailJson.trim().equals("")) {
                    double latitude = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("tStartLat", tripDetailJson));
                    double longitude = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("tStartLong", tripDetailJson));
                    Location loc = new Location("gps");
                    loc.setLatitude(latitude);
                    loc.setLongitude(longitude);
                    onLocationUpdate(loc);
                }
            }


        }
        initializeViews();


    }


    @Override
    public void onMapClick(LatLng latLng) {

    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public void initializeLoadCab() {

        if (userLocation == null) {
            Location temploc = new Location("PickupLoc");
            temploc.setLatitude(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("vAddressLat", obj_userProfile)));
            temploc.setLongitude(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("vAddressLong", obj_userProfile)));
            onLocationUpdate(temploc);
        }

        if (loadAvailCabs == null) {
            loadAvailCabs = new LoadAvailableCab(getActContext(), generalFunc, selectedCabTypeId, userLocation,
                    getMap(), obj_userProfile.toString());
            loadAvailCabs.pickUpAddress = pickUpLocationAddress;
            loadAvailCabs.currentGeoCodeResult = currentGeoCodeObject;
            loadAvailCabs.checkAvailableCabs();
        }
    }


    public void initializeViews() {

        if (isIinitializeViewsCall) {
            if (pickUpLocation != null && mainHeaderFrag != null && !Utils.IS_KIOSK_APP) {
                mainHeaderFrag.configDestinationMode(false);
                mainHeaderFrag.setSourceAddress(pickUpLocation.getLatitude(), pickUpLocation.getLongitude());
                return;
            }

            if (mainHeaderFrag != null && Utils.IS_KIOSK_APP && pickUpLocation != null) {
                mainHeaderFrag.configDestinationMode(false);
                mainHeaderFrag.setExistingPickUpAddress(pickUpLocationAddress, pickUpLocation);
                mainHeaderFrag.onAddressFound(pickUpLocationAddress, pickUpLocation.getLatitude(), pickUpLocation.getLongitude(), "");
                return;
            }

        }


        if (pickUpLocation != null && mainHeaderFrag != null && !Utils.IS_KIOSK_APP) {
            mainHeaderFrag.setSourceAddress(pickUpLocation.getLatitude(), pickUpLocation.getLongitude());
            return;
        }
        if (mainHeaderFrag != null && Utils.IS_KIOSK_APP && pickUpLocation != null) {
            mainHeaderFrag.configDestinationMode(false);
            mainHeaderFrag.setExistingPickUpAddress(pickUpLocationAddress, pickUpLocation);
            mainHeaderFrag.onAddressFound(pickUpLocationAddress, pickUpLocation.getLatitude(), pickUpLocation.getLongitude(), "");
            return;
        }

        isIinitializeViewsCall = true;

        setMainHeaderView(false);

        Utils.runGC();
    }

    private void setMainHeaderView(boolean isUfx) {
        try {
            if (mainHeaderFrag == null) {

                mainHeaderFrag = new MainHeaderFragment();

                Bundle bundle = new Bundle();
                bundle.putBoolean("isUfx", isUfx);
                bundle.putBoolean("isRedirectMenu", true);
                mainHeaderFrag.setArguments(bundle);
                if (getMap() != null) {
                    mainHeaderFrag.setGoogleMapInstance(getMap());
                }
            }
            if (mainHeaderFrag != null) {
                if (getMap() != null) {
                    mainHeaderFrag.releaseAddressFinder();
                }
            }

            try {
                super.onPostResume();
            } catch (Exception e) {

            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.headerContainer, mainHeaderFrag).commit();

        } catch (Exception e) {

        }

    }


    public void configDestinationMode(boolean isDestinationMode) {
        this.isDestinationMode = isDestinationMode;
        try {
            if (isDestinationMode == false) {
                if (loadAvailCabs != null) {
                    loadAvailCabs.filterDrivers(false);
                }
                animateToLocation(getPickUpLocation().getLatitude(), getPickUpLocation().getLongitude());
                if (cabSelectionFrag != null) {
                    noCabAvail = false;
                    changeLable();
                }
            } else {
                if (cabSelectionFrag != null) {
                    if (loadAvailCabs != null) {
                        if (loadAvailCabs.isAvailableCab) {
                            changeLable();
                            noCabAvail = true;
                        }
                    }
                }

                if (timeval.equalsIgnoreCase("\n" + "--")) {
                    noCabAvail = false;
                } else {
                    noCabAvail = true;
                }
                changeLable();
                if (isDestinationAdded == true && !getDestLocLatitude().trim().equals("") && !getDestLocLongitude().trim().equals("")) {
                    animateToLocation(generalFunc.parseDoubleValue(0.0, getDestLocLatitude()), generalFunc.parseDoubleValue(0.0, getDestLocLongitude()));
                }

            }
            changeLable();

            if (mainHeaderFrag != null) {
                mainHeaderFrag.configDestinationMode(isDestinationMode);
            }
        } catch (Exception e) {

        }
    }

    private void changeLable() {
        if (cabSelectionFrag != null) {
            cabSelectionFrag.setLabels(false);
        }
    }

    public void animateToLocation(double latitude, double longitude) {
        if (latitude != 0.0 && longitude != 0.0) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude))
                    .zoom(gMap.getCameraPosition().zoom).build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


    private void resetMapView() {
        map.getView().invalidate();
        gMap.setPadding(0, 0, 0, 0);
        map.getView().requestLayout();
    }

    private void resetUserLocBtnView() {
        userLocBtnImgView.invalidate();
        userLocBtnImgView.requestLayout();
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


            double currentZoomLevel = Utils.defaultZomLevel;

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()))
                    .zoom((float) currentZoomLevel).build();

            if (cameraPosition != null && getMap() != null) {
                getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                onGoogleMapCameraChangeList gmap = new onGoogleMapCameraChangeList();
                gmap.onCameraIdle();
            }

            if (pickUpLocation == null) {
                pickUpLocation = this.userLocation;
                initLoadAvailcab(pickUpLocationAddress, pickUpLocation.getLatitude(), pickUpLocation.getLongitude(), "", selectedCabTypeDetail.get("iVehicleTypeId"));
                initializeViews();
            }

            isFirstLocation = false;


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

        if (mainHeaderFrag != null) {
            mainHeaderFrag.handleSourceMarker(time, false);
            mainHeaderFrag.mangeMrakerPostion();
        }

    }

    public CameraPosition cameraForUserPosition() {

        try {
            if (cabSelectionFrag != null) {
                return null;
            }

            double currentZoomLevel = getMap() == null ? Utils.defaultZomLevel : getMap().getCameraPosition().zoom;
            // if (Utils.defaultZomLevel > currentZoomLevel) {
            currentZoomLevel = Utils.defaultZomLevel;
            // }
            String TripDetails = generalFunc.getJsonValueStr("TripDetails", obj_userProfile);

            String vTripStatus = generalFunc.getJsonValueStr("vTripStatus", obj_userProfile);
            if (generalFunc.isLocationEnabled()) {

                double startLat = 0.0;
                double startLong = 0.0;

                if (vTripStatus != null && startLat != 0.0 && startLong != 0.0 && ((vTripStatus.equals("Active") || vTripStatus.equals("On Going Trip")))) {

                    Location tempickuploc = new Location("temppickkup");

                    tempickuploc.setLatitude(startLat);
                    tempickuploc.setLongitude(startLong);

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(tempickuploc.getLatitude(), tempickuploc.getLongitude()))
                            .zoom((float) currentZoomLevel).build();


                    return cameraPosition;


                } else {

                    currentZoomLevel = Utils.defaultZomLevel;

                    if (userLocation != null) {
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()))
                                .zoom((float) currentZoomLevel).build();


                        return cameraPosition;
                    } else {
                        return null;
                    }
                }
            } else if (userLocation != null) {
                if (Utils.defaultZomLevel > currentZoomLevel) {
                    currentZoomLevel = Utils.defaultZomLevel;
                }
                if (userLocation != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()))
                            .zoom((float) currentZoomLevel).build();


                    return cameraPosition;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {

        }
        return null;

    }


    public void continuePickUpProcess() {
        String pickUpLocAdd = mainHeaderFrag != null ? (mainHeaderFrag.getPickUpAddress().equals(
                generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT")) ? "" : mainHeaderFrag.getPickUpAddress()) : "";

        if (!pickUpLocAdd.equals("")) {

            setCabReqType(Utils.CabReqType_Now);
            checkSurgePrice("", null);

        }
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

    public String getSelectedCabTypeId() {

        return this.selectedCabTypeId;

    }

    public String getSelectedCabType() {

        return getCurrentCabGeneralType();

    }

    public void checkSurgePrice(final String selectedTime, final Intent data) {
        if (Utils.IS_KIOSK_APP) {
            handleRequest(data);
            return;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "checkSurgePrice");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.userType);
        parameters.put("SelectedCarTypeID", "" + getSelectedCabTypeId());

        if (!selectedTime.trim().equals("")) {
            parameters.put("SelectedTime", selectedTime);
        }

        if (getPickUpLocation() != null) {
            parameters.put("PickUpLatitude", "" + getPickUpLocation().getLatitude());
            parameters.put("PickUpLongitude", "" + getPickUpLocation().getLongitude());
        }

        if (getDestLocLatitude() != null && !getDestLocLatitude().equalsIgnoreCase("")) {
            parameters.put("DestLatitude", "" + getDestLocLatitude());
            parameters.put("DestLongitude", "" + getDestLocLongitude());
        }

        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                generalFunc.sendHeartBeat();

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                    if (!selectedTime.trim().equals("")) {
                        if (generalFunc.getJsonValue("eFlatTrip", responseString).equalsIgnoreCase("Yes")) {
                            isFixFare = true;
                            openFixChargeDialog(responseString, false, data);
                        } else {
                            handleRequest(data);
                        }

                    } else {
                        if (generalFunc.getJsonValue("eFlatTrip", responseString).equalsIgnoreCase("Yes")) {
                            isFixFare = true;
                            openFixChargeDialog(responseString, false, data);
                        } else {
                            if (!isUfx) {
                                handleRequest(data);
                            }
                        }
                    }

                } else {

                    if (!isUfx) {
                        if (generalFunc.getJsonValue("eFlatTrip", responseString).equalsIgnoreCase("Yes")) {
                            isFixFare = true;
                            openFixChargeDialog(responseString, true, data);

                        } else {
                            openSurgeConfirmDialog(responseString, selectedTime, data);
                        }
                    }
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    private void handleRequest(Intent data) {


        String driverIds = getAvailableDriverIds();

        JSONObject cabRequestedJson = new JSONObject();
        try {
            cabRequestedJson.put("Message", "CabRequested");
            cabRequestedJson.put("sourceLatitude", "" + getPickUpLocation().getLatitude());
            cabRequestedJson.put("sourceLongitude", "" + getPickUpLocation().getLongitude());
            cabRequestedJson.put("PassengerId", generalFunc.getMemberId());
            cabRequestedJson.put("PName", generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                    + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
            cabRequestedJson.put("PPicName", generalFunc.getJsonValueStr("vImgName", obj_userProfile));
            cabRequestedJson.put("PFId", generalFunc.getJsonValueStr("vFbId", obj_userProfile));
            cabRequestedJson.put("PRating", generalFunc.getJsonValueStr("vAvgRating", obj_userProfile));
            cabRequestedJson.put("PPhone", generalFunc.getJsonValueStr("vPhone", obj_userProfile));
            cabRequestedJson.put("PPhoneC", generalFunc.getJsonValueStr("vPhoneCode", obj_userProfile));
            cabRequestedJson.put("REQUEST_TYPE", getCurrentCabGeneralType());
            if (getDestinationStatus() == true) {
                cabRequestedJson.put("destLatitude", "" + getDestLocLatitude());
                cabRequestedJson.put("destLongitude", "" + getDestLocLongitude());
            } else {
                cabRequestedJson.put("destLatitude", "");
                cabRequestedJson.put("destLongitude", "");
            }

            getTollcostValue(driverIds, cabRequestedJson.toString(), data);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void openFixChargeDialog(String responseString, boolean isSurCharge, Intent data) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.surge_confirm_design, null);
        builder.setView(dialogView);

        MTextView payableAmountTxt;
        MTextView payableTxt;

        ((MTextView) dialogView.findViewById(R.id.headerMsgTxt)).setText(generalFunc.retrieveLangLBl("", generalFunc.retrieveLangLBl("", "LBL_FIX_FARE_HEADER")));


        ((MTextView) dialogView.findViewById(R.id.tryLaterTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_TRY_LATER"));

        payableTxt = (MTextView) dialogView.findViewById(R.id.payableTxt);
        payableAmountTxt = (MTextView) dialogView.findViewById(R.id.payableAmountTxt);
        if (!generalFunc.getJsonValue("fFlatTripPricewithsymbol", responseString).equalsIgnoreCase("")) {
            payableAmountTxt.setVisibility(View.VISIBLE);
            payableTxt.setVisibility(View.GONE);

            if (isSurCharge) {

                payableAmount = generalFunc.getJsonValue("fFlatTripPricewithsymbol", responseString) + " " + "(" + generalFunc.retrieveLangLBl("", "LBL_AT_TXT") + " " +
                        generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("SurgePrice", responseString)) + ")";
                ((MTextView) dialogView.findViewById(R.id.surgePriceTxt)).setText(generalFunc.convertNumberWithRTL(payableAmount));
            } else {
                payableAmount = generalFunc.getJsonValue("fFlatTripPricewithsymbol", responseString);
                ((MTextView) dialogView.findViewById(R.id.surgePriceTxt)).setText(generalFunc.convertNumberWithRTL(payableAmount));
            }
        } else {
            payableAmountTxt.setVisibility(View.GONE);
            payableTxt.setVisibility(View.VISIBLE);
        }

        MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TXT"));
        btn_type2.setId(Utils.generateViewId());

        btn_type2.setOnClickListener(view -> {
            alertDialog_surgeConfirm.dismiss();
            handleRequest(data);
        });

        (dialogView.findViewById(R.id.tryLaterTxt)).setOnClickListener(view -> {
            isFixFare = false;
            alertDialog_surgeConfirm.dismiss();
            closeRequestDialog(false);
        });


        alertDialog_surgeConfirm = builder.create();
        alertDialog_surgeConfirm.setCancelable(false);
        alertDialog_surgeConfirm.setCanceledOnTouchOutside(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog_surgeConfirm);
        }

        alertDialog_surgeConfirm.show();
    }

    public void openSurgeConfirmDialog(String responseString, final String selectedTime, Intent data) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.surge_confirm_design, null);
        builder.setView(dialogView);

        MTextView payableAmountTxt;
        MTextView payableTxt;

        ((MTextView) dialogView.findViewById(R.id.headerMsgTxt)).setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
        ((MTextView) dialogView.findViewById(R.id.surgePriceTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("SurgePrice", responseString)));

        ((MTextView) dialogView.findViewById(R.id.tryLaterTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_TRY_LATER"));

        payableTxt = (MTextView) dialogView.findViewById(R.id.payableTxt);
        payableAmountTxt = (MTextView) dialogView.findViewById(R.id.payableAmountTxt);
        payableTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PAYABLE_AMOUNT"));


        if (cabSelectionFrag != null && cabTypeList != null && cabTypeList.size() > 0 && cabTypeList.get(cabSelectionFrag.selpos).get("total_fare") != null && !cabTypeList.get(cabSelectionFrag.selpos).get("total_fare").equals("") && !cabTypeList.get(cabSelectionFrag.selpos).get("eRental").equals("Yes")) {

            payableAmountTxt.setVisibility(View.VISIBLE);
            payableTxt.setVisibility(View.GONE);

            payableAmount = generalFunc.convertNumberWithRTL(cabTypeList.get(cabSelectionFrag.selpos).get("total_fare"));


            payableAmountTxt.setText(generalFunc.retrieveLangLBl("Approx payable amount", "LBL_APPROX_PAY_AMOUNT") + ": " + payableAmount);
        } else {
            payableAmountTxt.setVisibility(View.GONE);
            payableTxt.setVisibility(View.VISIBLE);

        }

        MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_SURGE"));
        btn_type2.setId(Utils.generateViewId());

        btn_type2.setOnClickListener(view -> {

            alertDialog_surgeConfirm.dismiss();
            handleRequest(data);
        });

        (dialogView.findViewById(R.id.tryLaterTxt)).setOnClickListener(view -> {
            alertDialog_surgeConfirm.dismiss();
            closeRequestDialog(false);
            isdelivernow = false;
            isdeliverlater = false;

        });


        alertDialog_surgeConfirm = builder.create();
        alertDialog_surgeConfirm.setCancelable(false);
        alertDialog_surgeConfirm.setCanceledOnTouchOutside(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog_surgeConfirm);
        }

        alertDialog_surgeConfirm.show();
    }

    public void setDefaultView() {

        try {
            super.onPostResume();
        } catch (Exception e) {

        }

        try {
            cabRquestType = Utils.CabReqType_Now;


            if (mainHeaderFrag != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.headerContainer, mainHeaderFrag).commit();
            }


            if (mainHeaderFrag != null) {
                mainHeaderFrag.releaseAddressFinder();
            }


            configDestinationMode(false);
            userLocBtnImgView.performClick();
            Utils.runGC();

            try {
                new CreateAnimation(dragView, getActContext(), R.anim.design_bottom_sheet_slide_in, 600, false).startAnimation();
            } catch (Exception e) {

            }


            if (loadAvailCabs != null) {
                loadAvailCabs.setTaskKilledValue(false);
                loadAvailCabs.onResumeCalled();
            }
        } catch (Exception e) {

        }


    }

    public void setPanelHeight(int value) {
        if (Utils.IS_KIOSK_APP) {
            return;
        }

        resetMapView();
        map.getView().requestLayout();

        if (userLocBtnImgView != null && cabSelectionFrag != null) {
            resetUserLocBtnView();
            userLocBtnImgView.requestLayout();
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

        if (isufxbackview) {
            return;
        }

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

        if (Utils.IS_KIOSK_APP) {
            if (mainHeaderFrag != null && loadAvailCabs != null && loadAvailCabs.listOfDrivers != null && loadAvailCabs.listOfDrivers.size() > 0) {
                if (mainHeaderFrag.isroutefound) {
                    if (loadAvailCabs.isAvailableCab) {
                        if (!timeval.equalsIgnoreCase("\n" + "--")) {
                            noCabAvail = true;
                        }
                    }
                }
            }
        }

        if (cabSelectionFrag != null) {
            cabSelectionFrag.setLabels(false);
        }
    }

    public void onMapCameraChanged() {
        if (cabSelectionFrag != null) {

            if (loadAvailCabs != null) {
                loadAvailCabs.filterDrivers(true);
            }

            if (mainHeaderFrag != null) {
                if (isDestinationMode == true) {
                    mainHeaderFrag.setDestinationAddress(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
                } else {
                    mainHeaderFrag.setPickUpAddress(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
                }
            }
        }
    }

    public void onAddressFound(String address) {
        if (cabSelectionFrag != null) {
            notifyCabsAvailable();
            if (mainHeaderFrag != null) {

                if (isDestinationMode == true) {
                    mainHeaderFrag.setDestinationAddress(address);
                } else {
                    mainHeaderFrag.setPickUpAddress(address);
                }
            }

        } else {
            if (isUserLocbtnclik && mainHeaderFrag != null) {
                isUserLocbtnclik = false;
                mainHeaderFrag.setPickUpAddress(address);
            }
        }


    }

    public void setDestinationPoint(String destLocLatitude, String destLocLongitude, String destAddress, boolean isDestinationAdded) {

        if (destLocation == null) {
            destLocation = new Location("dest");
        }
        destLocation.setLatitude(GeneralFunctions.parseDoubleValue(0.0, destLocLatitude));
        destLocation.setLongitude(GeneralFunctions.parseDoubleValue(0.0, destLocLongitude));


        this.isDestinationAdded = isDestinationAdded;
        this.destLocLatitude = destLocLatitude;
        this.destLocLongitude = destLocLongitude;
        this.destAddress = destAddress;
    }

    public boolean getDestinationStatus() {
        return this.isDestinationAdded;
    }

    public String getDestLocLatitude() {
        return this.destLocLatitude;
    }

    public String getDestLocLongitude() {
        return this.destLocLongitude;
    }

    public String getDestAddress() {
        return this.destAddress;
    }

    public void setCashSelection(boolean isCashSelected) {
        this.isCashSelected = isCashSelected;
        if (loadAvailCabs != null) {
            loadAvailCabs.changeCabs();
        }
    }

    public String getCabReqType() {
        return this.cabRquestType;
    }

    public void setCabReqType(String cabRquestType) {
        this.cabRquestType = cabRquestType;
    }


    public void requestPickUp() {


        if (!isWalletPopupFirst) {
            if (generalFunc.getJsonValueStr("eWalletBalanceAvailable", obj_userProfile).equalsIgnoreCase("Yes")) {


                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                generateAlert.setCancelable(false);
                generateAlert.setBtnClickList(btn_id -> {

                    if (btn_id == 1) {
                        eWalletDebitAllow = "Yes";
                        isWalletPopupFirst = true;
                        requestPickUp();
                    } else {
                        isWalletPopupFirst = true;
                        eWalletDebitAllow = "No";
                        requestPickUp();

                    }

                });


                if (getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) {

                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_WALLET_BALANCE_EXIST_RIDE").replace("####", generalFunc.getJsonValueStr("user_available_balance", obj_userProfile)));
                } else if (getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_WALLET_BALANCE_EXIST_JOB").replace("####", generalFunc.getJsonValueStr("user_available_balance", obj_userProfile)));
                } else {
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_WALLET_BALANCE_EXIST_DELIVERY").replace("####", generalFunc.getJsonValueStr("user_available_balance", obj_userProfile)));
                }
                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
                generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
                generateAlert.showAlertBox();

                return;
            }
        }
        isWalletPopupFirst = false;

        setLoadAvailCabTaskValue(true);
        requestNearestCab = new RequestNearestCab(getActContext(), generalFunc);
        requestNearestCab.run();

        String driverIds = getAvailableDriverIds();

        JSONObject cabRequestedJson = new JSONObject();
        try {
            cabRequestedJson.put("Message", "CabRequested");
            cabRequestedJson.put("sourceLatitude", "" + getPickUpLocation().getLatitude());
            cabRequestedJson.put("sourceLongitude", "" + getPickUpLocation().getLongitude());
            cabRequestedJson.put("PassengerId", generalFunc.getMemberId());
            cabRequestedJson.put("PName", generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                    + generalFunc.getJsonValue("vLastName", obj_userProfile));
            cabRequestedJson.put("PPicName", generalFunc.getJsonValueStr("vImgName", obj_userProfile));
            cabRequestedJson.put("PFId", generalFunc.getJsonValueStr("vFbId", obj_userProfile));
            cabRequestedJson.put("PRating", generalFunc.getJsonValueStr("vAvgRating", obj_userProfile));
            cabRequestedJson.put("PPhone", generalFunc.getJsonValueStr("vPhone", obj_userProfile));
            cabRequestedJson.put("PPhoneC", generalFunc.getJsonValueStr("vPhoneCode", obj_userProfile));
            cabRequestedJson.put("REQUEST_TYPE", getCurrentCabGeneralType());
            if (getDestinationStatus() == true) {
                cabRequestedJson.put("destLatitude", "" + getDestLocLatitude());
                cabRequestedJson.put("destLongitude", "" + getDestLocLongitude());
            } else {
                cabRequestedJson.put("destLatitude", "");
                cabRequestedJson.put("destLongitude", "");
            }


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!generalFunc.getJsonValue("Message", cabRequestedJson.toString()).equals("")) {
            requestNearestCab.setRequestData(driverIds, cabRequestedJson.toString());

            if (DRIVER_REQUEST_METHOD.equals("All")) {
                sendReqToAll(driverIds, cabRequestedJson.toString());
            } else if (DRIVER_REQUEST_METHOD.equals("Distance") || DRIVER_REQUEST_METHOD.equals("Time")) {
                sendReqByDist(driverIds, cabRequestedJson.toString());
            } else {
                sendReqToAll(driverIds, cabRequestedJson.toString());
            }
        }


    }

    public void sendReqToAll(String driverIds, String cabRequestedJson) {
        isreqnow = true;
        sendRequestToDrivers(driverIds, cabRequestedJson);
        if (allCabRequestTask != null) {
            allCabRequestTask.stopRepeatingTask();
            allCabRequestTask = null;
        }

        int interval = generalFunc.parseIntegerValue(30, generalFunc.getJsonValue("RIDER_REQUEST_ACCEPT_TIME", generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));

        allCabRequestTask = new RecurringTask((interval + 5) * 1000);
        allCabRequestTask.startRepeatingTask();
        allCabRequestTask.setTaskRunListener(() -> {
            setRetryReqBtn(true);
            allCabRequestTask.stopRepeatingTask();
        });

    }

    public void sendReqByDist(String driverIds, String cabRequestedJson) {
        if (sendNotificationToDriverByDist == null) {
            sendNotificationToDriverByDist = new SendNotificationsToDriverByDist(driverIds, cabRequestedJson);
        } else {
            sendNotificationToDriverByDist.startRepeatingTask();
        }
    }

    public void setRetryReqBtn(boolean isVisible) {
        if (isVisible == true) {
            if (requestNearestCab != null) {
                requestNearestCab.setVisibilityOfRetryArea(View.VISIBLE);
            }
        } else {
            if (requestNearestCab != null) {
                requestNearestCab.setVisibilityOfRetryArea(View.GONE);
            }
        }
    }

    public void retryReqBtnPressed(String driverIds, String cabRequestedJson) {

        if (DRIVER_REQUEST_METHOD.equals("All")) {
            sendReqToAll(driverIds, cabRequestedJson.toString());
        } else if (DRIVER_REQUEST_METHOD.equals("Distance") || DRIVER_REQUEST_METHOD.equals("Time")) {
            sendReqByDist(driverIds, cabRequestedJson.toString());
        } else {
            sendReqToAll(driverIds, cabRequestedJson.toString());
        }

        setRetryReqBtn(false);
    }

    public void setLoadAvailCabTaskValue(boolean value) {
        if (loadAvailCabs != null) {
            loadAvailCabs.setTaskKilledValue(value);
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

    public String getAvailableDriverIds() {
        String driverIds = "";

        if (currentLoadedDriverList == null) {
            return driverIds;
        }

        ArrayList<HashMap<String, String>> finalLoadedDriverList = new ArrayList<HashMap<String, String>>();
        finalLoadedDriverList.addAll(currentLoadedDriverList);

        if (DRIVER_REQUEST_METHOD.equals("Distance")) {
            Collections.sort(finalLoadedDriverList, new HashMapComparator("DIST_TO_PICKUP"));
        }

        for (int i = 0; i < finalLoadedDriverList.size(); i++) {
            String iDriverId = finalLoadedDriverList.get(i).get("driver_id");

            driverIds = driverIds.equals("") ? iDriverId : (driverIds + "," + iDriverId);
        }

        return driverIds;
    }

    public void sendRequestToDrivers(String driverIds, String cabRequestedJson) {

        HashMap<String, Object> requestCabData = new HashMap<String, Object>();
        requestCabData.put("type", "sendRequestToDrivers");
        requestCabData.put("message", cabRequestedJson);
        requestCabData.put("userId", generalFunc.getMemberId());
        requestCabData.put("CashPayment", "" + isCashSelected);

        requestCabData.put("PickUpAddress", getPickUpLocationAddress());


        requestCabData.put("vTollPriceCurrencyCode", tollcurrancy);
        requestCabData.put("eWalletDebitAllow", eWalletDebitAllow);


        String tollskiptxt = "";

        if (istollIgnore) {
            tollamount = 0;
            tollskiptxt = "Yes";

        } else {
            tollskiptxt = "No";
        }
        requestCabData.put("fTollPrice", tollamount + "");
        requestCabData.put("eTollSkipped", tollskiptxt);

        requestCabData.put("eType", getCurrentCabGeneralType());
        requestCabData.put("driverIds", driverIds);
        requestCabData.put("SelectedCarTypeID", "" + selectedCabTypeId);


        requestCabData.put("DestLatitude", getDestLocLatitude());
        requestCabData.put("DestLongitude", getDestLocLongitude());
        requestCabData.put("DestAddress", getDestAddress());


        if (isUfx) {
            requestCabData.put("PickUpLatitude", getIntent().getStringExtra("latitude"));
            requestCabData.put("PickUpLongitude", getIntent().getStringExtra("longitude"));
        } else {
            requestCabData.put("PickUpLatitude", "" + getPickUpLocation().getLatitude());
            requestCabData.put("PickUpLongitude", "" + getPickUpLocation().getLongitude());
        }


        if (cabSelectionFrag != null) {
            requestCabData.put("PromoCode", "");
        }


        ServerTask exeWebServer = ApiHandler.execute(getActContext(), requestCabData, false, generalFunc, responseString -> {


            if (responseString != null && !responseString.equals("")) {

                generalFunc.sendHeartBeat();

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == false) {
                    Bundle bn = new Bundle();
                    bn.putString("msg", "" + generalFunc.getJsonValue(Utils.message_str, responseString));

                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                    if (message.equals("SESSION_OUT")) {
                        closeRequestDialog(false);
                        MyApp.getInstance().notifySessionTimeOut();
                        Utils.runGC();
                        return;
                    }

                    if (message.equals("NO_CARS") && !DRIVER_REQUEST_METHOD.equalsIgnoreCase("ALL") && sendNotificationToDriverByDist != null) {
                        sendNotificationToDriverByDist.incTask();
                        return;

                    }
                    if (message.equals("NO_CARS") || message.equals("LBL_PICK_DROP_LOCATION_NOT_ALLOW")
                            || message.equals("LBL_DROP_LOCATION_NOT_ALLOW") || message.equals("LBL_PICKUP_LOCATION_NOT_ALLOW")) {
                        closeRequestDialog(false);
                        String messageLabel = "";

                        if (getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                            messageLabel = "LBL_NO_CAR_AVAIL_TXT";

                        } else if (getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                            messageLabel = "LBL_NO_PROVIDERS_AVAIL_TXT";
                        } else {
                            messageLabel = "LBL_NO_CARRIERS_AVAIL_TXT";
                        }
                        buildMessage(generalFunc.retrieveLangLBl("", message.equals("NO_CARS") ? messageLabel : message),
                                generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), false);
                        if (loadAvailCabs != null) {
                            isufxbackview = false;
                            loadAvailCabs.onResumeCalled();
                        }

                    } else if (message.equalsIgnoreCase("LBL_HOTEL_DISABLED")) {
                        closeRequestDialog(false);
                        buildMessage(generalFunc.retrieveLangLBl("", message), generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), false);
                    } else if (message.equals(Utils.GCM_FAILED_KEY) || message.equals(Utils.APNS_FAILED_KEY)) {
                        releaseScheduleNotificationTask();
                        generalFunc.restartApp();
                    }

                }
            } else {
                if (reqSentErrorDialog != null) {
                    reqSentErrorDialog.closeAlertBox();
                    reqSentErrorDialog = null;
                }


                InternetConnection intConnection = new InternetConnection(getActContext());

                reqSentErrorDialog = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", intConnection.isNetworkConnected() ? "LBL_TRY_AGAIN_TXT" : "LBL_NO_INTERNET_TXT"), generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"), buttonId -> {
                    if (buttonId == 1) {
                        sendRequestToDrivers(driverIds, cabRequestedJson);
                    } else {
                        //Negative
                        closeRequestDialog(true);

                        MyApp.getInstance().restartWithGetDataApp();
                    }
                });
            }
        });
        exeWebServer.setCancelAble(false);

        generalFunc.sendHeartBeat();
    }


    public void closeRequestDialog(boolean isSetDefault) {
        if (requestNearestCab != null) {
            requestNearestCab.dismissDialog();
        }

        if (loadAvailCabs != null) {
            loadAvailCabs.selectProviderId = "";

        }

        setLoadAvailCabTaskValue(false);

        releaseScheduleNotificationTask();

        if (isSetDefault == true) {
            setDefaultView();
        }

    }

    public void releaseScheduleNotificationTask() {
        if (allCabRequestTask != null) {
            allCabRequestTask.stopRepeatingTask();
            allCabRequestTask = null;
        }

        if (sendNotificationToDriverByDist != null) {
            sendNotificationToDriverByDist.stopRepeatingTask();
            sendNotificationToDriverByDist = null;
        }
    }

    public void buildMessage(String message, String positiveBtn, final boolean isRestart) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            generateAlert.closeAlertBox();
            if (isRestart == true) {
                generalFunc.restartApp();
            } else if (!TextUtils.isEmpty(tripId) && eTripType.equals(Utils.eType_Multi_Delivery)) {
                MyApp.getInstance().restartWithGetDataApp(tripId);

            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);
        generateAlert.showAlertBox();
    }

    public void onGcmMessageArrived(String message) {

        String driverMsg = generalFunc.getJsonValue("Message", message);
        String eType = generalFunc.getJsonValue("eType", message);

        if (!eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            if (!assignedTripId.equals("") && !generalFunc.getJsonValue("iTripId", message).equalsIgnoreCase("") && !generalFunc.getJsonValue("iTripId", message).equalsIgnoreCase(assignedTripId)) {
                return;
            }
        }
        currentTripId = generalFunc.getJsonValue("iTripId", message);
        if (driverMsg.equals("CabRequestAccepted")) {
            if (isDriverAssigned == true) {
                return;
            }

            if (generalFunc.getJsonValue("eSystem", message) != null && generalFunc.getJsonValue("eSystem", message).equalsIgnoreCase("DeliverAll")) {
                generalFunc.showGeneralMessage("", generalFunc.getJsonValue("vTitle", message));
                return;
            }

            isDriverAssigned = true;
            userLocBtnImgView.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) (userLocBtnImgView).getLayoutParams();
            params.bottomMargin = Utils.dipToPixels(getActContext(), 200);
            assignedDriverId = generalFunc.getJsonValue("iDriverId", message);
            assignedTripId = generalFunc.getJsonValue("iTripId", message);

            generalFunc.removeValue(Utils.DELIVERY_DETAILS_KEY);

            generalFunc.resetStoredDetails();

            boolean isRestart = getIntent().getBooleanExtra("isRestart", true);
            if (generalFunc.isJSONkeyAvail("iCabBookingId", message) == true && !generalFunc.getJsonValue("iCabBookingId", message).trim().equals("")) {
                generalFunc.restartApp();
            } else if (Utils.IS_KIOSK_APP) {

                Bundle bn = new Bundle();
                bn.putString("message", message);
                bn.putString("vLogo1", selectedCabTypeDetail.get("vLogo1"));
                bn.putString("iVehicleTypeId", selectedCabTypeDetail.get("iVehicleTypeId"));
                new ActUtils(getActContext()).startActWithData(KioskBookingDetailsActivity.class, bn);
            }

            tripStatus = "Assigned";

        } else if (driverMsg.equals("TripEnd") && !Utils.IS_KIOSK_APP) {
            if (isDriverAssigned == false) {
                return;
            }

            if (isTripEnded == true && isDriverAssigned == false) {
                generalFunc.restartApp();
                return;
            }

            if (isTripEnded) {
                return;
            }

            tripStatus = "TripEnd";

        } else if (driverMsg.equals("TripStarted") && !Utils.IS_KIOSK_APP) {
            try {
                if (isDriverAssigned == false) {
                    return;
                }

                if (isDriverAssigned == false && isTripStarted == true) {
                    generalFunc.restartApp();
                    return;
                }

                if (isTripStarted) {
                    return;
                }


                // Change Status as per trip
                JSONObject tripDetailJson = generalFunc.getJsonObject("TripDetails", obj_userProfile);

                if (tripDetailJson != null && !generalFunc.getJsonValueStr("iDriverId", tripDetailJson).equalsIgnoreCase(generalFunc.getJsonValue("iDriverId", message)) && eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                    return;
                }

                tripStatus = "TripStarted";


                isTripStarted = true;

                userLocBtnImgView.performClick();
            } catch (Exception e) {

            }


        } else if (driverMsg.equals("DestinationAdded")) {
            if (isDriverAssigned == false) {
                return;
            }

            // Change Status as per trip
            JSONObject tripDetailJson = generalFunc.getJsonObject("TripDetails", obj_userProfile);

            if (tripDetailJson != null && !generalFunc.getJsonValueStr("iDriverId", tripDetailJson).equalsIgnoreCase(generalFunc.getJsonValue("iDriverId", message))) {
                return;
            }

            LocalNotification.dispatchLocalNotification(getActContext(), generalFunc.retrieveLangLBl("Destination is added by driver.", "LBL_DEST_ADD_BY_DRIVER"), true);

            buildMessage(generalFunc.retrieveLangLBl("Destination is added by driver.", "LBL_DEST_ADD_BY_DRIVER"), generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), false);

            String destLatitude = generalFunc.getJsonValue("DLatitude", message);
            String destLongitude = generalFunc.getJsonValue("DLongitude", message);
            String destAddress = generalFunc.getJsonValue("DAddress", message);
            setDestinationPoint(destLatitude, destLongitude, destAddress, true);

        } else if (driverMsg.equals("TripCancelledByDriver")) {

            if (!generalFunc.getJsonValue("eType", message).equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                if (isDriverAssigned == false) {
                    generalFunc.restartApp();
                    return;
                }
            }

            if (tripStatus.equals("TripCanelled")) {
                return;
            }

            tripStatus = "TripCanelled";
        }
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


        if (!isufxbackview) {
            if (currentLoadedDriverList != null) {

                AppService.getInstance().executeService(new EventInformation.EventInformationBuilder().setChanelList(getDriverLocationChannelList()).build(), AppService.Event.SUBSCRIBE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {

            if (mainHeaderFrag != null) {
                mainHeaderFrag.releaseResources();
                mainHeaderFrag = null;
            }

            releaseScheduleNotificationTask();


            if (gMap != null) {
                gMap.clear();
                gMap = null;
            }

            Utils.runGC();

        } catch (Exception e) {

        }

    }


    @SuppressLint("MissingPermission")
    public void pubNubMsgArrived(final String message) {

        currentTripId = generalFunc.getJsonValue("iTripId", message);
        runOnUiThread(() -> {

            String msgType = generalFunc.getJsonValue("MsgType", message);

            if (msgType.equals("TripEnd") && !Utils.IS_KIOSK_APP) {

                if (isDriverAssigned == false) {
                    generalFunc.restartApp();
                    return;
                }
            }
            if (msgType.equals("LocationUpdate")) {
                if (loadAvailCabs == null) {
                    return;
                }

                String iDriverId = generalFunc.getJsonValue("iDriverId", message);
                String vLatitude = generalFunc.getJsonValue("vLatitude", message);
                String vLongitude = generalFunc.getJsonValue("vLongitude", message);

                Marker driverMarker = getDriverMarkerOnPubNubMsg(iDriverId, false);

                LatLng driverLocation_update = new LatLng(generalFunc.parseDoubleValue(0.0, vLatitude),
                        generalFunc.parseDoubleValue(0.0, vLongitude));
                Location driver_loc = new Location("gps");
                driver_loc.setLatitude(driverLocation_update.latitude);
                driver_loc.setLongitude(driverLocation_update.longitude);

                if (driverMarker != null) {
                    float rotation = (float) SphericalUtil.computeHeading(driverMarker.getPosition(), driverLocation_update);

                    MarkerAnim.animateMarker(driverMarker, gMap, driver_loc, rotation, 1200);
                }

            } else if (msgType.equals("TripRequestCancel")) {

                tripStatus = "TripCanelled";
                if (TextUtils.isEmpty(tripId) && eTripType.equals(Utils.CabGeneralType_Deliver) && getCurrentCabGeneralType().equals(Utils.CabGeneralType_Deliver)) {
                    if (tripId.equalsIgnoreCase(currentTripId)) {
                        if (DRIVER_REQUEST_METHOD.equals("Distance") || DRIVER_REQUEST_METHOD.equals("Time")) {
                            if (sendNotificationToDriverByDist != null) {
                                sendNotificationToDriverByDist.incTask();
                            }
                        }
                    }
                } else {
                    if (DRIVER_REQUEST_METHOD.equals("Distance") || DRIVER_REQUEST_METHOD.equals("Time")) {
                        if (sendNotificationToDriverByDist != null) {
                            sendNotificationToDriverByDist.incTask();
                        }
                    }
                }
            } else if (msgType.equals("LocationUpdateOnTrip") && !Utils.IS_KIOSK_APP) {

                if (!isDriverAssigned) {
                    return;
                }

                if (generalFunc.checkLocationPermission(true)) {
                    getMap().setMyLocationEnabled(false);
                }
                // Change Status as per trip
                JSONObject tripDetailJson = generalFunc.getJsonObject("TripDetails", obj_userProfile);

                if (tripDetailJson != null && !generalFunc.getJsonValueStr("iDriverId", tripDetailJson).equalsIgnoreCase(generalFunc.getJsonValue("iDriverId", message))) {
                    return;
                }
            } else if (msgType.equals("DriverArrived") && !Utils.IS_KIOSK_APP) {

                if (isDriverAssigned == false) {
                    generalFunc.restartApp();
                    return;
                }

                // Change Status as per trip
                JSONObject tripDetailJson = generalFunc.getJsonObject("TripDetails", obj_userProfile);

                if (tripDetailJson != null && !generalFunc.getJsonValueStr("iDriverId", tripDetailJson).equalsIgnoreCase(generalFunc.getJsonValue("iDriverId", message))) {
                    return;
                }

                tripStatus = "DriverArrived";
                userLocBtnImgView.performClick();

            } else {

                onGcmMessageArrived(message);

            }

        });

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

            if (status) {
                if (requestNearestCab != null) {
                    requestNearestCab.dismissDialog();
                }

                releaseScheduleNotificationTask();
            }


            if (cabSelectionFrag != null) {

                PolyLineAnimator.getInstance().stopRouteAnim();
                getSupportFragmentManager().beginTransaction().remove(cabSelectionFrag).commit();
                cabSelectionFrag.releaseResources();
                cabSelectionFrag = null;

                gMap.clear();


                configDestinationMode(false);

                isRental = false;
                if (loadAvailCabs != null) {
                    loadAvailCabs.changeCabs();
                }

                if (isMenuImageShow) {
                    mainHeaderFrag.menuImgView.setVisibility(View.VISIBLE);
                    mainHeaderFrag.backImgView.setVisibility(View.GONE);
                }

                mainHeaderFrag.handleDestAddIcon();
                cabTypesArrList.clear();
                mainHeaderFrag.setDefaultView();
                if (loadAvailCabs != null) {
                    selectedCabTypeId = loadAvailCabs.getFirstCarTypeID();
                }
                resetUserLocBtnView();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) (userLocBtnImgView).getLayoutParams();
                params.bottomMargin = Utils.dipToPixels(getActContext(), 10);
                userLocBtnImgView.requestLayout();

                if (mainHeaderFrag != null) {
                    mainHeaderFrag.releaseAddressFinder();
                }

                resetMapView();

                if (pickUpLocation != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.pickUpLocation.getLatitude(), this.pickUpLocation.getLongitude()))
                            .zoom(Utils.defaultZomLevel).build();
                    getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else if (userLocation != null) {
                    getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraForUserPosition()));
                }

                return;

            }
            super.onBackPressed();

        } catch (Exception e) {
            Log.e("Exception", "::" + e.toString());
        }
    }


    public Context getActContext() {
        return KioskBookNowActivity.this;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, 1, 0, "" + generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        menu.add(0, 2, 0, "" + generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                LatLng placeLocation = place.getLatLng();

                setDestinationPoint(placeLocation.latitude + "", placeLocation.longitude + "", place.getAddress().toString(), true);
                mainHeaderFrag.setDestinationAddress(place.getAddress().toString());

                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, Utils.defaultZomLevel);

                if (gMap != null) {
                    gMap.clear();
                    gMap.moveCamera(cu);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);


                generalFunc.showMessage(generalFunc.getCurrentView(KioskBookNowActivity.this),
                        status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }


        } else if (requestCode == Utils.REQUEST_CODE_GPS_ON) {


        } else if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null && gMap != null) {

            if (data.getStringExtra("Address") != null) {
                pickUp_tmpAddress = data.getStringExtra("Address");
            }

            pickUp_tmpLatitude = generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude"));
            pickUp_tmpLongitude = generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude"));

            final Location location = new Location("gps");
            location.setLatitude(generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude")));
            location.setLongitude(generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude")));
            onLocationUpdate(location);

        } else if (requestCode == WEBVIEWPAYMENT) {
            if (data != null && data.getStringExtra("isPaymentSuccess").equalsIgnoreCase("Yes")){
                BounceAnimation.setBounceAnimation(getActContext(), btn_book_now_type2);
                BounceAnimation.setBounceAnimListener(this);
            }
        }
    }


    public void getTollcostValue(final String driverIds, final String cabRequestedJson, final Intent data) {

        if (isFixFare) {
            setDeliverOrRideReq(driverIds, cabRequestedJson, data);
            return;
        }


        if (cabSelectionFrag != null) {
            if (cabSelectionFrag.isSkip) {
                setDeliverOrRideReq(driverIds, cabRequestedJson, data);
                return;
            }
        }

        // Toll Disabled fro MultiDelivery

        HashMap<String, String> mapData = new HashMap<>();
        mapData.put(Utils.ENABLE_TOLL_COST, "");
        mapData.put(Utils.TOLL_COST_APP_ID, "");
        mapData.put(Utils.TOLL_COST_APP_CODE, "");
        mapData = generalFunc.retrieveValue(mapData);


        if (mapData.get(Utils.ENABLE_TOLL_COST).equalsIgnoreCase("Yes")) {

            String wayPoints = "";
            wayPoints = "&waypoint1=" + getDestLocLatitude() + "," + getDestLocLongitude();

            String url = CommonUtilities.TOLLURL + mapData.get(Utils.TOLL_COST_APP_ID)
                    + "&app_code=" + mapData.get(Utils.TOLL_COST_APP_CODE) + "&waypoint0=" + getPickUpLocation().getLatitude()
                    + "," + getPickUpLocation().getLongitude() + wayPoints + "&mode=fastest;car";

            ApiHandler.execute(getActContext(), url, true, true, generalFunc, responseString -> {


                if (responseString != null && !responseString.equals("")) {

                    if (generalFunc.getJsonValue("onError", responseString).equalsIgnoreCase("FALSE")) {
                        try {

                            String costs = generalFunc.getJsonValue("costs", responseString);
                            String currency = generalFunc.getJsonValue("currency", costs);
                            String details = generalFunc.getJsonValue("details", costs);
                            String tollCost = generalFunc.getJsonValue("tollCost", details);
                            if (!currency.equals("") && currency != null) {
                                tollcurrancy = currency;
                            }
                            tollamount = 0.0;
                            if (!tollCost.equals("") && tollCost != null && !tollCost.equals("0.0")) {
                                tollamount = generalFunc.parseDoubleValue(0.0, tollCost);
                            }


                            TollTaxDialog(driverIds, cabRequestedJson, data);


                        } catch (Exception e) {

                            TollTaxDialog(driverIds, cabRequestedJson, data);
                        }

                    } else {
                        TollTaxDialog(driverIds, cabRequestedJson, data);
                    }


                } else {
                    generalFunc.showError();
                }

            });


        } else {
            setDeliverOrRideReq(driverIds, cabRequestedJson, data);


        }

    }

    private void setDeliverOrRideReq(String driverIds, String cabRequestedJson, Intent data) {
        isreqnow = true;
        if (isreqnow) {
            isreqnow = false;
            requestPickUp();
        }
    }

    public void TollTaxDialog(final String driverIds, final String cabRequestedJson, final Intent data) {

        if (!isTollCostdilaogshow) {
            if (tollamount != 0.0 && tollamount != 0 && tollamount != 0.00) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());

                LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.dialog_tolltax, null);

                final MTextView tolltaxTitle = (MTextView) dialogView.findViewById(R.id.tolltaxTitle);
                final MTextView tollTaxMsg = (MTextView) dialogView.findViewById(R.id.tollTaxMsg);
                final MTextView tollTaxpriceTxt = (MTextView) dialogView.findViewById(R.id.tollTaxpriceTxt);
                final MTextView cancelTxt = (MTextView) dialogView.findViewById(R.id.cancelTxt);

                final CheckBox checkboxTolltax = (CheckBox) dialogView.findViewById(R.id.checkboxTolltax);

                checkboxTolltax.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    if (checkboxTolltax.isChecked()) {
                        istollIgnore = true;
                    } else {
                        istollIgnore = false;
                    }

                });


                MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
                int submitBtnId = Utils.generateViewId();
                btn_type2.setId(submitBtnId);
                btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"));
                btn_type2.setOnClickListener(v -> {
                    tolltax_dialog.dismiss();
                    isTollCostdilaogshow = true;
                    setDeliverOrRideReq(driverIds, cabRequestedJson, data);


                });


                builder.setView(dialogView);
                tolltaxTitle.setText(generalFunc.retrieveLangLBl("", "LBL_TOLL_ROUTE"));
                tollTaxMsg.setText(generalFunc.retrieveLangLBl("", "LBL_TOLL_PRICE_DESC"));

                tollTaxMsg.setText(generalFunc.retrieveLangLBl("", "LBL_TOLL_PRICE_DESC"));

                String payAmount = payableAmount;
                if (cabSelectionFrag != null && cabTypeList != null && cabTypeList.size() > 0 && cabTypeList.get(cabSelectionFrag.selpos).get("total_fare") != null && !cabTypeList.get(cabSelectionFrag.selpos).get("total_fare").equals("") && !cabTypeList.get(cabSelectionFrag.selpos).get("eRental").equals("Yes")) {

                    try {
                        payAmount = generalFunc.convertNumberWithRTL(cabTypeList.get(cabSelectionFrag.selpos).get("total_fare"));
                    } catch (Exception e) {

                    }


                }

                if (payAmount.equalsIgnoreCase("")) {
                    tollTaxpriceTxt.setText(generalFunc.retrieveLangLBl("Total toll price", "LBL_TOLL_PRICE_TOTAL") + ": " + tollcurrancy + " " + tollamount);
                } else {
                    tollTaxpriceTxt.setText(generalFunc.retrieveLangLBl(
                            "Current Fare", "LBL_CURRENT_FARE") + ": " + payAmount + "\n" + "+" + "\n" +
                            generalFunc.retrieveLangLBl("Total toll price", "LBL_TOLL_PRICE_TOTAL") + ": " + tollcurrancy + " " + tollamount);
                }

                checkboxTolltax.setText(generalFunc.retrieveLangLBl("", "LBL_IGNORE_TOLL_ROUTE"));
                cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

                cancelTxt.setOnClickListener(v -> {
                    tolltax_dialog.dismiss();
                    isreqnow = false;

                });


                tolltax_dialog = builder.create();
                if (generalFunc.isRTLmode() == true) {
                    generalFunc.forceRTLIfSupported(tolltax_dialog);
                }
                tolltax_dialog.setCancelable(false);
                tolltax_dialog.show();
            } else {
                setDeliverOrRideReq(driverIds, cabRequestedJson, data);
            }
        } else {
            setDeliverOrRideReq(driverIds, cabRequestedJson, data);

        }
    }


    private void moveToCurrentLoc() {
        if (!generalFunc.isLocationEnabled()) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please enable you GPS location service", "LBL_GPSENABLE_TXT"));
            return;
        }

        isUserLocbtnclik = true;

        if (Utils.IS_KIOSK_APP) {

            if (mainHeaderFrag != null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                if (mainHeaderFrag.sourceMarker != null) {
                    builder.include(mainHeaderFrag.sourceMarker.getPosition());
                }
                if (mainHeaderFrag.destMarker != null) {
                    builder.include(mainHeaderFrag.destMarker.getPosition());
                }
                float maxZoomLevel = gMap.getMaxZoomLevel();
                if (mainHeaderFrag.sourceMarker != null && mainHeaderFrag.destMarker != null && gMap != null) {
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    int width = metrics.widthPixels;
                    int height = metrics.heightPixels;
                    gMap.setMaxZoomPreference(maxZoomLevel);
                    int heightN = Utils.IS_KIOSK_APP ? Utils.dipToPixels(getActContext(), (int) (height * 0.30)) : Utils.dipToPixels(getActContext(), 300);
                    try {


                        gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width - Utils.dipToPixels(getActContext(), 80), (height - heightN), 0));
                    } catch (Exception e) {

                    }
                } else {
                    try {
                        CameraPosition cameraPosition = cameraForUserPosition();
                        if (cameraPosition != null) {
                            getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            if (mainHeaderFrag != null && mainHeaderFrag.getAddressFromLocation != null && userLocation != null) {
                                mainHeaderFrag.getAddressFromLocation.setLocation(userLocation.getLatitude(), userLocation.getLongitude());
                                mainHeaderFrag.getAddressFromLocation.execute();
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            } else {
                try {
                    CameraPosition cameraPosition = cameraForUserPosition();
                    if (cameraPosition != null) {
                        getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        if (mainHeaderFrag != null && mainHeaderFrag.getAddressFromLocation != null && userLocation != null) {
                            mainHeaderFrag.getAddressFromLocation.setLocation(userLocation.getLatitude(), userLocation.getLongitude());
                            mainHeaderFrag.getAddressFromLocation.execute();
                        }
                    }
                } catch (Exception e) {

                }
            }


        }

    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String
            geocodeobject) {

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

    public void setTotalFare(String totalfare, String distVal, String timeval) {
        fareValueTxt.setText(generalFunc.convertNumberWithRTL(totalfare));
        distance = distVal;
        time = timeval;
    }

    public class onGoogleMapCameraChangeList implements GoogleMap.OnCameraIdleListener {
        @Override
        public void onCameraIdle() {
            if (getAddressFromLocation == null) {
                return;
            }

            LatLng center = null;
            if (gMap != null && gMap.getCameraPosition() != null) {
                center = gMap.getCameraPosition().target;
            }

            if (center == null) {
                return;
            }

            if (!isAddressEnable) {
                setSourceAddress(center.latitude, center.longitude);
                onMapCameraChanged();
            } else {
                isAddressEnable = false;
            }
        }
    }

    public class setOnClickList implements View.OnClickListener, BounceAnimation.BounceAnimListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(getActContext());
            if (i == userLocBtnImgView.getId()) {
                moveToCurrentLoc();

            } else if (i == backImgView.getId()) {
                onBackPressed();
            } else if (i == infoimage.getId()) {
                Bundle bn = new Bundle();
                bn.putString("SelectedCar", selectedCabTypeDetail.get("iVehicleTypeId"));
                bn.putString("iUserId", generalFunc.getMemberId());
                bn.putString("distance", distance);
                bn.putString("time", time);
                bn.putString("PromoCode", "");
                if (selectedCabTypeDetail.get("eRental").equals("yes")) {
                    bn.putString("vVehicleType", selectedCabTypeDetail.get("vRentalVehicleTypeName"));
                } else {
                    bn.putString("vVehicleType", selectedCabTypeDetail.get("vVehicleType"));
                }
                bn.putBoolean("isSkip", false);
                if (pickUpLocation != null) {
                    bn.putString("picupLat", pickUpLocation.getLatitude() + "");
                    bn.putString("pickUpLong", pickUpLocation.getLongitude() + "");
                }
                if (destLocation != null) {

                    bn.putString("destLat", destLocation.getLatitude() + "");
                    bn.putString("destLong", destLocation.getLongitude() + "");
                    bn.putBoolean("isSkip", false);
                } else {
                    bn.putString("destLat", "");
                    bn.putString("destLong", "");
                    bn.putBoolean("isSkip", true);
                }

                if (isFixFare) {
                    bn.putBoolean("isFixFare", true);
                } else {
                    bn.putBoolean("isFixFare", false);
                }

                new ActUtils(getActContext()).startActWithData(FareBreakDownActivity.class, bn);

            } else if (i == btn_book_now_type2.getId()) {
                if (generalFunc.retrieveValue("ENABLE_CARD_IN_KIOSK").equalsIgnoreCase("Yes")) {
                    isCashSelected = false;
                    initPaymentWebView();
                } else {
                    isCashSelected = true;
                    BounceAnimation.setBounceAnimation(getActContext(), btn_book_now_type2);
                    BounceAnimation.setBounceAnimListener(this);
                }
            }
        }


        @Override
        public void onAnimationFinished(View view) {

            if (view == btn_book_now_type2) {

                if (getDestinationStatus() == false) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please add your destination location " +
                            "to deliver your package.", "LBL_ADD_DEST_MSG"));
                    return;
                }

                if (mainHeaderFrag != null && mainHeaderFrag.isCabsLoadedIsInProcess) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Route not found", "LBL_DEST_ROUTE_NOT_FOUND"));
                    return;
                }
                if ((currentLoadedDriverList != null && currentLoadedDriverList.size() < 1) || currentLoadedDriverList == null) {

                    buildNoCabMessage(generalFunc.retrieveLangLBl("", "LBL_NO_CARS_AVAIL_IN_TYPE"),
                            generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                    return;
                }


                if (mainHeaderFrag.isRouteFail) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Route not found", "LBL_DEST_ROUTE_NOT_FOUND"));
                    return;
                }

                setCabReqType(Utils.CabReqType_Now);


                if (cabRquestType.equalsIgnoreCase(Utils.CabReqType_Now)) {
                    continuePickUpProcess();
                } else {
                    checkSurgePrice(selectedTime, deliveryData);
                }
            }
        }
    }

    public class SendNotificationsToDriverByDist implements Runnable {

        String[] list_drivers_ids;
        String cabRequestedJson;

        int interval = generalFunc.parseIntegerValue(30, generalFunc.getJsonValueStr("RIDER_REQUEST_ACCEPT_TIME", obj_userProfile));

        int mInterval = (interval + 5) * 1000;

        int current_position_driver_id = 0;
        private Handler mHandler_sendNotification;

        public SendNotificationsToDriverByDist(String list_drivers_ids, String cabRequestedJson) {
            this.list_drivers_ids = list_drivers_ids.split(",");
            this.cabRequestedJson = cabRequestedJson;
            mHandler_sendNotification = new Handler();

            startRepeatingTask();
        }

        @Override
        public void run() {
            setRetryReqBtn(false);

            if ((current_position_driver_id + 1) <= list_drivers_ids.length) {
                sendRequestToDrivers(list_drivers_ids[current_position_driver_id], cabRequestedJson);
                current_position_driver_id = current_position_driver_id + 1;
                mHandler_sendNotification.postDelayed(this, mInterval);
            } else {
                setRetryReqBtn(true);
                stopRepeatingTask();
            }

        }


        public void stopRepeatingTask() {
            mHandler_sendNotification.removeCallbacks(this);
            mHandler_sendNotification.removeCallbacksAndMessages(null);
            current_position_driver_id = 0;
        }

        public void incTask() {
            mHandler_sendNotification.removeCallbacks(this);
            mHandler_sendNotification.removeCallbacksAndMessages(null);
            this.run();
        }

        public void startRepeatingTask() {
            stopRepeatingTask();

            this.run();
        }
    }
}
