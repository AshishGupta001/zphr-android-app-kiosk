package com.zphr.kiosk;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.activity.ParentActivity;
import com.adapter.files.UberXBannerPagerAdapter;
import com.adapter.files.kiosk.HotelAdapter;
import com.dialogs.OpenListView;
import com.facebook.ads.AdSize;
import com.general.files.ActUtils;
import com.general.files.BounceAnimation;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.LinearLayoutManagerWithSmoothScroller;
import com.general.files.LoadAvailableCab;
import com.general.files.MyApp;
import com.general.files.RecurringTask;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.model.Hotel;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.service.handler.ApiHandler;
import com.utils.LoadImage;
import com.utils.Utils;
import com.view.LoopingCirclePageIndicator;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class KioskLandingScreenActivity extends ParentActivity implements HotelAdapter.OnItemClickList, GetLocationUpdates.LocationUpdates, GetAddressFromLocation.AddressFound {

    public MTextView tvTimeCount;
    public MTextView tvTimeCountTxt;
    public Location pickUpLocation;
    public GetAddressFromLocation getAddressFromLocation;
    public String userProfileJson;
    public boolean isCashSelected = true;
    public boolean ishandicap = false;
    public boolean isfemale = false;
    public String selectedCabType = "Ride";
    public ArrayList<HashMap<String, String>> currentLoadedDriverList;
    public boolean noCabAvail = false;
    String vName = "";
    String vLastName = "";
    String vAddress = "";
    String vCountry = "";
    MTextView tv_hotelName, tv_hotelAddress;
    MTextView languageText, currancyText;
    MTextView currancyHTxt, languageHTxt;
    MTextView txtSelectDest, tv_arrivalInfoTxt;
    LinearLayout langSelectArea, currencySelectArea, logoutArea;
    String selected_language_code = "";
    String selected_currency = "";
    String selected_currency_symbol = "";
    AVLoadingIndicatorView loaderView;
    ImageView iv_logo;
    ShimmerTextView btn_type2;
    ArrayList<Hotel> destinationList = new ArrayList<>();
    //GetLocationUpdates getLastLocation;
    boolean isCalled = true;
    View bannerArea;
    ViewPager bannerViewPager;
    LoopingCirclePageIndicator bannerCirclePageIndicator;
    RecurringTask updateBannerChangeFreqTask;
    int currentBannerPosition = 0;
    int BANNER_AUTO_ROTATE_INTERVAL = 4000;
    androidx.appcompat.app.AlertDialog alertDialog;
    boolean isFirstLocation = true;
    ArrayList<String> bannerList = new ArrayList<String>();
    UberXBannerPagerAdapter bannerAdapter;
    private Hotel hotelMapData = new Hotel();
    private RecyclerView mRecyclerView;
    private HotelAdapter mAdapter;
    private LoadAvailableCab loadAvailCabs;
    private Shimmer shimmer;
    private String LOGO_IMAGE;

    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    int selCurrancyPosition = -1;
    int selLanguagePosition = -1;
    ArrayList<HashMap<String, String>> language_list = new ArrayList<>();
    String default_selected_language_code = "";
    ArrayList<HashMap<String, String>> currency_list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.kiosk_activity_landing);

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);
        userProfileJson = generalFunc.retrieveValue(Utils.HOTEL_PROFILE_JSON);
        obj_userProfile = generalFunc.getJsonObject(userProfileJson);

        if (generalFunc.getJsonValueStr("ENABLE_FACEBOOK_ADS", obj_userProfile).equalsIgnoreCase("Yes")) {
            faceBooksAdds();
        }
        if (generalFunc.getJsonValueStr("ENABLE_GOOGLE_ADS", obj_userProfile).equalsIgnoreCase("Yes")) {
            googleAdds();
        }


        initView();
        setData();
        setlable();
        getBanners();
        buildLanguageList();
        visitLocationList();
        initializeLoadCab();
    }

    private void googleAdds() {
        AdView mAdView;
        LinearLayout google_banner_container = findViewById(R.id.google_banner_container);
        MobileAds.initialize(getActContext());
        mAdView = new AdView(getActContext());
        mAdView.setAdSize(com.google.android.gms.ads.AdSize.FULL_BANNER);
        mAdView.setAdUnitId(generalFunc.getJsonValueStr("GOOGLE_ADMOB_ID", obj_userProfile));
        AdRequest adRequest = new AdRequest.Builder().build();
        google_banner_container.addView(mAdView);
        mAdView.loadAd(adRequest);
    }

    private void faceBooksAdds() {
        LinearLayout banner_container = findViewById(R.id.banner_container);
        com.facebook.ads.AdView adView;
        adView = new com.facebook.ads.AdView(this, "IMG_16_9_APP_INSTALL#" + generalFunc.getJsonValueStr("FACEBOOK_PLACEMENT_ID", obj_userProfile), AdSize.BANNER_HEIGHT_50);
        banner_container.addView(adView);
        adView.loadAd();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // KIOSK MODE & SCREEN PINNING IMPLEMENTATION START


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Toast.makeText(this, "Volume button is disabled", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Toast.makeText(this, "Volume button is disabled", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    public void getBanners() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getHotelBanners");
        parameters.put("iMemberId", generalFunc.getHotelId());
        parameters.put("UserType", Utils.app_type);


        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc,
                responseString -> {
                    if (responseString != null && !responseString.equals("")) {
                        boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);
                        if (isDataAvail) {
                            JSONArray arr = generalFunc.getJsonArray(Utils.message_str, responseString);

                            if (arr != null) {
                                bannerList = new ArrayList<>();
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject obj_temp = generalFunc.getJsonObject(arr, i);
                                    bannerList.add(generalFunc.getJsonValue("vImage", obj_temp.toString()));
                                }
                                setBannerAdapter();
                            }
                        }
                    }
                });

    }

    private void setBannerAdapter() {
        if (bannerAdapter == null) {
            bannerAdapter = new UberXBannerPagerAdapter(getActContext(), bannerList);
            bannerViewPager.setAdapter(bannerAdapter);

            bannerCirclePageIndicator.setDataSize(bannerList.size());
            bannerCirclePageIndicator.setViewPager(bannerViewPager);

            if (bannerList.size() > 1) {
                bannerCirclePageIndicator.setVisibility(View.VISIBLE);
                updateBannerChangeFreqTask = new RecurringTask(BANNER_AUTO_ROTATE_INTERVAL);
                updateBannerChangeFreqTask.setTaskRunListener(new RecurringTask.OnTaskRunCalled() {
                    @Override
                    public void onTaskRun() {
                        if (currentBannerPosition < (bannerAdapter.getCount() - 1)) {
                            bannerViewPager.setCurrentItem((bannerViewPager.getCurrentItem() + 1), true);
                        } else {
                            bannerViewPager.setCurrentItem(0, true);

                        }
                    }
                });
                updateBannerChangeFreqTask.avoidFirstRun();
                updateBannerChangeFreqTask.startRepeatingTask();
            } else {
                bannerCirclePageIndicator.setVisibility(View.GONE);
            }
        }


    }

    public void initializeLoadCab() {

        if (pickUpLocation == null) {
            Location temploc = new Location("PickupLoc");
            temploc.setLatitude(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("vAddressLat", obj_userProfile)));
            temploc.setLongitude(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("vAddressLong", obj_userProfile)));
            onLocationUpdate(temploc);
        }

        if (pickUpLocation == null) {
            return;
        }

        setSourceAddress(pickUpLocation.getLatitude(), pickUpLocation.getLongitude());

    }

    public void setSourceAddress(double latitude, double longitude) {
        try {
            getAddressFromLocation.setLocation(latitude, longitude);
            getAddressFromLocation.execute();
        } catch (Exception e) {

        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (loadAvailCabs != null) {
            loadAvailCabs.onPauseCalled();
        }
    }


    private void setlable() {

        languageText.setText(generalFunc.retrieveLangLBl("", "LBL_LANGUAGE_TXT"));
        languageHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LANGUAGE_TXT"));

        currancyText.setText(generalFunc.retrieveLangLBl("", "LBL_CURRENCY_TXT"));
        currancyHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CURRENCY_TXT"));

        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.DEFAULT_LANGUAGE_VALUE, "");
        data.put(Utils.DEFAULT_CURRENCY_VALUE, "");
        data = generalFunc.retrieveValue(data);

        languageText.setText(data.get(Utils.DEFAULT_LANGUAGE_VALUE));
        currancyText.setText(data.get(Utils.DEFAULT_CURRENCY_VALUE));

        selected_currency = (data.get(Utils.DEFAULT_CURRENCY_VALUE));
        txtSelectDest.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_DESTINATION"));
        tv_arrivalInfoTxt.setText(generalFunc.retrieveLangLBl("Your Taxi would arrive here in", "LBL_TAXI_ARRIVE"));
        btn_type2.setText(generalFunc.retrieveLangLBl("Proceed", "LBL_PROCEED"));

    }

    private Context getActContext() {
        return KioskLandingScreenActivity.this;
    }

    private void initView() {
        bannerArea = findViewById(R.id.bannerArea);

        int bannerWidth = (int) (Utils.getScreenPixelWidth(getActContext()));
        bannerWidth = ((bannerWidth / 100) * 70);
        int bannerHeight = (int) (bannerWidth / 2.0);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) bannerArea.getLayoutParams();
        layoutParams.weight = bannerWidth;
        layoutParams.height = bannerHeight;
        bannerArea.setLayoutParams(layoutParams);

        bannerViewPager = (ViewPager) findViewById(R.id.bannerViewPager);
        bannerCirclePageIndicator = (LoopingCirclePageIndicator) findViewById(R.id.bannerCirclePageIndicator);

        tv_hotelName = findViewById(R.id.tv_hotelName);
        tv_hotelAddress = findViewById(R.id.tv_hotelAddress);

        txtSelectDest = findViewById(R.id.txtSelectDest);
        tv_arrivalInfoTxt = findViewById(R.id.tv_arrivalInfoTxt);

        tvTimeCount = findViewById(R.id.tvTimeCount);
        tvTimeCountTxt = findViewById(R.id.tvTimeCountTxt);

        languageText = (MTextView) findViewById(R.id.languageText);
        languageHTxt = (MTextView) findViewById(R.id.languageHTxt);
        currancyText = (MTextView) findViewById(R.id.currancyText);
        currancyHTxt = (MTextView) findViewById(R.id.currancyHTxt);

        currencySelectArea = (LinearLayout) findViewById(R.id.currencySelectArea);
        langSelectArea = (LinearLayout) findViewById(R.id.langSelectArea);
        logoutArea = (LinearLayout) findViewById(R.id.logoutArea);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManagerWithSmoothScroller(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setNestedScrollingEnabled(false);


        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        loaderView.setVisibility(View.GONE);

        iv_logo = (ImageView) findViewById(R.id.iv_logo);

        btn_type2 = (ShimmerTextView) findViewById(R.id.btn_type2);
        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickAct());

        shimmer = new Shimmer();
        shimmer.start(btn_type2);

        //.......... END -> Animation for book now text  ..........//

        langSelectArea.setOnClickListener(new setOnClickAct());
        currencySelectArea.setOnClickListener(new setOnClickAct());
        logoutArea.setOnClickListener(new setOnClickAct());
    }


    private void setData() {
        vName = generalFunc.getJsonValueStr("vName", obj_userProfile);
        vLastName = generalFunc.getJsonValueStr("vLastName", obj_userProfile);
        vAddress = generalFunc.getJsonValueStr("vAddress", obj_userProfile);
        vCountry = generalFunc.getJsonValueStr("vCountry", obj_userProfile);

        LOGO_IMAGE = generalFunc.getJsonValueStr("LOGO_IMAGE", obj_userProfile);

        LOGO_IMAGE = Utils.getResizeImgURL(getActContext(), LOGO_IMAGE, Utils.dpToPx(getActContext(), getActContext().getResources().getDimension(R.dimen._65sdp)), Utils.dpToPx(getActContext(), getActContext().getResources().getDimension(R.dimen._65sdp)));

        if (Utils.checkText(LOGO_IMAGE)) {
            new LoadImage.builder(LoadImage.bind(LOGO_IMAGE), iv_logo).setErrorImagePath(R.mipmap.ic_no_icon).setPlaceholderImagePath(R.mipmap.ic_no_icon).build();
        }


        tv_hotelName.setText(vName + " " + vLastName);
        tv_hotelAddress.setText(vAddress);


        setETA("--", "");
    }

    public void setETA(String eta, String minText) {
        if (eta.equals("--") || eta.equalsIgnoreCase("\n" + "--")) {
            noCabAvail = false;
            setCurrentLoadedDriverList(new ArrayList<HashMap<String, String>>());
        } else {
            noCabAvail = true;
        }

        tvTimeCount.setText(eta);
        tvTimeCountTxt.setText(minText);
    }


    public void buildLanguageList() {
        HashMap<String, String> hashMapData = new HashMap<>();
        hashMapData.put(Utils.LANGUAGE_LIST_KEY, "");
        hashMapData.put(Utils.LANGUAGE_CODE_KEY, "");
        hashMapData = generalFunc.retrieveValue(hashMapData);

        JSONArray languageList_arr = generalFunc.getJsonArray(hashMapData.get(Utils.LANGUAGE_LIST_KEY));

        language_list.clear();

        if (languageList_arr != null) {
            for (int i = 0; i < languageList_arr.length(); i++) {
                JSONObject obj_temp = generalFunc.getJsonObject(languageList_arr, i);


                if ((generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY)).equals(generalFunc.getJsonValueStr("vCode", obj_temp))) {
                    selected_language_code = generalFunc.getJsonValueStr("vCode", obj_temp);

                    default_selected_language_code = selected_language_code;
                    selLanguagePosition = i;
                    languageText.setText(generalFunc.getJsonValue("vTitle", obj_temp.toString()));

                }

                HashMap<String, String> data = new HashMap<>();
                data.put("vTitle", generalFunc.getJsonValueStr("vTitle", obj_temp));
                data.put("vCode", generalFunc.getJsonValueStr("vCode", obj_temp));


                language_list.add(data);
            }
            if (language_list.size() < 2 || generalFunc.retrieveValue("LANGUAGE_OPTIONAL").equalsIgnoreCase("Yes")) {
                langSelectArea.setVisibility(View.GONE);
            } else {
                langSelectArea.setVisibility(View.VISIBLE);
            }
        } else {
            langSelectArea.setVisibility(View.GONE);
        }
        buildCurrencyList();
    }


    public void changeLanguagedata(String langcode, boolean showDialog) {


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "changelanguagelabel");
        parameters.put("iUserId", Utils.IS_KIOSK_APP ? generalFunc.getHotelId() : generalFunc.getMemberId());
        parameters.put("vLang", langcode);
        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {
            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    HashMap<String, String> storeData = new HashMap<>();

                    storeData.put(Utils.LANGUAGE_CODE_KEY, selected_language_code);
                    storeData.put(Utils.DEFAULT_LANGUAGE_VALUE, selected_language_code);


                    storeData.put(Utils.languageLabelsKey, generalFunc.getJsonValue(Utils.message_str, responseString));
                    storeData.put(Utils.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValue("eType", responseString));
                    storeData.put(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vGMapLangCode", responseString));
                    storeData.put(Utils.DEFAULT_CURRENCY_VALUE, selected_currency);
                    generalFunc.storeData(storeData);
                    new Handler().postDelayed(() -> generalFunc.restartApp(), 100);


                } else {


                }
            } else {

            }

        });

    }


    public void buildCurrencyList() {
        currency_list.clear();
        JSONArray currencyList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.CURRENCY_LIST_KEY));

        if (currencyList_arr != null) {
            for (int i = 0; i < currencyList_arr.length(); i++) {
                JSONObject obj_temp = generalFunc.getJsonObject(currencyList_arr, i);

                HashMap<String, String> data = new HashMap<>();
                data.put("vName", generalFunc.getJsonValueStr("vName", obj_temp));
                data.put("vSymbol", generalFunc.getJsonValueStr("vSymbol", obj_temp));
                if (!selected_currency.equalsIgnoreCase("") && selected_currency.equalsIgnoreCase(generalFunc.getJsonValueStr("vName", obj_temp))) {
                    selCurrancyPosition = i;
                    currancyText.setText(selected_currency);
                }
                currency_list.add(data);
            }

            if (currency_list.size() < 2 || generalFunc.retrieveValue("CURRENCY_OPTIONAL").equalsIgnoreCase("Yes")) {
                currencySelectArea.setVisibility(View.GONE);
            } else {
                currencySelectArea.setVisibility(View.VISIBLE);

            }
        } else {
            currencySelectArea.setVisibility(View.GONE);
        }
    }

    public void visitLocationList() {

        JSONArray jsonArray = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.VisitLocationsKey));
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jobj = generalFunc.getJsonObject(jsonArray, i);
                hotelMapData = new Hotel();
                hotelMapData.setiVisitId(generalFunc.getJsonValueStr("iVisitId", jobj));
                hotelMapData.setvSourceLatitude(generalFunc.getJsonValueStr("vSourceLatitude", jobj));
                hotelMapData.setvSourceLongitude(generalFunc.getJsonValueStr("vSourceLongitude", jobj));
                hotelMapData.setvSourceAddresss(generalFunc.getJsonValueStr("vSourceAddresss", jobj));
                hotelMapData.setvDestLatitude(generalFunc.getJsonValueStr("vDestLatitude", jobj));
                hotelMapData.setvDestLongitude(generalFunc.getJsonValueStr("vDestLongitude", jobj));
                hotelMapData.settDestAddress(generalFunc.getJsonValueStr("tDestAddress", jobj));
                hotelMapData.settDestName(generalFunc.getJsonValueStr("tDestLocationName", jobj));

                destinationList.add(hotelMapData);
            }
        }

        hotelMapData = new Hotel();
        hotelMapData.setiVisitId("-1");
        hotelMapData.setvSourceLatitude("0.00");
        hotelMapData.setvSourceLongitude("0.00");
        hotelMapData.setvSourceAddresss("");
        hotelMapData.setvDestLatitude("0.00");
        hotelMapData.setvDestLongitude("0.00");
        hotelMapData.settDestAddress("");
        hotelMapData.settDestName(generalFunc.retrieveLangLBl("", "LBL_OTHER_TYPE"));

        destinationList.add(hotelMapData);


        if (mAdapter == null) {
            mAdapter = new HotelAdapter(getActContext(), destinationList);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickList(this);
        }
        mAdapter.notifyDataSetChanged();

    }

    public void showLanguageList() {

        OpenListView.getInstance(getActContext(), getSelectLangText(), language_list, OpenListView.OpenDirection.CENTER, true, position -> {


            selLanguagePosition = position;
            selected_language_code = language_list.get(selLanguagePosition).get("vCode");
            generalFunc.storeData(Utils.DEFAULT_LANGUAGE_VALUE, language_list.get(selLanguagePosition).get("vTitle"));


            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                generalFunc.showGeneralMessage("",
                        generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
            } else {
                changeLanguagedata(selected_language_code, false);
            }

        }).show(selLanguagePosition, "vTitle");
    }

    public void showCurrencyList() {

        OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_SELECT_CURRENCY"), currency_list, OpenListView.OpenDirection.CENTER, true, position -> {


            selCurrancyPosition = position;
            selected_currency_symbol = currency_list.get(selCurrancyPosition).get("vSymbol");
            selected_currency = currency_list.get(selCurrancyPosition).get("vName");
            currancyText.setText(selected_currency);
            generalFunc.storeData(Utils.DEFAULT_CURRENCY_VALUE, selected_currency);

        }).show(selCurrancyPosition, "vName");
    }

    public String getSelectLangText() {
        return ("" + generalFunc.retrieveLangLBl("Select", "LBL_SELECT_LANGUAGE_HINT_TXT"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCalled = true;

        if (bannerList.size() <= 0) {
            getBanners();
        } else {
            setBannerAdapter();
        }

        if (generalFunc.containsKey(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY) && mAdapter != null && !mAdapter.selectedDestAddress.equalsIgnoreCase("-1")) {
            generalFunc.removeValue(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY);
            mAdapter.selectedDestAddress = "-1";
            mAdapter.notifyDataSetChanged();
        }

        if (loadAvailCabs != null) {
            loadAvailCabs.onResumeCalled();
        }
    }


    @Override
    public void onItemClick(int position) {

        if (mAdapter != null) {
            mAdapter.selectedDestAddress = destinationList.get(position).getiVisitId();
        }

        mAdapter.notifyDataSetChanged();

        if (destinationList.get(position).getiVisitId().equalsIgnoreCase("-1")) {
            if (generalFunc.containsKey(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY)) {
                generalFunc.removeValue(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY);
            }
            return;
        }


        Hotel item = destinationList.get(position);
        Gson gson = new Gson();
        String json = gson.toJson(item);
        generalFunc.storeData(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY, json);
    }

    @Override
    public void onLocationUpdate(Location location) {
        if (location == null) {
            return;
        }

        pickUpLocation = location;

        if (isFirstLocation == true) {

            isFirstLocation = false;
            initializeLoadCab();
        } else if (loadAvailCabs != null) {
            loadAvailCabs.setPickUpLocation(pickUpLocation);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (shimmer != null) {
            shimmer.cancel();
        }

        if (loadAvailCabs != null) {
            loadAvailCabs.setTaskKilledValue(true);
        }


    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {
        generalFunc.storeData(Utils.KIOSK_HOTEL_ADDRESS_KEY, address);
        generalFunc.storeData(Utils.KIOSK_HOTEL_Lattitude_KEY, "" + latitude);
        generalFunc.storeData(Utils.KIOSK_HOTEL_Longitude_KEY, "" + longitude);
        initLoadAvailcab(address, latitude, longitude, geocodeobject);

    }

    private void initLoadAvailcab(String address, double latitude, double longitude, String geocodeobject) {

        Location pickUpLoc = new Location("");
        pickUpLoc.setLatitude(latitude);
        pickUpLoc.setLongitude(longitude);


        if (loadAvailCabs == null) {
            loadAvailCabs = new LoadAvailableCab(getActContext(), generalFunc, selectedCabType, pickUpLocation, null, userProfileJson);
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

    public void showLogoutBox() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(generalFunc.retrieveLangLBl("Are you sure want to logout?", "LBL_LOGOUT_HEADER"));

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);

        final String required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        final String noWhiteSpace = generalFunc.retrieveLangLBl("Password should not contain whitespace.", "LBL_ERROR_NO_SPACE_IN_PASS");
        final String pass_length = generalFunc.retrieveLangLBl("Password must be", "LBL_ERROR_PASS_LENGTH_PREFIX")
                + " " + Utils.minPasswordLength + " " + generalFunc.retrieveLangLBl("or more character long.", "LBL_ERROR_PASS_LENGTH_SUFFIX");
        final String Driver_Password_decrypt = generalFunc.getJsonValue("Driver_Password_decrypt", userProfileJson);

        final MaterialEditText previous_passwordBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        previous_passwordBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CURR_PASS_HEADER"));
        previous_passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setView(dialogView);
        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isCurrentPasswordEnter = Utils.checkText(previous_passwordBox) ?
                        (Utils.getText(previous_passwordBox).contains(" ") ? Utils.setErrorFields(previous_passwordBox, noWhiteSpace)
                                : (Utils.getText(previous_passwordBox).length() >= Utils.minPasswordLength ? true : Utils.setErrorFields(previous_passwordBox, pass_length)))
                        : Utils.setErrorFields(previous_passwordBox, required_str);

                if (isCurrentPasswordEnter == false) {
                    return;
                }

                alertDialog.dismiss();

                MyApp.getInstance().logOutFromDevice(true, Utils.getText(previous_passwordBox));


            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }


    public void logout(String password) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "callOnLogout");
        parameters.put("iMemberId", generalFunc.getHotelId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("vPassword", password);

        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    removeScreenPin();
                    generalFunc.restartApp();
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    public void notifyCabsAvailable() {
        if (loadAvailCabs != null && loadAvailCabs.listOfDrivers != null && loadAvailCabs.listOfDrivers.size() > 0) {

            if (loadAvailCabs.isAvailableCab) {
                if (!Utils.getText(tvTimeCount).equalsIgnoreCase("\n" + "--")) {
                    noCabAvail = true;
                }
            }
        }
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

    public void setCurrentLoadedDriverList(ArrayList<HashMap<String, String>> currentLoadedDriverList) {
        this.currentLoadedDriverList = currentLoadedDriverList;
    }

    public class setOnClickAct implements View.OnClickListener, BounceAnimation.BounceAnimListener {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(getActContext());
            if (i == btn_type2.getId()) {
                BounceAnimation.setBounceAnimation(getActContext(), btn_type2);
                BounceAnimation.setBounceAnimListener(this);
            } else if (i == R.id.langSelectArea) {
                showLanguageList();
            } else if (i == R.id.currencySelectArea) {
                showCurrencyList();
            } else if (i == R.id.logoutArea) {
                showLogoutBox();
            }
        }

        @Override
        public void onAnimationFinished(View view) {

            if (view == btn_type2) {

                if ((currentLoadedDriverList != null && currentLoadedDriverList.size() < 1) || currentLoadedDriverList == null) {
                    generalFunc.notifyRestartApp("",
                            generalFunc.retrieveLangLBl("Car Not Available", "LBL_CAR_NOT_AVAILABEL"));
                    return;
                }

            }
            Bundle bundle = new Bundle();
            bundle.putString("type", "register");
            new ActUtils(getActContext()).startActWithData(AppLoignRegisterActivity.class, bundle);

        }
    }
}
