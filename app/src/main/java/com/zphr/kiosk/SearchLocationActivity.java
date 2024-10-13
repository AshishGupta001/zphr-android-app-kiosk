package com.zphr.kiosk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.ParentActivity;
import com.adapter.files.PlacesAdapter;
import com.general.files.ActUtils;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.OnScrollTouchDelegate;
import com.general.files.RecurringTask;
import com.google.android.gms.maps.model.LatLng;
import com.service.handler.AppService;
import com.service.model.DataProvider;
import com.service.server.ServerTask;
import com.utils.Utils;
import com.view.MTextView;
import com.view.MyScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchLocationActivity extends ParentActivity implements PlacesAdapter.setRecentLocClickList, GetAddressFromLocation.AddressFound, GetLocationUpdates.LocationUpdates, OnScrollTouchDelegate {

    LinearLayout mapLocArea, sourceLocationView, destLocationView;
    ArrayList<HashMap<String, String>> recentLocList = new ArrayList<>();

    JSONArray SourceLocations_arr;
    JSONArray DestinationLocations_arr;
    MTextView placesTxt, recentLocHTxtView;

    String whichLocation = "";
    MTextView mapLocTxt, homePlaceTxt, homePlaceHTxt;
    MTextView workPlaceTxt, workPlaceHTxt;
    LinearLayout homeLocArea, workLocArea;

    ImageView homeActionImgView, workActionImgView;

    MTextView cancelTxt;

    RecyclerView placesRecyclerView;
    EditText searchTxt;

    // MainActivity mainAct;
    ArrayList<HashMap<String, String>> placelist;
    PlacesAdapter placesAdapter;
    ImageView imageCancel;

    MTextView noPlacedata;
    InternetConnection intCheck;
    LinearLayout placearea;
    LinearLayout myLocationarea;
    LinearLayout destinationLaterArea;
    MTextView destHTxtView;

    GetAddressFromLocation getAddressFromLocation;


    double currentLat = 0.0;
    double currentLong = 0.0;

    MTextView mylocHTxtView;

    LinearLayout placesarea;

    private boolean isFirstLocation = true;
    boolean isDriverAssigned;
    ImageView googleimagearea;
    MyScrollView dataArea;

    String session_token = "";
    RecurringTask sessionTokenFreqTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        isDriverAssigned = getIntent().getBooleanExtra("isDriverAssigned", false);

        intCheck = new InternetConnection(getActContext());


        sourceLocationView = (LinearLayout) findViewById(R.id.sourceLocationView);
        placesarea = (LinearLayout) findViewById(R.id.placesarea);
        dataArea = (MyScrollView) findViewById(R.id.dataArea);
        dataArea.setTouchDelegate(this);


        mapLocArea = (LinearLayout) findViewById(R.id.mapLocArea);
        addToClickHandler(mapLocArea);
        mapLocTxt = (MTextView) findViewById(R.id.mapLocTxt);

        destLocationView = (LinearLayout) findViewById(R.id.destLocationView);
        homePlaceTxt = (MTextView) findViewById(R.id.homePlaceTxt);
        homePlaceHTxt = (MTextView) findViewById(R.id.homePlaceHTxt);
        workPlaceTxt = (MTextView) findViewById(R.id.workPlaceTxt);
        workPlaceHTxt = (MTextView) findViewById(R.id.workPlaceHTxt);
        placesTxt = (MTextView) findViewById(R.id.locPlacesTxt);
        recentLocHTxtView = (MTextView) findViewById(R.id.recentLocHTxtView);
        cancelTxt = (MTextView) findViewById(R.id.cancelTxt);

        cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        placesarea = (LinearLayout) findViewById(R.id.placesarea);
        placesRecyclerView = (RecyclerView) findViewById(R.id.placesRecyclerView);
        placesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Utils.hideKeyboard(getActContext());
            }
        });


        searchTxt = (EditText) findViewById(R.id.searchTxt);
        searchTxt.setHint(generalFunc.retrieveLangLBl("Search", "LBL_Search"));
        addToClickHandler(searchTxt);
        imageCancel = (ImageView) findViewById(R.id.imageCancel);
        noPlacedata = (MTextView) findViewById(R.id.noPlacedata);
        addToClickHandler(noPlacedata);
        myLocationarea = (LinearLayout) findViewById(R.id.myLocationarea);
        mylocHTxtView = (MTextView) findViewById(R.id.mylocHTxtView);

        homeActionImgView = (ImageView) findViewById(R.id.homeActionImgView);
        workActionImgView = (ImageView) findViewById(R.id.workActionImgView);
        homeLocArea = (LinearLayout) findViewById(R.id.homeLocArea);
        workLocArea = (LinearLayout) findViewById(R.id.workLocArea);
        placearea = (LinearLayout) findViewById(R.id.placearea);
        destinationLaterArea = (LinearLayout) findViewById(R.id.destinationLaterArea);
        destHTxtView = (MTextView) findViewById(R.id.destHTxtView);

        googleimagearea = (ImageView) findViewById(R.id.googleimagearea);

        addToClickHandler(homeLocArea);
        addToClickHandler(workLocArea);
        addToClickHandler(placesTxt);
        addToClickHandler(cancelTxt);
        addToClickHandler(imageCancel);


        addToClickHandler(destinationLaterArea);
        addToClickHandler(workActionImgView);
        addToClickHandler(homeActionImgView);
        addToClickHandler(myLocationarea);

        placelist = new ArrayList<>();

        setLabel();
        setWhichLocationAreaSelected(getIntent().getStringExtra("locationArea"));


        if (getIntent().hasExtra("eSystem")) {
            findViewById(R.id.placearea).setVisibility(View.GONE);
            myLocationarea.setVisibility(View.VISIBLE);
            if (generalFunc.getMemberId().equalsIgnoreCase("")) {
                placearea.setVisibility(View.GONE);
            } else {
                placearea.setVisibility(View.VISIBLE);
            }
        } else {
            if (Utils.IS_KIOSK_APP) {
                placearea.setVisibility(View.GONE);
                myLocationarea.setVisibility(View.GONE);
            }

        }
        if (getIntent().getStringExtra("locationArea").equalsIgnoreCase("dest")) {
            if ((getIntent().getStringExtra("type") != null && getIntent().getStringExtra("type").equalsIgnoreCase(Utils.CabGeneralType_Ride) && generalFunc.getJsonValueStr("APP_DESTINATION_MODE", obj_userProfile).equalsIgnoreCase("NONSTRICT") && !isDriverAssigned)) {
                destinationLaterArea.setVisibility(View.VISIBLE);
            }
        }

        if (getIntent().hasExtra("isFromMulti") || Utils.IS_KIOSK_APP) {
            destinationLaterArea.setVisibility(View.GONE);
        }

        searchTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideSoftKeyboard(searchTxt);
                return true;
            }
            return false;
        });

        searchTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    hideSoftKeyboard(searchTxt);
                } else {
                    showSoftKeyboard(searchTxt);
                }
            }
        });

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() >= 2) {
                    placesRecyclerView.setVisibility(View.VISIBLE);

                    if (getIntent().hasExtra("eSystem")) {
                        googleimagearea.setVisibility(View.VISIBLE);
                    }
                    placesarea.setVisibility(View.GONE);
                    if (session_token.trim().equalsIgnoreCase("")) {
                        session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis();
                        initializeSessionRegeneration();
                    }
                    getGooglePlaces(searchTxt.getText().toString());
                } else {

                    googleimagearea.setVisibility(View.GONE);

                    if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                        placesarea.setVisibility(View.VISIBLE);
                    }
                    placesRecyclerView.setVisibility(View.GONE);
                    noPlacedata.setVisibility(View.GONE);
                }

            }
        });

        if (getIntent().hasExtra("hideSetMapLoc")) {
            mapLocArea.setVisibility(View.GONE);
            placesarea.setVisibility(View.GONE);
        } else {
            mapLocArea.setVisibility(View.VISIBLE);
        }

        if (getIntent().hasExtra("eSystem") || Utils.IS_KIOSK_APP) {
            mapLocArea.setVisibility(View.GONE);
        }
        placesRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        placesRecyclerView.setLayoutManager(mLayoutManager);
//        placesRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        placesRecyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    public void initializeSessionRegeneration() {

        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        sessionTokenFreqTask = new RecurringTask(170000);
        sessionTokenFreqTask.setTaskRunListener(() -> session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis());

        sessionTokenFreqTask.startRepeatingTask();
    }

    public void showSoftKeyboard(EditText view) {
        if (view.requestFocus()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            InputMethodManager imm = (InputMethodManager)
                    this.getSystemService(getActContext().INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    public void hideSoftKeyboard(View view) {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(getActContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    void setLabel() {


        homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
        workPlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
        homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
        workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
        mapLocTxt.setText(generalFunc.retrieveLangLBl("Set location on map", "LBL_SET_LOC_ON_MAP"));

        placesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FAV_LOCATIONS"));
        recentLocHTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_RECENT_LOCATIONS"));


        mylocHTxtView.setText(generalFunc.retrieveLangLBl("I want services at my current location", "LBL_SERVICE_MY_LOCATION_HINT_INFO"));
        destHTxtView.setText(generalFunc.retrieveLangLBl("Enter destination later", "LBL_DEST_ADD_LATER"));

    }

    @Override
    public void itemRecentLocClick(int position) {

        //getSelectAddresLatLong(placelist.get(position).get("place_id"), placelist.get(position).get("description"));
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Place_id", placelist.get(position).get("Place_id"));
        hashMap.put("description", placelist.get(position).get("description"));
        hashMap.put("session_token", placelist.get(position).get("session_token"));


        String latitude = "";
        String longitude = "";
        if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {
            latitude = getIntent().getDoubleExtra("lat", 0.0) + "";
            longitude = getIntent().getDoubleExtra("long", 0.0) + "";
        }


        if (placelist.get(position).get("Place_id") == null || placelist.get(position).get("Place_id").equals("")) {
            HashMap<String, Object> data_dict = new HashMap<>();
            data_dict.put("ADDRESS", placelist.get(position).get("description"));
            data_dict.put("LATITUDE", GeneralFunctions.parseDoubleValue(0.0, placelist.get(position).get("latitude")));
            data_dict.put("LONGITUDE", GeneralFunctions.parseDoubleValue(0.0, placelist.get(position).get("longitude")));
            data_dict.put("RESPONSE_TYPE", AppService.Service.PLACE_DETAILS);
            handlePlaceDeailsRespose(data_dict);
        } else {
            hashMap.put("vServiceId", placelist.get(position).get("vServiceId"));

            AppService.getInstance().executeService(getActContext(), new DataProvider.DataProviderBuilder(latitude, longitude).setPlaceId(placelist.get(position).get("Place_id")).setServiceId(placelist.get(position).get("vServiceId")).setData_Str(placelist.get(position).get("description")).setToken(session_token).build(), AppService.Service.PLACE_DETAILS, data -> handlePlaceDeailsRespose(data)
            );
        }

    }

    private void handlePlaceDeailsRespose(HashMap<String, Object> data) {

        if (data.get("RESPONSE_TYPE") != null && data.get("RESPONSE_TYPE").toString().equalsIgnoreCase("FAIL")) {

            return;
        }

        Bundle bn = new Bundle();
        bn.putString("Address", data.get("ADDRESS").toString());
        bn.putString("Latitude", data.get("LATITUDE").toString());
        bn.putString("Longitude", data.get("LONGITUDE").toString());

        Utils.hideKeyboard(getActContext());

        new ActUtils(getActContext()).setOkResult(bn);


        finish();

    }


    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {


        Bundle bn = new Bundle();
        bn.putString("Address", address);
        bn.putString("Latitude", "" + latitude);
        bn.putString("Longitude", "" + longitude);
        bn.putBoolean("isSkip", false);

        if (getIntent().hasExtra("isFromMulti")) {
            bn.putBoolean("isFromMulti", true);
            bn.putInt("pos", getIntent().getIntExtra("pos", -1));
        }

        new ActUtils(getActContext()).setOkResult(bn);

        finish();

    }


    public void setWhichLocationAreaSelected(String locationArea) {
        if (locationArea != null) {
            this.whichLocation = locationArea;

            if (locationArea.equals("dest")) {
                destLocationView.setVisibility(View.VISIBLE);
                sourceLocationView.setVisibility(View.GONE);
                getRecentLocations("dest");
                checkPlaces(locationArea);

            } else if (locationArea.equals("source")) {
                destLocationView.setVisibility(View.GONE);
                sourceLocationView.setVisibility(View.VISIBLE);
                getRecentLocations("source");
                checkPlaces(locationArea);
            }
        }

    }

    public void checkPlaces(final String whichLocationArea) {

        HashMap<String, String> data = new HashMap<>();
        data.put("userHomeLocationAddress", "");
        data.put("userWorkLocationAddress", "");
        data = generalFunc.retrieveValue(data);

        String home_address_str = data.get("userHomeLocationAddress");
//        if(home_address_str.equalsIgnoreCase("")){
//            home_address_str = "----";
//        }
        String work_address_str = data.get("userWorkLocationAddress");
//        if(work_address_str.equalsIgnoreCase("")){
//            work_address_str = "----";
//        }

        if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {

            homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homePlaceHTxt.setText("" + home_address_str);
            homePlaceHTxt.setVisibility(View.VISIBLE);
            homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));
            homeActionImgView.setImageResource(R.mipmap.ic_edit);

        } else {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (work_address_str != null && !work_address_str.equalsIgnoreCase("")) {

            workPlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
            workPlaceHTxt.setText("" + work_address_str);
            workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            workPlaceTxt.setTextColor(getResources().getColor(R.color.gray));
            workPlaceHTxt.setVisibility(View.VISIBLE);
//            workPlaceTxt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img_edit, null);
            workPlaceHTxt.setTextColor(getResources().getColor(R.color.black));
            workActionImgView.setImageResource(R.mipmap.ic_edit);

        } else {
            workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
            workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
            workPlaceTxt.setTextColor(Color.parseColor("#909090"));
            workActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (home_address_str != null && home_address_str.equalsIgnoreCase("")) {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setText("----");

            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            homePlaceHTxt.setVisibility(View.VISIBLE);
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (work_address_str != null && work_address_str.equalsIgnoreCase("")) {
            workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
            workPlaceTxt.setText("----");

            workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            workPlaceTxt.setTextColor(Color.parseColor("#909090"));
            workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            workPlaceHTxt.setVisibility(View.VISIBLE);
            workActionImgView.setImageResource(R.mipmap.ic_pluse);
        }
    }

    private void getRecentLocations(final String whichView) {
        final LayoutInflater mInflater = (LayoutInflater)
                getActContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        DestinationLocations_arr = generalFunc.getJsonArray("DestinationLocations", obj_userProfile);
        SourceLocations_arr = generalFunc.getJsonArray("SourceLocations", obj_userProfile);

        if (DestinationLocations_arr != null || SourceLocations_arr != null) {

            if (whichView.equals("dest")) {

                if (destLocationView != null) {
                    destLocationView.removeAllViews();
                    recentLocList.clear();
                }
                for (int i = 0; i < DestinationLocations_arr.length(); i++) {
                    final View view = mInflater.inflate(R.layout.item_recent_loc_design, null);
                    JSONObject destLoc_obj = generalFunc.getJsonObject(DestinationLocations_arr, i);

                    MTextView recentAddrTxtView = (MTextView) view.findViewById(R.id.recentAddrTxtView);
                    LinearLayout recentAdapterView = (LinearLayout) view.findViewById(R.id.recentAdapterView);

                    final String tEndLat = generalFunc.getJsonValueStr("tEndLat", destLoc_obj);
                    final String tEndLong = generalFunc.getJsonValueStr("tEndLong", destLoc_obj);
                    final String tDaddress = generalFunc.getJsonValueStr("tDaddress", destLoc_obj);

                    recentAddrTxtView.setText(tDaddress);

                    HashMap<String, String> map = new HashMap<>();
                    map.put("tLat", tEndLat);
                    map.put("tLong", tEndLong);
                    map.put("taddress", tDaddress);

                    recentLocList.add(map);
                    recentAdapterView.setOnClickListener(view1 -> {
                        if (whichView != null) {
                            if (whichView.equals("dest")) {

                                Bundle bn = new Bundle();
                                bn.putString("Address", tDaddress);
                                bn.putString("Latitude", "" + tEndLat);
                                bn.putString("Longitude", "" + tEndLong);
                                bn.putBoolean("isSkip", false);

                                if (getIntent().hasExtra("isFromMulti")) {
                                    bn.putBoolean("isFromMulti", true);
                                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                                }

                                new ActUtils(getActContext()).setOkResult(bn);

                                finish();
                            }

                        } else {

                        }
                    });
                    destLocationView.addView(view);
                }
            } else {
                if (sourceLocationView != null) {
                    sourceLocationView.removeAllViews();
                    recentLocList.clear();
                }
                for (int i = 0; i < SourceLocations_arr.length(); i++) {

                    final View view = mInflater.inflate(R.layout.item_recent_loc_design, null);
                    JSONObject loc_obj = generalFunc.getJsonObject(SourceLocations_arr, i);

                    MTextView recentAddrTxtView = (MTextView) view.findViewById(R.id.recentAddrTxtView);
                    LinearLayout recentAdapterView = (LinearLayout) view.findViewById(R.id.recentAdapterView);

                    final String tStartLat = generalFunc.getJsonValueStr("tStartLat", loc_obj);
                    final String tStartLong = generalFunc.getJsonValueStr("tStartLong", loc_obj);
                    final String tSaddress = generalFunc.getJsonValueStr("tSaddress", loc_obj);

                    recentAddrTxtView.setText(tSaddress);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("tLat", tStartLat);
                    map.put("tLong", tStartLong);
                    map.put("taddress", tSaddress);

                    recentLocList.add(map);
                    recentAdapterView.setOnClickListener(view12 -> {
                        if (whichView != null) {
                            if (whichView.equals("source")) {

                                Bundle bn = new Bundle();
                                bn.putString("Address", tSaddress);
                                bn.putString("Latitude", "" + tStartLat);
                                bn.putString("Longitude", "" + tStartLong);

                                if (getIntent().hasExtra("isFromMulti")) {
                                    bn.putBoolean("isFromMulti", true);
                                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                                }

                                new ActUtils(getActContext()).setOkResult(bn);

                                finish();

                            }


                        } else {

                        }
                    });
                    sourceLocationView.addView(view);
                }
            }

        } else {
            destLocationView.setVisibility(View.GONE);
            sourceLocationView.setVisibility(View.GONE);
            recentLocHTxtView.setVisibility(View.GONE);
        }
    }

    public Context getActContext() {
        return SearchLocationActivity.this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.ADD_HOME_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            HashMap<String, String> storeData = new HashMap<>();
            storeData.put("userHomeLocationLatitude", "" + data.getStringExtra("Latitude"));
            storeData.put("userHomeLocationLongitude", "" + data.getStringExtra("Longitude"));
            storeData.put("userHomeLocationAddress", "" + data.getStringExtra("Address"));
            generalFunc.storeData(storeData);
            homePlaceTxt.setText(data.getStringExtra("Address"));
            checkPlaces(whichLocation);


            Bundle bn = new Bundle();
            bn.putString("Latitude", data.getStringExtra("Latitude"));
            bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
            bn.putString("Address", "" + data.getStringExtra("Address"));

            bn.putBoolean("isSkip", false);
            if (getIntent().hasExtra("isFromMulti")) {
                bn.putBoolean("isFromMulti", true);
                bn.putInt("pos", getIntent().getIntExtra("pos", -1));
            }


            new ActUtils(getActContext()).setOkResult(bn);
            finish();

        } else if (requestCode == Utils.ADD_MAP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

            Bundle bn = new Bundle();
            bn.putString("Latitude", data.getStringExtra("Latitude"));
            bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
            bn.putString("Address", "" + data.getStringExtra("Address"));
            bn.putBoolean("isSkip", false);
            if (getIntent().hasExtra("isFromMulti")) {
                bn.putBoolean("isFromMulti", true);
                bn.putInt("pos", getIntent().getIntExtra("pos", -1));
            }
            new ActUtils(getActContext()).setOkResult(bn);
            finish();

        } else if (requestCode == Utils.ADD_WORK_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            HashMap<String, String> storeData = new HashMap<>();
            storeData.put("userWorkLocationLatitude", "" + data.getStringExtra("Latitude"));
            storeData.put("userWorkLocationLongitude", "" + data.getStringExtra("Longitude"));
            storeData.put("userWorkLocationAddress", "" + data.getStringExtra("Address"));
            generalFunc.storeData(storeData);
            workPlaceTxt.setText(data.getStringExtra("Address"));
            checkPlaces(whichLocation);


            Bundle bn = new Bundle();
            bn.putString("Latitude", data.getStringExtra("Latitude"));
            bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
            bn.putString("Address", "" + data.getStringExtra("Address"));
            bn.putBoolean("isSkip", false);
            if (getIntent().hasExtra("isFromMulti")) {
                bn.putBoolean("isFromMulti", true);
                bn.putInt("pos", getIntent().getIntExtra("pos", -1));
            }
            new ActUtils(getActContext()).setOkResult(bn);
            finish();


        }


    }

    public void getGooglePlaces(String input) {

        noPlacedata.setVisibility(View.GONE);
        String latitude = "";
        String longitude = "";
        if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {
            latitude = getIntent().getDoubleExtra("lat", 0.0) + "";
            latitude = getIntent().getDoubleExtra("long", 0.0) + "";
        }

        AppService.getInstance().executeService(getActContext(), new DataProvider.DataProviderBuilder(latitude, longitude).setData_Str(input).setToken(session_token).build(), AppService.Service.PLACE_SUGGESTIONS, new AppService.ServiceDelegate() {
            @Override
            public void onResult(HashMap<String, Object> data) {
                if (data.get("RESPONSE_TYPE") != null && data.get("RESPONSE_TYPE").toString().equalsIgnoreCase("FAIL")) {

                    placelist.clear();
                    if (placesAdapter != null) {
                        placesAdapter.notifyDataSetChanged();
                    }

                    String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
                    noPlacedata.setText(msg);
                    placesRecyclerView.setVisibility(View.VISIBLE);

                    noPlacedata.setVisibility(View.VISIBLE);

                    return;
                }
                searchResult((ArrayList<HashMap<String, String>>) data.get("PLACE_SUGGESTION_DATA"), data);

            }
        });
    }

    public void getSelectAddresLatLong(String Place_id, final String address) {

        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY, "");
        data.put(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY, "");
        data = generalFunc.retrieveValue(data);


        String serverKey = data.get(Utils.GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY);


        String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + Place_id + "&key=" + serverKey +
                "&language=" + data.get(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";

        ServerTask exeWebServer = new ServerTask(getActContext(), url, true);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (generalFunc.getJsonValue("status", responseString).equals("OK")) {
                String resultObj = generalFunc.getJsonValue("result", responseString);
                String geometryObj = generalFunc.getJsonValue("geometry", resultObj);
                String locationObj = generalFunc.getJsonValue("location", geometryObj);
                String latitude = generalFunc.getJsonValue("lat", locationObj);
                String longitude = generalFunc.getJsonValue("lng", locationObj);

                Bundle bn = new Bundle();
                bn.putString("Address", address);
                bn.putString("Latitude", "" + latitude);
                bn.putString("Longitude", "" + longitude);
                bn.putBoolean("isSkip", false);

                if (getIntent().hasExtra("isFromMulti")) {
                    bn.putBoolean("isFromMulti", true);
                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                }

                new ActUtils(getActContext()).setOkResult(bn);
                finish();


            }

        });
        exeWebServer.execute();

    }

    @Override
    public void onLocationUpdate(Location mLastLocation) {
        if (isFirstLocation) {
            isFirstLocation = false;
            currentLat = mLastLocation.getLatitude();
            currentLong = mLastLocation.getLongitude();
        }

    }

    @Override
    public void onTouchDelegate() {
        Utils.hideKeyboard(getActContext());
    }


    public void searchResult(ArrayList<HashMap<String, String>> placelist, HashMap<String, Object> data) {
        this.placelist.clear();
        this.placelist.addAll(placelist);
        imageCancel.setVisibility(View.VISIBLE);


        if (searchTxt.getText().toString().length() == 0) {
            placesRecyclerView.setVisibility(View.GONE);
            noPlacedata.setVisibility(View.GONE);

            return;
        }


        googleimagearea.setVisibility(View.GONE);
        if (placelist.size() > 0) {
            JSONObject jsonObject = new JSONObject(data);

            String RESPONSE_DATA = generalFunc.getJsonValueStr("RESPONSE_DATA", jsonObject);
            if (Utils.checkText(RESPONSE_DATA)) {
                JSONObject RESPONSE_DATA_OBJ = generalFunc.getJsonObject(RESPONSE_DATA);
                String vServiceName = generalFunc.getJsonValueStr("vServiceName", RESPONSE_DATA_OBJ);
                if (!RESPONSE_DATA_OBJ.has("vServiceName") || (Utils.checkText(vServiceName) && vServiceName.equalsIgnoreCase("Google"))) {
                    googleimagearea.setVisibility(View.VISIBLE);
                }
            }

            placesRecyclerView.setVisibility(View.VISIBLE);
            if (placesAdapter == null) {
                placesAdapter = new PlacesAdapter(getActContext(), this.placelist);
                placesRecyclerView.setAdapter(placesAdapter);
                placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

            } else {
                placesAdapter.notifyDataSetChanged();
            }
        } else if (searchTxt.getText().toString().length() == 0) {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }

            String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);

            noPlacedata.setVisibility(View.VISIBLE);

            return;
        } else {

            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }
            String msg = "";
            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

            } else {
                msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

            }

            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);
            noPlacedata.setVisibility(View.VISIBLE);

            //} else if (generalFunc.getJsonValue("status", responseString).equals("ZERO_RESULTS")) {
        }

    }

//    @Override
//    public void resetOrAddDest(int selPos, String address, double latitude, double longitude, String isSkip) {
//        Bundle bn = new Bundle();
//        bn.putString("Address", address);
//        bn.putString("Latitude", "" + latitude);
//        bn.putString("Longitude", "" + longitude);
//        if (Utils.checkText(isSkip)) {
//            bn.putBoolean("isSkip", isSkip.equalsIgnoreCase("true") ? true : false);
//        }
//
//        Utils.hideKeyboard(this);
//
//        new ActUtils(getActContext()).setOkResult(bn);
//
//
//        finish();
//
//    }


    public void onClick(View view) {
        int i = view.getId();

        Bundle bndl = new Bundle();

        if (i == R.id.cancelTxt) {
            finish();

        } else if (i == R.id.mapLocArea) {
            bndl.putString("locationArea", getIntent().getStringExtra("locationArea"));
            String from = !whichLocation.equals("dest") ? "isPickUpLoc" : "isDestLoc";
            String lati = !whichLocation.equals("dest") ? "PickUpLatitude" : "DestLatitude";
            String longi = !whichLocation.equals("dest") ? "PickUpLongitude" : "DestLongitude";
            String address = !whichLocation.equals("dest") ? "PickUpAddress" : "DestAddress";


            bndl.putString(from, "true");
            if (getIntent().getDoubleExtra("lat", 0.0) != 0.0 && getIntent().getDoubleExtra("long", 0.0) != 0.0) {
                bndl.putString(lati, "" + getIntent().getDoubleExtra("lat", 0.0));
                bndl.putString(longi, "" + getIntent().getDoubleExtra("long", 0.0));
                if (!getIntent().getStringExtra("address").equalsIgnoreCase("")) {
                    bndl.putString(address, "" + getIntent().getStringExtra("address"));
                } else {
                    bndl.putString(address, "");
                }

            }
            bndl.putString("IS_FROM_SELECT_LOC", "Yes");

            if (getIntent().hasExtra("isFromMulti")) {
                bndl.putBoolean("isFromMulti", true);
                bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
            }
            if (getIntent().hasExtra("eSystem")) {
                bndl.putString("eSystem", Utils.eSystem_Type);
            }

            new ActUtils(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                    bndl, Utils.ADD_MAP_LOC_REQ_CODE);


        } else if (i == R.id.locPlacesTxt) {

            //  bottomSheet.performClick();
        } else if (i == R.id.homeLocArea) {

//                if (mpref_place != null) {

            HashMap<String, String> data = new HashMap<>();
            data.put("userHomeLocationAddress", "");
            data.put("userHomeLocationLatitude", "");
            data.put("userHomeLocationLongitude", "");
            data = generalFunc.retrieveValue(data);

            final String home_address_str = data.get("userHomeLocationAddress");
            final String home_addr_latitude = data.get("userHomeLocationLatitude");
            final String home_addr_longitude = data.get("userHomeLocationLongitude");

            if (home_address_str != null) {

                if (whichLocation.equals("dest")) {


                    LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, home_addr_latitude), generalFunc.parseDoubleValue(0.0, home_addr_longitude));


                    Bundle bn = new Bundle();
                    bn.putString("Address", home_address_str);
                    bn.putString("Latitude", "" + placeLocation.latitude);
                    bn.putString("Longitude", "" + placeLocation.longitude);

                    bn.putBoolean("isSkip", false);

                    if (getIntent().hasExtra("isFromMulti")) {
                        bn.putBoolean("isFromMulti", true);
                        bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                    }

                    new ActUtils(getActContext()).setOkResult(bn);
                    finish();
                } else {

                    LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, home_addr_latitude), generalFunc.parseDoubleValue(0.0, home_addr_longitude));

                    Bundle bn = new Bundle();
                    bn.putString("Address", home_address_str);
                    bn.putString("Latitude", "" + placeLocation.latitude);
                    bn.putString("Longitude", "" + placeLocation.longitude);
                    bn.putBoolean("isSkip", false);

                    if (getIntent().hasExtra("isFromMulti")) {
                        bn.putBoolean("isFromMulti", true);
                        bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                    }

                    new ActUtils(getActContext()).setOkResult(bn);
                    finish();
                }
            } else {
                if (intCheck.isNetworkConnected()) {
                    bndl.putString("isHome", "true");

                    if (getIntent().hasExtra("isFromMulti")) {
                        bndl.putBoolean("isFromMulti", true);
                        bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                    }

                    new ActUtils(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bndl, Utils.ADD_HOME_LOC_REQ_CODE);
                } else {
                    generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }
            }
                /*} else {
                    if (intCheck.isNetworkConnected()) {
                        bndl.putString("isHome", "true");

                        if (getIntent().hasExtra("isFromMulti")) {
                            bndl.putBoolean("isFromMulti", true);
                            bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }

                        new ActUtils(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                                bndl, Utils.ADD_HOME_LOC_REQ_CODE);
                    } else {
                        generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                    }
                }*/

        } else if (i == R.id.workLocArea) {

//                if (mpref_place != null) {

            HashMap<String, String> data = new HashMap<>();
            data.put("userWorkLocationAddress", "");
            data.put("userWorkLocationLatitude", "");
            data.put("userWorkLocationLongitude", "");
            data = generalFunc.retrieveValue(data);

            String work_address_str = data.get("userWorkLocationAddress");

            String work_addr_latitude = data.get("userWorkLocationLatitude");
            String work_addr_longitude = data.get("userWorkLocationLongitude");

            if (work_address_str != null) {

                if (whichLocation.equals("dest")) {

                    LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, work_addr_latitude), generalFunc.parseDoubleValue(0.0, work_addr_longitude));

                    Bundle bn = new Bundle();
                    bn.putString("Address", work_address_str);
                    bn.putString("Latitude", "" + placeLocation.latitude);
                    bn.putString("Longitude", "" + placeLocation.longitude);
                    bn.putBoolean("isSkip", false);

                    if (getIntent().hasExtra("isFromMulti")) {
                        bn.putBoolean("isFromMulti", true);
                        bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                    }

                    new ActUtils(getActContext()).setOkResult(bn);
                    finish();
                } else {


                    LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, work_addr_latitude), generalFunc.parseDoubleValue(0.0, work_addr_longitude));

                    Bundle bn = new Bundle();
                    bn.putString("Address", work_address_str);
                    bn.putString("Latitude", "" + placeLocation.latitude);
                    bn.putString("Longitude", "" + placeLocation.longitude);
                    bn.putBoolean("isSkip", false);

                    if (getIntent().hasExtra("isFromMulti")) {
                        bn.putBoolean("isFromMulti", true);
                        bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                    }

                    new ActUtils(getActContext()).setOkResult(bn);
                    finish();
                }
            } else {

                if (intCheck.isNetworkConnected()) {
                    bndl.putString("isWork", "true");
                    if (getIntent().hasExtra("isFromMulti")) {
                        bndl.putBoolean("isFromMulti", true);
                        bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                    }
                    new ActUtils(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bndl, Utils.ADD_WORK_LOC_REQ_CODE);
                } else {
                    generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }
            }
                /*} else {

                    if (intCheck.isNetworkConnected()) {
                        bndl.putString("isWork", "true");
                        if (getIntent().hasExtra("isFromMulti")) {
                            bndl.putBoolean("isFromMulti", true);
                            bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }
                        new ActUtils(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                                bndl, Utils.ADD_WORK_LOC_REQ_CODE);
                    } else {
                        generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                    }
                }*/
        } else if (i == R.id.homeActionImgView) {
            if (intCheck.isNetworkConnected()) {
                Bundle bn = new Bundle();
                bn.putString("isHome", "true");
                if (getIntent().hasExtra("isFromMulti")) {
                    bn.putBoolean("isFromMulti", true);
                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                }
                new ActUtils(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bn, Utils.ADD_HOME_LOC_REQ_CODE);
            } else {
                generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
            }
        } else if (i == R.id.workActionImgView) {

            if (intCheck.isNetworkConnected()) {
                Bundle bn = new Bundle();
                bn.putString("isWork", "true");
                if (getIntent().hasExtra("isFromMulti")) {
                    bn.putBoolean("isFromMulti", true);
                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                }
                new ActUtils(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bn, Utils.ADD_WORK_LOC_REQ_CODE);
            } else {
                generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
            }
        } else if (i == R.id.imageCancel) {
            placesRecyclerView.setVisibility(View.GONE);
            googleimagearea.setVisibility(View.GONE);
            if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                placesarea.setVisibility(View.VISIBLE);
            }
            searchTxt.setText("");
            noPlacedata.setVisibility(View.GONE);
        } else if (i == R.id.myLocationarea) {

            if (intCheck.isNetworkConnected()) {
                if (generalFunc.isLocationEnabled()) {
                    if (currentLat == 0.0 || currentLong == 0.0) {
                        generalFunc.showMessage(myLocationarea, generalFunc.retrieveLangLBl("", "LBL_NO_LOCATION_FOUND_TXT"));

                        return;
                    }

                    getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
                    getAddressFromLocation.setLoaderEnable(true);
                    getAddressFromLocation.setLocation(currentLat, currentLong);
                    getAddressFromLocation.setAddressList(SearchLocationActivity.this);
                    getAddressFromLocation.execute();


                } else {
                    //GPS Dialog
                }
            } else {
                generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
            }
        } else if (i == R.id.destinationLaterArea) {

            Bundle bn = new Bundle();
            bn.putBoolean("isSkip", true);
            if (getIntent().hasExtra("isFromMulti")) {
                bn.putBoolean("isFromMulti", true);
                bn.putInt("pos", getIntent().getIntExtra("pos", -1));
            }
            new ActUtils(getActContext()).setOkResult(bn);
            finish();

        }

    }

}
