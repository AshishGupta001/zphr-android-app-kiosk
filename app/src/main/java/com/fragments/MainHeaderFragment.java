package com.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.drawRoute.DirectionsJSONParser;
import com.general.files.ActUtils;
import com.general.files.BounceAnimation;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.PolyLineAnimator;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.zphr.kiosk.KioskBookNowActivity;
import com.zphr.kiosk.R;
import com.zphr.kiosk.SearchLocationActivity;
import com.service.handler.ApiHandler;
import com.service.handler.AppService;
import com.service.model.DataProvider;
import com.service.server.ServerTask;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainHeaderFragment extends Fragment implements GetAddressFromLocation.AddressFound, ViewTreeObserver.OnGlobalLayoutListener {

    public ImageView menuImgView;
    public ImageView backImgView;
    public MTextView pickUpLocTxt;
    public LinearLayout pickupLocArea1;
    public MTextView sourceLocSelectTxt;
    public MTextView destLocSelectTxt;
    public LinearLayout pickUpLocSearchArea;
    public MTextView uberXTitleTxtView;
    public LinearLayout MainHeaderLayout;
    public boolean isfirst = false;
    public boolean isfirstly = false;
    public boolean isAddressEnable;
    public GetAddressFromLocation getAddressFromLocation;
    public View view;
    public LinearLayout destarea;
    public LatLng sourceLocation = null;
    public LatLng destLocation = null;
    public Marker sourceMarker, destMarker, sourceDotMarker, destDotMarker;
    public boolean isroutefound = false;
    /*Route Draw on destination selection*/
    public boolean isRouteFail = false;
    public int fragmentWidth = 0;
    public int fragmentHeight = 0;
    public boolean isCabsLoadedIsInProcess = true;
    boolean isDestinationMode = false;
    KioskBookNowActivity mainAct;
    GeneralFunctions generalFunc;
    GoogleMap gMap;
    ImageView headerLogo;
    MTextView destLocTxt;
    String pickUpAddress = "";
    String destAddress = "";
    MainHeaderFragment mainHeaderFrag;
    String userProfileJson = "";
    MTextView pickUpLocHTxt, destLocHTxt;
    boolean isUfx = false;
    boolean isclickabledest = false;
    boolean isclickablesource = false;
    ImageView addPickUpImage, editPickupImage, addPickArea2Image, editPickArea2Image;
    ImageView addDestImageview, editDestImageview, addDestarea2Image, editDestarea2Image;
    /*Multi Delivery*/
    RelativeLayout destinationArea1;
    String distance = "";
    String time = "";
    Polyline route_polyLine;
    View marker_view;
    MarkerOptions source_dot_option, dest_dot_option;
    MTextView addressTxt, etaTxt;
    ServerTask estimateFareTask;
    private CardView area_source;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_main_header, container, false);
        menuImgView = (ImageView) view.findViewById(R.id.menuImgView);
        backImgView = (ImageView) view.findViewById(R.id.backImgView);
        pickUpLocTxt = (MTextView) view.findViewById(R.id.pickUpLocTxt);
        sourceLocSelectTxt = (MTextView) view.findViewById(R.id.sourceLocSelectTxt);
        destLocSelectTxt = (MTextView) view.findViewById(R.id.destLocSelectTxt);
        pickUpLocSearchArea = (LinearLayout) view.findViewById(R.id.pickUpLocSearchArea);
        destLocTxt = (MTextView) view.findViewById(R.id.destLocTxt);
        pickUpLocHTxt = (MTextView) view.findViewById(R.id.pickUpLocHTxt);
        destLocHTxt = (MTextView) view.findViewById(R.id.destLocHTxt);
        pickupLocArea1 = (LinearLayout) view.findViewById(R.id.pickupLocArea1);
        destinationArea1 = (RelativeLayout) view.findViewById(R.id.destinationArea1);
        pickupLocArea1.setOnClickListener(new setOnClickList());
        destarea = (LinearLayout) view.findViewById(R.id.destarea);
        area_source = (CardView) view.findViewById(R.id.area_source);
        destarea.setOnClickListener(new setOnClickList());


        addPickUpImage = (ImageView) view.findViewById(R.id.addPickUpImage);
        editPickupImage = (ImageView) view.findViewById(R.id.editPickupImage);
        addDestImageview = (ImageView) view.findViewById(R.id.addDestImageview);
        editDestImageview = (ImageView) view.findViewById(R.id.editDestImageview);
        addPickArea2Image = (ImageView) view.findViewById(R.id.addPickArea2Image);
        editPickArea2Image = (ImageView) view.findViewById(R.id.editPickArea2Image);

        addDestarea2Image = (ImageView) view.findViewById(R.id.addDestarea2Image);
        editDestarea2Image = (ImageView) view.findViewById(R.id.editDestarea2Image);
        MainHeaderLayout = (LinearLayout) view.findViewById(R.id.MainHeaderLayout);

        headerLogo = (ImageView) view.findViewById(R.id.headerLogo);
        uberXTitleTxtView = (MTextView) view.findViewById(R.id.titleTxt);

        destarea.setOnClickListener(new setOnClickList());

        mainAct = (KioskBookNowActivity) getActivity();

        if (!mainAct.isMenuImageShow) {
            menuImgView.setVisibility(View.GONE);
            backImgView.setVisibility(View.GONE);
        }

        generalFunc = mainAct.generalFunc;

        isUfx = getArguments().getBoolean("isUfx", false);

        pickUpLocHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PICK_UP_FROM"));
        destLocHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DROP_AT"));

        uberXTitleTxtView.setText(generalFunc.retrieveLangLBl("Service Providers", "LBL_SERVICE_PROVIDERS"));
        mainHeaderFrag = mainAct.getMainHeaderFrag();
        userProfileJson = mainAct.obj_userProfile.toString();

        getAddressFromLocation = new GetAddressFromLocation(mainAct.getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        pickUpLocTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));


        if (Utils.IS_KIOSK_APP) {
            mainHeaderFrag.configDestinationMode(false);
            mainHeaderFrag.setExistingPickUpAddress(mainAct.getPickUpLocationAddress(), mainAct.getPickUpLocation());

        }

        menuImgView.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());

        if (!isUfx && !Utils.IS_KIOSK_APP) {
            if (mainAct.isFirstTime) {
                menuImgView.performClick();
                mainAct.isFirstTime = false;
            }
        }

        sourceLocSelectTxt.setOnClickListener(new setOnClickList());
        destLocSelectTxt.setOnClickListener(new setOnClickList());
        pickUpLocSearchArea.setOnClickListener(new setOnClickList());

        destLocTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_DESTINATION_BTN_TXT"));
        destLocSelectTxt.setText(generalFunc.retrieveLangLBl("", Utils.IS_KIOSK_APP ? "LBL_SELECT_DESTINATION" : "LBL_ADD_DESTINATION_BTN_TXT"));

        handleDestAddIcon();


        MainHeaderLayout.setVisibility(View.VISIBLE);


        new CreateRoundedView(getResources().getColor(R.color.pickup_req_now_btn), Utils.dipToPixels(mainAct, 25), 0, Color.parseColor("#00000000"), view.findViewById(R.id.imgsourcearea1));
        new CreateRoundedView(getResources().getColor(R.color.pickup_req_later_btn), Utils.dipToPixels(mainAct, 25), 0, Color.parseColor("#00000000"), view.findViewById(R.id.imagemarkerdest1));
        new CreateRoundedView(getResources().getColor(R.color.pickup_req_now_btn), Utils.dipToPixels(mainAct, 25), 0, Color.parseColor("#00000000"), view.findViewById(R.id.imgsourcearea2));
        new CreateRoundedView(getResources().getColor(R.color.pickup_req_later_btn), Utils.dipToPixels(mainAct, 25), 0, Color.parseColor("#00000000"), view.findViewById(R.id.imagemarkerdest2));

        CameraPosition cameraPosition = mainAct.cameraForUserPosition();

        if (mainAct.getMap() != null && mainAct.getIntent().getStringExtra("latitude") != null && mainAct.getIntent().getStringExtra("longitude") != null && !mainAct.getIntent().getStringExtra("latitude").equals("0.0") && !mainAct.getIntent().getStringExtra("longitude").equals("0.0")) {
            CameraPosition cameraPosition1 = new CameraPosition.Builder().target(
                    new LatLng(generalFunc.parseDoubleValue(0.0, mainAct.getIntent().getStringExtra("latitude")),
                            generalFunc.parseDoubleValue(0.0, mainAct.getIntent().getStringExtra("longitude"))))
                    .zoom(Utils.defaultZomLevel).build();
            mainAct.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1));
        } else if (cameraPosition != null) {
            mainAct.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


        if (cameraPosition != null) {
            onGoogleMapCameraChangeList gmap = new onGoogleMapCameraChangeList();
            gmap.onCameraIdle();
        }
        return view;
    }

    private void setDestinationView(boolean callCabs) {
        mainAct.destAddress = "";
        mainAct.destLocLatitude = "";
        mainAct.destLocLongitude = "";

        if (mainAct.isMenuImageShow) {
            menuImgView.setVisibility(View.GONE);
            backImgView.setVisibility(View.VISIBLE);
        }


        if (mainAct != null && callCabs) {
            mainAct.addcabselectionfragment();
        }

        if (gMap != null) {
            gMap.clear();
        }
    }



    public void setGoogleMapInstance(GoogleMap gMap) {
        this.gMap = gMap;
        this.gMap.setOnCameraIdleListener(new onGoogleMapCameraChangeList());
    }

    public void setDefaultView() {
        destLocTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_DESTINATION_BTN_TXT"));
        destLocSelectTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_DESTINATION_BTN_TXT"));
        mainAct.setDestinationPoint("", "", "", false);

        if (mainAct.pickUpLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mainAct.pickUpLocation.getLatitude(), mainAct.pickUpLocation.getLongitude())).zoom(gMap.getCameraPosition().zoom).build();

            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void setDestinationAddress(String destAddress) {
        LatLng center = null;
        if (gMap != null && gMap.getCameraPosition() != null) {
            center = gMap.getCameraPosition().target;
        }

        if (center == null) {
            return;
        }

        if (destLocTxt != null) {
            destLocTxt.setText(destAddress);
        } else {
            this.destAddress = destAddress;
        }

        mainAct.setDestinationPoint("" + center.latitude, "" + center.longitude, destAddress, true);
    }


    public String getPickUpAddress() {
        return pickUpLocTxt.getText().toString();
    }

    public void setPickUpAddress(String pickUpAddress) {
        LatLng center = null;
        if (gMap != null && gMap.getCameraPosition() != null) {
            center = gMap.getCameraPosition().target;
        }
        if (center == null) {
            return;
        }

        if (sourceLocSelectTxt != null) {
            sourceLocSelectTxt.setText(pickUpAddress);
        }
        this.pickUpAddress = pickUpAddress;
        if (pickUpLocTxt != null) {
            pickUpLocTxt.setText(pickUpAddress);
            handlePickEditIcon();
        } else {
            this.pickUpAddress = pickUpAddress;
        }

        mainAct.pickUpLocationAddress = this.pickUpAddress;
        Location pickupLocation = new Location("");
        pickupLocation.setLongitude(center.longitude);
        pickupLocation.setLatitude(center.latitude);
        mainAct.pickUpLocation = pickupLocation;

        if (Utils.IS_KIOSK_APP) {
            new Handler().postDelayed(() -> handleSourceMarker("--", true), 200);
        }

    }

    public void setExistingPickUpAddress(String pickUpAddress, Location pickupLocation) {

        if (sourceLocSelectTxt != null) {
            sourceLocSelectTxt.setText(pickUpAddress);
        }
        this.pickUpAddress = pickUpAddress;
        if (pickUpLocTxt != null) {
            pickUpLocTxt.setText(pickUpAddress);
            handlePickEditIcon();
        } else {
            this.pickUpAddress = pickUpAddress;
        }

        mainAct.pickUpLocationAddress = this.pickUpAddress;
        mainAct.pickUpLocation = pickupLocation;

        if (Utils.IS_KIOSK_APP) {
            new Handler().postDelayed(() -> handleSourceMarker("--", true), 200);
        }

    }

    public void handlePickEditIcon() {
        addPickUpImage.setVisibility(View.GONE);
        editPickupImage.setVisibility(View.VISIBLE);
        addPickArea2Image.setVisibility(View.GONE);
        editPickArea2Image.setVisibility(View.VISIBLE);
    }

    public void handleDestEditIcon() {
        addDestImageview.setVisibility(View.GONE);
        editDestImageview.setVisibility(View.VISIBLE);
        addDestarea2Image.setVisibility(View.GONE);
        editDestarea2Image.setVisibility(View.VISIBLE);
    }

    public void handleDestAddIcon() {
        addDestImageview.setVisibility(View.VISIBLE);
        editDestImageview.setVisibility(View.GONE);
        addDestarea2Image.setVisibility(View.VISIBLE);
        editDestarea2Image.setVisibility(View.GONE);
    }

    public void configDestinationMode(boolean isDestinationMode) {
        this.isDestinationMode = isDestinationMode;
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {


        if (latitude == mainAct.pickUp_tmpLatitude && longitude == mainAct.pickUp_tmpLongitude && !mainAct.pickUp_tmpAddress.equalsIgnoreCase("")) {
            address = mainAct.pickUp_tmpAddress;
        }

        geocodeobject = Utils.removeWithSpace(geocodeobject);

        if (isDestinationMode == false) {
            mainAct.tempDestGeoCode = geocodeobject;
            pickUpLocTxt.setText(address);
            handlePickEditIcon();
            sourceLocSelectTxt.setText(address);
        } else {
            mainAct.tempPickupGeoCode = geocodeobject;
        }
        mainAct.onAddressFound(address);


        Location location = new Location("gps");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        if (isDestinationMode == false) {
            mainAct.pickUpLocationAddress = address;
            mainAct.currentGeoCodeObject = geocodeobject;

        }


        if (mainAct.cabSelectionFrag != null) {
            if (isDestinationMode == false) {
                isPickUpAddressStateChanged(mainAct.pickUpLocation);
            }
        }


        if (!isfirst) {
            isfirst = true;
            mainAct.uberXAddress = address;
            mainAct.uberXlat = latitude;
            mainAct.uberXlong = longitude;

            if (isDestinationMode == false) {
                pickUpLocTxt.setText(address);
                handlePickEditIcon();
                sourceLocSelectTxt.setText(address);
                Location pickUpLoc = new Location("");
                pickUpLoc.setLatitude(latitude);
                pickUpLoc.setLongitude(longitude);
                mainAct.pickUpLocation = pickUpLoc;

            }


            isDestinationMode = true;


            mainAct.configDestinationMode(isDestinationMode);
            mainAct.onAddressFound(address);
        }
        mainAct.currentGeoCodeObject = geocodeobject;
    }

    public String getAvailableCarTypesIds() {
        String carTypesIds = "";
        for (int i = 0; i < mainAct.cabTypesArrList.size(); i++) {
            String iVehicleTypeId = mainAct.cabTypesArrList.get(i).get("iVehicleTypeId");

            carTypesIds = carTypesIds.equals("") ? iVehicleTypeId : (carTypesIds + "," + iVehicleTypeId);
        }
        return carTypesIds;
    }

    public void isPickUpAddressStateChanged(Location pickupLocation) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckSourceLocationState");
        parameters.put("PickUpLatitude", pickupLocation.getLatitude() + "");
        parameters.put("PickUpLongitude", pickupLocation.getLongitude() + "");
        parameters.put("SelectedCarTypeID", getAvailableCarTypesIds());
        parameters.put("CurrentCabGeneralType", mainAct.getCurrentCabGeneralType());
        ApiHandler.execute(mainAct, parameters, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                }
            } else {

            }
        });
    }

    public void disableDestMode() {
        isDestinationMode = false;
        mainAct.configDestinationMode(isDestinationMode);
    }

    public void releaseResources() {
        if (this.gMap != null) {
            this.gMap.setOnCameraIdleListener(null);
            this.gMap = null;
            getAddressFromLocation.setAddressList(null);
            getAddressFromLocation = null;
        }
    }

    public void releaseAddressFinder() {
        if (this.gMap != null) {
            this.gMap.setOnCameraIdleListener(null);
        }
    }

    public void addAddressFinder() {
        this.gMap.setOnCameraIdleListener(new onGoogleMapCameraChangeList());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE) {
            isclickablesource = false;
        }

        if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == mainAct.RESULT_OK && data != null && gMap != null) {
            if (resultCode == mainAct.RESULT_OK) {

                LatLng pickUplocation = new LatLng(generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude")), generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude")));

                if (mainAct.pickUpLocation == null) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pickUplocation, Utils.defaultZomLevel);
                    if (gMap != null) {
                        gMap.clear();
                        gMap.moveCamera(cu);
                    }
                    onAddressFound(data.getStringExtra("Address"), pickUplocation.latitude, pickUplocation.longitude, "");
                    return;
                }

                isAddressEnable = true;

                pickUpLocTxt.setText(data.getStringExtra("Address"));
                sourceLocSelectTxt.setText(data.getStringExtra("Address"));


                CameraPosition cameraPosition = new CameraPosition.Builder().target(
                        new LatLng(pickUplocation.latitude, pickUplocation.longitude))
                        .zoom(gMap.getCameraPosition().zoom).build();

                mainAct.pickUpLocationAddress = data.getStringExtra("Address");


                if (mainAct.pickUpLocation == null) {
                    final Location location = new Location("gps");
                    location.setLatitude(pickUplocation.latitude);
                    location.setLongitude(pickUplocation.longitude);

                    mainAct.pickUpLocation = location;
                } else {
                    mainAct.pickUpLocation.setLatitude(pickUplocation.latitude);
                    mainAct.pickUpLocation.setLongitude(pickUplocation.longitude);
                }


                if (mainAct.cabSelectionFrag != null) {
                    mainAct.cabSelectionFrag.findRoute("--");
                }

                if (mainAct.cabSelectionFrag == null) {
                    gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    gMap.clear();
                }

                if (mainAct.cabSelectionFrag == null) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pickUplocation, Utils.defaultZomLevel);
                    if (gMap != null) {
                        gMap.clear();
                        gMap.moveCamera(cu);
                    } else {
                        gMap.clear();
                    }
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                generalFunc.showMessage(generalFunc.getCurrentView(getActivity()),
                        status.getStatusMessage());
            } else if (requestCode == mainAct.RESULT_CANCELED) {

            } else {

            }

        } else if (requestCode == Utils.SEARCH_DEST_LOC_REQ_CODE) {

            if (resultCode == mainAct.RESULT_OK && data != null && gMap != null) {
                isclickabledest = false;
                isDestinationMode = true;
                mainAct.isDestinationMode = true;
                isAddressEnable = true;

                destLocTxt.setText(data.getStringExtra("Address"));
                destLocSelectTxt.setText(data.getStringExtra("Address"));
                handleDestEditIcon();

                if (data.getBooleanExtra("isSkip", false)) {
                    setDestinationView(mainAct.cabSelectionFrag != null ? false : true);
                    return;
                }

                mainAct.setDestinationPoint(data.getStringExtra("Latitude"), data.getStringExtra("Longitude"), data.getStringExtra("Address"), true);

                LatLng destlocation = new LatLng(generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude")), generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude")));


                if (mainAct.cabSelectionFrag == null && !Utils.IS_KIOSK_APP) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(destlocation.latitude, destlocation.longitude)).zoom(gMap.getCameraPosition().zoom).build();

                    gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    gMap.clear();
                }

                mainAct.destAddress = data.getStringExtra("Address");
                destLocTxt.setText(data.getStringExtra("Address"));
                destLocSelectTxt.setText(data.getStringExtra("Address"));
                handleDestEditIcon();

                findRoute("--");

                if (mainAct.isMenuImageShow) {
                    menuImgView.setVisibility(View.GONE);
                    backImgView.setVisibility(View.VISIBLE);
                }
            } else {
                isclickabledest = false;
            }


        } else if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            isclickabledest = false;
            if (resultCode == mainAct.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);

                if (place == null) {
                    return;
                }

                LatLng placeLocation = place.getLatLng();

                if (placeLocation == null) {
                    return;
                }

                mainAct.setDestinationPoint(placeLocation.latitude + "", placeLocation.longitude + "", place.getAddress().toString(), true);


                destLocTxt.setText(place.getAddress().toString());
                destLocSelectTxt.setText(place.getAddress().toString());
                handleDestEditIcon();


                if (mainAct != null) {
                    mainAct.addcabselectionfragment();
                }

                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, 14.0f);

                if (mainAct.cabSelectionFrag == null) {

                    if (gMap != null) {
                        gMap.clear();
                        gMap.moveCamera(cu);
                    }
                }
                destLocTxt.setText(place.getAddress().toString());
                destLocSelectTxt.setText(place.getAddress().toString());


                if (mainAct.isMenuImageShow) {
                    menuImgView.setVisibility(View.GONE);
                    backImgView.setVisibility(View.VISIBLE);
                }


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);


                generalFunc.showMessage(generalFunc.getCurrentView(getActivity()),
                        status.getStatusMessage());
            } else if (requestCode == mainAct.RESULT_CANCELED) {

            } else {
                isclickabledest = false;

            }

        }
    }

    @Override
    public void onDestroyView() {
        removeOnLayoutChangeListener();
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());

    }

    private void removeOnLayoutChangeListener() {

        if (getView() != null) {
            getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        if (view != null) {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    public void setSourceAddress(double latitude, double longitude) {
        try {

            getAddressFromLocation.setLocation(latitude, longitude);
            getAddressFromLocation.execute();
        } catch (Exception e) {

        }
    }

    public void findRoute(String etaVal) {
        try {
            HashMap<String, String> hashMap = new HashMap<>();

            if (mainAct.destLocation != null) {
                hashMap.put("d_latitude", mainAct.getDestLocLatitude() + "");
                hashMap.put("d_longitude", mainAct.getDestLocLongitude() + "");
            } else {
                hashMap.put("d_latitude", mainAct.getPickUpLocation().getLatitude() + "");
                hashMap.put("d_longitude", mainAct.getPickUpLocation().getLongitude() + "");
            }

            isCabsLoadedIsInProcess = true;


            hashMap.put("s_latitude", mainAct.getPickUpLocation().getLatitude() + "");
            hashMap.put("s_longitude", mainAct.getPickUpLocation().getLongitude() + "");


            AppService.getInstance().executeService(mainAct.getActContext(), new DataProvider.DataProviderBuilder(hashMap.get("s_latitude"), hashMap.get("s_longitude")).setDestLatitude(hashMap.get("d_latitude")).setDestLongitude(hashMap.get("d_longitude")).setWayPoints(new JSONArray()).build(), AppService.Service.DIRECTION, new AppService.ServiceDelegate() {
                @Override
                public void onResult(HashMap<String, Object> data) {
                    if (data.get("RESPONSE_TYPE") != null && data.get("RESPONSE_TYPE").toString().equalsIgnoreCase("FAIL")) {
                        isRouteFail = true;
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_DEST_ROUTE_NOT_FOUND"));
                        return;
                    }
                    if (data.get("ROUTES") != null) {
                        responseString = data.get("ROUTES").toString();
                    }

                    isRouteFail = false;

                    if (responseString != null && !responseString.equalsIgnoreCase("") && data.get("DISTANCE") == null) {
                        isGoogle = true;


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


                            new Handler().postDelayed(() -> handleMapAnimation(responseString, sourceLocation, destLocation, "--", false), 250);

                            if (getActivity() != null) {
                                estimateFare(distance, time);
                            }
                        }
                    } else {
                        if (responseString == null) {
                            isRouteFail = true;
                            GenerateAlertBox alertBox = new GenerateAlertBox(mainAct.getActContext());
                            alertBox.setContentMessage("", generalFunc.retrieveLangLBl("Route not found", "LBL_DEST_ROUTE_NOT_FOUND"));
                            alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                            alertBox.setBtnClickList(btn_id -> {
                                alertBox.closeAlertBox();
                            });
                            alertBox.showAlertBox();
                            estimateFare(null, null);
                            return;
                        } else {
                            isGoogle = false;
                            distance = data.get("DISTANCE").toString();
                            time = data.get("DURATION").toString();


                            sourceLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, hashMap.get("s_latitude")), GeneralFunctions.parseDoubleValue(0.0, hashMap.get("s_longitude"))
                            );

                            destLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, hashMap.get("d_latitude")), GeneralFunctions.parseDoubleValue(0.0, hashMap.get("d_longitude"))
                            );

                            HashMap<String, Object> data_dict = new HashMap<>();
                            data_dict.put("routes", data.get("ROUTES"));
                            responseString = data_dict.toString();
                            new Handler().postDelayed(() -> handleMapAnimation(responseString, sourceLocation, destLocation, "--", false), 250);
                            if (getActivity() != null) {
                                estimateFare(distance, time);
                            }
                        }
                    }


                }
            });


        } catch (Exception e) {

        }
    }

    public void estimateFare(final String distance, String time) {

        if (estimateFareTask != null) {
            estimateFareTask.cancel(true);
            estimateFareTask = null;
        }
        if (distance == null && time == null) {
            return;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "estimateFareNew");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("SelectedCarTypeID", mainAct.selectedCabTypeId);
        if (distance != null && time != null) {
            parameters.put("distance", distance);
            parameters.put("time", time);
        }
        parameters.put("SelectedCar", mainAct.getSelectedCabTypeId());
        parameters.put("PromoCode", "");

        if (mainAct.destLocation != null) {
            parameters.put("DestLatitude", "" + mainAct.getDestLocLatitude());
            parameters.put("DestLongitude", "" + mainAct.getDestLocLongitude());
        }

        if (mainAct.getPickUpLocation() != null) {
            parameters.put("StartLatitude", "" + mainAct.getPickUpLocation().getLatitude());
            parameters.put("EndLongitude", "" + mainAct.getPickUpLocation().getLongitude());
        }


        estimateFareTask = ApiHandler.execute(mainAct, parameters, responseString -> {

            if (responseString != null && !responseString.equals("")) {


                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                    JSONArray vehicleTypesArr = generalFunc.getJsonArray(Utils.message_str, responseString);
                    for (int i = 0; i < vehicleTypesArr.length(); i++) {

                        JSONObject obj_temp = generalFunc.getJsonObject(vehicleTypesArr, i);

                        if (distance != null) {

                            if (mainAct.selectedCabTypeId.equalsIgnoreCase(generalFunc.getJsonValue("iVehicleTypeId", obj_temp.toString()))) {
                                String totalfare = generalFunc.getJsonValue("total_fare", obj_temp.toString());
                                mainAct.setTotalFare(totalfare, distance, time);
                            }

                        } else {

                        }

                    }
                    isCabsLoadedIsInProcess = false;

                }
            }
        });

    }

    public void handleMapAnimation(String responseString, LatLng sourceLocation, LatLng destLocation, String etaVal, boolean buildBounds) {
        try {
            if (mainAct == null) {
                return;
            }

            handleSourceMarker(etaVal, buildBounds);

            PolyLineAnimator.getInstance().stopRouteAnim();

            LatLng fromLnt = new LatLng(sourceLocation.latitude, sourceLocation.longitude);
            LatLng toLnt = new LatLng(destLocation.latitude, destLocation.longitude);


            if (marker_view == null) {

                marker_view = ((LayoutInflater) mainAct.getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.custom_marker, null);
                addressTxt = (MTextView) marker_view
                        .findViewById(R.id.addressTxt);
                etaTxt = (MTextView) marker_view.findViewById(R.id.etaTxt);
            }

            addressTxt.setTextColor(mainAct.getActContext().getResources().getColor(R.color.destAddressTxt));


            addressTxt.setText(mainAct.destAddress);

            MarkerOptions marker_opt_dest = new MarkerOptions().position(toLnt);
            etaTxt.setVisibility(View.GONE);

            marker_opt_dest.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(mainAct.getActContext(), marker_view))).anchor(0.00f, 0.20f);
            if (dest_dot_option != null) {
                destDotMarker.remove();
            }
            dest_dot_option = new MarkerOptions().position(toLnt).icon(BitmapDescriptorFactory.fromResource(R.mipmap.dot));
            destDotMarker = mainAct.getMap().addMarker(dest_dot_option);

            if (destMarker != null) {
                destMarker.remove();
            }
            destMarker = mainAct.getMap().addMarker(marker_opt_dest);
            destMarker.setTag("2");
       /* LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(fromLnt);
        builder.include(toLnt);*/

            if (Utils.IS_KIOSK_APP) {
                buildBuilder();
            }

            JSONArray obj_routes1 = generalFunc.getJsonArray("routes", responseString);


            if (obj_routes1 != null && obj_routes1.length() > 0) {
                PolylineOptions lineOptions;
                if (isGoogle) {
                    lineOptions = getGoogleRouteOptions(responseString, Utils.dipToPixels(mainAct.getActContext(), 5), mainAct.getActContext().getResources().getColor(android.R.color.black));
                } else {

                    lineOptions = getGoogleRouteOptionsHandle(responseString, Utils.dipToPixels(mainAct, 5), mainAct.getResources().getColor(R.color.black));
                }
                if (lineOptions != null) {
                    if (route_polyLine != null) {
                        route_polyLine.remove();
                        route_polyLine = null;

                    }
                    route_polyLine = mainAct.getMap().addPolyline(lineOptions);
                    route_polyLine.remove();
                }
            }

            DisplayMetrics metrics = new DisplayMetrics();
            mainAct.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
//        mainAct.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width - Utils.dpToPx(getActContext(), 80), metrics.heightPixels - Utils.dipToPixels(getActContext(), 300), 0));

            if (route_polyLine != null && route_polyLine.getPoints().size() > 1) {
                PolyLineAnimator.getInstance().animateRoute(mainAct.getMap(), route_polyLine.getPoints(), mainAct.getActContext());
            }

            mainAct.getMap().setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {

                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    mainAct.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int height = displaymetrics.heightPixels;
                    int width = displaymetrics.widthPixels;


                }
            });

            if (mainAct.loadAvailCabs != null) {
                mainAct.loadAvailCabs.changeCabs();
            }


        } catch (Exception e) {
            // Backpress done by user then app crashes

            e.printStackTrace();
        }

    }

    public PolylineOptions getGoogleRouteOptionsHandle(String directionJson, int width, int color) {
        PolylineOptions lineOptions = new PolylineOptions();


        try {
            JSONArray obj_routes1 = generalFunc.getJsonArray("routes", directionJson);

            ArrayList<LatLng> points = new ArrayList<LatLng>();

            if (obj_routes1.length() > 0) {
                // Fetching i-th route
                // Fetching all the points in i-th route
                for (int j = 0; j < obj_routes1.length(); j++) {

                    JSONObject point = generalFunc.getJsonObject(obj_routes1, j);

                    LatLng position = new LatLng(GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("latitude", point).toString()), GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("longitude", point).toString()));


                    points.add(position);

                }


                lineOptions.addAll(points);
                lineOptions.width(width);
                lineOptions.color(color);

                return lineOptions;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        addGlobalLayoutListner();
    }

    private void addGlobalLayoutListner() {

        removeOnLayoutChangeListener();

        if (getView() != null) {

            getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        } else if (view != null) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    public void handleSourceMarker(String etaVal, boolean buildBounds) {

        try {

            if (mainAct.pickUpLocation == null) {
                return;
            }

            if (marker_view == null) {
                marker_view = ((LayoutInflater) mainAct.getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.custom_marker, null);
                addressTxt = (MTextView) marker_view
                        .findViewById(R.id.addressTxt);
                etaTxt = (MTextView) marker_view.findViewById(R.id.etaTxt);
            }

            if (marker_view != null) {
                etaTxt = (MTextView) marker_view.findViewById(R.id.etaTxt);
            }

            addressTxt.setTextColor(mainAct.getActContext().getResources().getColor(R.color.sourceAddressTxt));

            LatLng fromLnt;

            if (!mainAct.getDestinationStatus()) {
                if (destMarker != null) {
                    destMarker.remove();
                }
                if (destDotMarker != null) {
                    destDotMarker.remove();
                }
                if (route_polyLine != null) {
                    route_polyLine.remove();
                }

                destLocation = null;
                mainAct.destLocation = null;
            }

            fromLnt = new LatLng(mainAct.pickUpLocation.getLatitude(), mainAct.pickUpLocation.getLongitude());

            etaTxt.setVisibility(View.VISIBLE);
            etaTxt.setText(etaVal);

            if (sourceMarker != null) {
                sourceMarker.remove();
                sourceMarker = null;
            }

            if (source_dot_option != null) {
                if (sourceDotMarker != null) {
                    sourceDotMarker.remove();
                    sourceDotMarker = null;
                }

                source_dot_option = null;
            }

            source_dot_option = new MarkerOptions().position(fromLnt).icon(BitmapDescriptorFactory.fromResource(R.mipmap.dot));

            if (mainAct.getMap() != null) {
                sourceDotMarker = mainAct.getMap().addMarker(source_dot_option);
            }
            addressTxt.setText(mainAct.pickUpLocationAddress);
            MarkerOptions marker_opt_source = new MarkerOptions().position(fromLnt).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(mainAct.getActContext(), marker_view))).anchor(0.00f, 0.20f);
            if (mainAct.getMap() != null) {
                sourceMarker = mainAct.getMap().addMarker(marker_opt_source);
                sourceMarker.setTag("1");
            }

            if (buildBounds) {
                buildBuilder();
            }

        } catch (Exception e) {
            // Backpress done by user then app crashes
            e.printStackTrace();
        }
    }


    public void buildBuilder() {
        if (mainAct == null) {
            return;
        }
        if (sourceMarker != null && (destMarker == null)) {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            if (sourceMarker != null) {
                builder.include(sourceMarker.getPosition());

                DisplayMetrics metrics = new DisplayMetrics();
                mainAct.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                //int padding = (mainAct != null) ? (width != 0 ? (int) (width / 5) : 0) : 0; // offset from edges of the map in pixels
                int padding = 0;
                
                LatLngBounds bounds = builder.build();
                LatLng center = bounds.getCenter();
                LatLng northEast = SphericalUtil.computeOffset(center, 30 * Math.sqrt(2.0), SphericalUtil.computeHeading(center, bounds.northeast));
                LatLng southWest = SphericalUtil.computeOffset(center, 30 * Math.sqrt(2.0), (180 + (180 + SphericalUtil.computeHeading(center, bounds.southwest))));
                builder.include(southWest);
                builder.include(northEast);

                mainAct.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width - Utils.dipToPixels(mainAct.getActContext(), 80), height - ((fragmentHeight + 5) + Utils.dipToPixels(mainAct.getActContext(), 60)), padding));
            }

            addDestinationIfExist();

        } else if (mainAct.map != null && mainAct.map.getView() != null && mainAct.map.getView().getViewTreeObserver().isAlive()) {
            mainAct.map.getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {

                   /* if (Utils.IS_KIOSK_APP) {
                        mainAct.userLocBtnImgView.performClick();

                    } else {*/
                    boolean isBoundIncluded = false;

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    if (sourceMarker != null) {
                        isBoundIncluded = true;
                        builder.include(sourceMarker.getPosition());
                    }


                    if (destMarker != null) {
                        isBoundIncluded = true;
                        builder.include(destMarker.getPosition());
                    }


                    if (isBoundIncluded) {

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            mainAct.map.getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            mainAct.map.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }


                        LatLngBounds bounds = builder.build();


                        LatLng center = bounds.getCenter();

                        LatLng northEast = SphericalUtil.computeOffset(center, 10 * Math.sqrt(2.0), SphericalUtil.computeHeading(center, bounds.northeast));
                        LatLng southWest = SphericalUtil.computeOffset(center, 10 * Math.sqrt(2.0), (180 + (180 + SphericalUtil.computeHeading(center, bounds.southwest))));

                        builder.include(southWest);
                        builder.include(northEast);


                        /*  Method 1 */
//                            mainAct.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                        DisplayMetrics metrics = new DisplayMetrics();
                        mainAct.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                        int width = metrics.widthPixels;
                        int height = metrics.heightPixels;
                        // Set Padding according to included bounds

                        /*  Method 2 */
                            /*Utils.printELog("MapHeight","newLatLngZoom");
                            mainAct.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(),16));*/


                        int padding = (int) (width * 0.25); // offset from edges of the map 10% of screen
                        try {
                            /*  Method 3 */
                            int screenWidth = getResources().getDisplayMetrics().widthPixels;
                            int screenHeight = getResources().getDisplayMetrics().heightPixels;
                            padding = (height - ((fragmentHeight + 5) + Utils.dipToPixels(mainAct.getActContext(), 60))) / 3;
//                                Utils.printELog("MapHeight", "cameraUpdate" + padding);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,
                                    screenWidth, screenHeight, padding);
                            mainAct.getMap().animateCamera(cameraUpdate);
                        } catch (Exception e) {
                            e.printStackTrace();

                            /*  Method 1 */
                            mainAct.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width - Utils.dipToPixels(mainAct.getActContext(), 80), height - ((fragmentHeight + 5) + Utils.dipToPixels(mainAct.getActContext(), 60)), padding));
                        }
                    }


                    if (Utils.IS_KIOSK_APP) {
                        mainAct.userLocBtnImgView.performClick();

                    }

                    // }

                }
            });
        } /*else if (sourceMarker != null && destMarker != null && Utils.IS_KIOSK_APP) {
            mainAct.userLocBtnImgView.performClick();

        }*/
    }

    private void addDestinationIfExist() {

        if (mainAct.isDestinationAdded && isfirstly == false) {
            isfirstly = true;
            isclickabledest = false;
            isDestinationMode = true;
            mainAct.isDestinationMode = true;
            isAddressEnable = true;

            destLocTxt.setText(mainAct.getDestAddress().toString());
            destLocSelectTxt.setText(mainAct.getDestAddress().toString());
            handleDestEditIcon();


            LatLng destlocation = new LatLng(generalFunc.parseDoubleValue(0.0, mainAct.getDestLocLatitude()), generalFunc.parseDoubleValue(0.0, mainAct.getDestLocLongitude()));

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(destlocation.latitude, destlocation.longitude)).zoom(gMap.getCameraPosition().zoom).build();


            if (mainAct.cabSelectionFrag == null) {
                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else {
                gMap.clear();
            }

            destLocTxt.setText(mainAct.getDestAddress().toString());
            destLocSelectTxt.setText(mainAct.getDestAddress().toString());

            handleDestEditIcon();

            findRoute("--");

            if (mainAct.isMenuImageShow) {
                menuImgView.setVisibility(View.GONE);
                backImgView.setVisibility(View.VISIBLE);
            }
        } else {
            isclickabledest = false;
        }
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

//            Utils.printELog("FragHeight", "is :::" + fragmentHeight + "\n" + "Frag Width is :::" + fragmentWidth);

            if (heightChanged && fragmentWidth != 0 && fragmentHeight != 0) {
                mainAct.setPanelHeight(fragmentHeight);
            } else if (!heightChanged && fragmentWidth != 0 && fragmentHeight != 0) {
                removeOnLayoutChangeListener();
            }
        }
    }

    public PolylineOptions getGoogleRouteOptions(String directionJson, int width, int color) {
        PolylineOptions lineOptions = new PolylineOptions();

        try {
            DirectionsJSONParser parser = new DirectionsJSONParser();
            List<List<HashMap<String, String>>> routes_list = parser.parse(new JSONObject(directionJson));

            ArrayList<LatLng> points = new ArrayList<LatLng>();

            if (routes_list.size() > 0) {
                // Fetching i-th route
                List<HashMap<String, String>> path = routes_list.get(0);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);

                }

                lineOptions.addAll(points);
                lineOptions.width(width);
                lineOptions.color(color);

                return lineOptions;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void mangeMrakerPostion() {
       /* try {

            if (mainAct.pickUpLocation != null) {
                Point PickupPoint = mainAct.getMap().getProjection().toScreenLocation(new LatLng(mainAct.pickUpLocation.getLatitude(), mainAct.pickUpLocation.getLongitude()));
                if (sourceMarker != null) {
                    sourceMarker.setAnchor(PickupPoint.x < Utils.dpToPx(mainAct.getActContext(), 200) ? 0.00f : 1.00f, PickupPoint.y < Utils.dpToPx(mainAct.getActContext(), 100) ? 0.20f : 1.20f);
                }
            }
            if (destLocation != null) {
                Point DestinationPoint = mainAct.getMap().getProjection().toScreenLocation(destLocation);
                //dest
                if (destMarker != null) {
                    destMarker.setAnchor(DestinationPoint.x < Utils.dpToPx(mainAct.getActContext(), 200) ? 0.00f : 1.00f, DestinationPoint.y < Utils.dpToPx(mainAct.getActContext(), 100) ? 0.20f : 1.20f);
                }
            }
        } catch (Exception e) {

        }*/


    }


    String responseString;
    boolean isGoogle = false;


    public class setOnClickList implements View.OnClickListener, BounceAnimation.BounceAnimListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActivity());
            int id = view.getId();
            if (id == destarea.getId() || id == area_source.getId()) {
//                BounceAnimation.setBounceAnimation(mainAct.getActContext(),view);
//                BounceAnimation.setBounceAnimListener(this);
                destinationSelected();
            } else if (view.getId() == pickupLocArea1.getId()) {
                try {
                    if (!isclickablesource) {
                        isclickablesource = true;
                        LatLng pickupPlaceLocation = null;
                        Bundle bn = new Bundle();
                        bn.putString("locationArea", "source");
                        bn.putBoolean("isDriverAssigned", mainAct.isDriverAssigned);
                        if (mainAct.pickUpLocation != null) {
                            pickupPlaceLocation = new LatLng(mainAct.pickUpLocation.getLatitude(),
                                    mainAct.pickUpLocation.getLongitude());
                            bn.putDouble("lat", pickupPlaceLocation.latitude);
                            bn.putDouble("long", pickupPlaceLocation.longitude);
                            bn.putString("address", mainAct.pickUpLocationAddress);
                        } else {
                            bn.putDouble("lat", 0.0);
                            bn.putDouble("long", 0.0);
                            bn.putString("address", "");
                        }
                        bn.putString("type", mainAct.getCurrentCabGeneralType());
                        new ActUtils(mainAct.getActContext()).startActForResult(mainHeaderFrag, SearchLocationActivity.class,
                                Utils.SEARCH_PICKUP_LOC_REQ_CODE, bn);
                    }
                } catch (Exception e) {

                }
            } else if (view.getId() == R.id.sourceLocSelectTxt) {
//                Utils.printELog("ExceptionSourceLocSelect", ":sourceLocSelectTxt::");
                try {
                    if (Utils.checkText(mainAct.pickUpLocationAddress)) {
                        isAddressEnable = true;
                    }

                    disableDestMode();

                    if (mainAct.getDestinationStatus() == true) {
                        destLocSelectTxt.setText(mainAct.getDestAddress());
                        handleDestEditIcon();
                    } else {
                        destLocSelectTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_DESTINATION_BTN_TXT"));
                        handleDestAddIcon();
                    }
                } catch (Exception e) {
//                    Utils.printELog("ExceptionSourceLocSelect", ":::" + e.toString());
                }
            } else if (view.getId() == R.id.destLocSelectTxt) {
                if (mainAct.getDestinationStatus()) {
                    isAddressEnable = true;
                }

                if (mainAct.pickUpLocation != null) {
                    isDestinationMode = true;
                    mainAct.configDestinationMode(isDestinationMode);
                    if (mainAct.getDestinationStatus() == false || Utils.IS_KIOSK_APP) {
                        new Handler().postDelayed(() -> destarea.performClick(), 250);
                    }
                }
            } else if (view.getId() == backImgView.getId()) {
                if (mainAct.isMenuImageShow) {
                    menuImgView.setVisibility(View.VISIBLE);
                    backImgView.setVisibility(View.GONE);
                } else {
                    menuImgView.setVisibility(View.GONE);
                    backImgView.setVisibility(View.GONE);
                }
                mainAct.onBackPressed();
            } else if (view.getId() == menuImgView.getId()) {
            }
        }

        @Override
        public void onAnimationFinished(View view) {

            if (view == destarea) {
                destinationSelected();
            }
        }

        private void destinationSelected() {
            try {
                if (mainAct.pickUpLocationAddress != null && !mainAct.pickUpLocationAddress.equals("")) {
                    if (!isclickabledest) {
                        isclickabledest = true;
                        isDestinationMode = true;
                        LatLng pickupPlaceLocation = null;

                        Bundle bn = new Bundle();
                        bn.putString("locationArea", "dest");
                        bn.putBoolean("isDriverAssigned", mainAct.isDriverAssigned);

                        if (mainAct.pickUpLocation != null) {
                            pickupPlaceLocation = new LatLng(mainAct.pickUpLocation.getLatitude(),
                                    mainAct.pickUpLocation.getLongitude());
                            bn.putDouble("lat", pickupPlaceLocation.latitude);
                            bn.putDouble("long", pickupPlaceLocation.longitude);
                            bn.putString("address", mainAct.pickUpLocationAddress);
                        } else {
                            bn.putDouble("lat", 0.0);
                            bn.putDouble("long", 0.0);
                            bn.putString("address", "");

                        }

                        if (mainAct.destLocation != null && mainAct.destLocation.getLatitude() != 0.0) {
                            bn.putDouble("lat", mainAct.destLocation.getLatitude());
                            bn.putDouble("long", mainAct.destLocation.getLongitude());
                            bn.putString("address", mainAct.destAddress);

                        }

                        bn.putString("type", mainAct.getCurrentCabGeneralType());
                        new ActUtils(mainAct.getActContext()).startActForResult(mainHeaderFrag, SearchLocationActivity.class,
                                Utils.SEARCH_DEST_LOC_REQ_CODE, bn);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    public class onGoogleMapCameraChangeList implements GoogleMap.OnCameraIdleListener {
        @Override
        public void onCameraIdle() {
            if (getAddressFromLocation == null) {
                return;
            }

            mangeMrakerPostion();

            LatLng center = null;
            if (gMap != null && gMap.getCameraPosition() != null) {
                center = gMap.getCameraPosition().target;
            }

            if (center == null) {
                return;
            }

//            Utils.printELog("cameraPosition", "::MainHeader::" + isAddressEnable);
            if (!isAddressEnable) {
                setSourceAddress(center.latitude, center.longitude);

                mainAct.onMapCameraChanged();
            } else {
                isAddressEnable = false;
            }
        }
    }


}
