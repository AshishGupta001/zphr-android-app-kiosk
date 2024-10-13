package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zphr.kiosk.R;
import com.zphr.kiosk.SearchLocationActivity;
import com.zphr.kiosk.KioskLandingScreenActivity;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

public class OpenNoLocationView implements GetLocationUpdates.LocationUpdates {
    ViewGroup viewGroup;
    Activity currentAct;

    GetLocationUpdates getLocUpdate;

    View noLocView;

    private static OpenNoLocationView currentInst;
    private boolean isViewExecutionLocked = false;

    public static OpenNoLocationView getInstance(Activity currentAct, ViewGroup viewGroup) {
        if (currentInst == null) {
            currentInst = new OpenNoLocationView();
        }

        currentInst.viewGroup = viewGroup;
        currentInst.currentAct = currentAct;

        return currentInst;
    }

    public void configView(boolean isFromNetwork) {
        if (viewGroup != null && currentAct != null) {

            if (isViewExecutionLocked == true) {
                return;
            }

            isViewExecutionLocked = true;

            GeneralFunctions generalFunc = new GeneralFunctions(MyApp.getInstance().getCurrentAct());

            closeView(generalFunc);

            boolean isNetworkConnected = new InternetConnection(currentAct).isNetworkConnected();
            LayoutInflater inflater = (LayoutInflater) currentAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View noLocView = inflater.inflate(R.layout.desgin_no_locatin_view, null);

            ImageView noLocMenuImgView = (ImageView) noLocView.findViewById(R.id.noLocMenuImgView);
            ImageView noLocImgView = (ImageView) noLocView.findViewById(R.id.noLocImgView);

            MTextView noLocTitleTxt = (MTextView) noLocView.findViewById(R.id.noLocTitleTxt);
            MTextView noLocMsgTxt = (MTextView) noLocView.findViewById(R.id.noLocMsgTxt);

            MButton settingsBtn = ((MaterialRippleLayout) noLocView.findViewById(R.id.settingsBtn)).getChildView();
            MButton enterLocBtn = ((MaterialRippleLayout) noLocView.findViewById(R.id.enterLocBtn)).getChildView();


            settingsBtn.setBackgroundColor(Color.parseColor("#FFFFFF"));
            settingsBtn.setTextColor(Color.parseColor("#000000"));

            enterLocBtn.setBackgroundColor(Color.parseColor("#000000"));
            enterLocBtn.setTextColor(Color.parseColor("#FFFFFF"));

            enterLocBtn.setText(generalFunc.retrieveLangLBl("", "LBL_ENTER_PICK_UP_ADDRESS"));

            if (currentAct instanceof KioskLandingScreenActivity) {
                noLocMenuImgView.setVisibility(View.INVISIBLE);
                noLocView.setPadding(0, getActionBarHeight(), 0, 0);
            }


            noLocMenuImgView.setOnClickListener(v -> {

            });
            settingsBtn.setOnClickListener(v -> {
                if (isNetworkConnected) {
                    new ActUtils(MyApp.getInstance().getCurrentAct()).
                            startActForResult(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Utils.REQUEST_CODE_GPS_ON);
                } else {
                    new ActUtils(MyApp.getInstance().getCurrentAct()).
                            startActForResult(Settings.ACTION_SETTINGS, Utils.REQUEST_CODE_NETWOEK_ON);
                }
            });

            enterLocBtn.setOnClickListener(v -> {
//                configView();

//                if (currentAct instanceof UberXActivity) {
//                    try {
//                        LatLngBounds bounds = null;
//                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
//                                .setBoundsBias(bounds)
//                                .build(currentAct);
//                        currentAct.startActivityForResult(intent, Utils.SEARCH_PICKUP_LOC_REQ_CODE);
//                    } catch (Exception e) {
//
//                    }
//                } else if (currentAct instanceof MainActivity) {
                    Bundle bn = new Bundle();
                    bn.putString("locationArea", "source");
                    bn.putDouble("lat", 0.0);
                    bn.putDouble("long", 0.0);
                    new ActUtils(currentAct).startActForResult(SearchLocationActivity.class, bn, Utils.SEARCH_PICKUP_LOC_REQ_CODE);
//                }

            });

            if (isNetworkConnected == false) {

                currentInst.noLocView = noLocView;

                (noLocView.findViewById(R.id.enterLocBtn)).setVisibility(View.INVISIBLE);

                noLocImgView.setImageResource(R.mipmap.ic_wifi_off);

                settingsBtn.setText(generalFunc.retrieveLangLBl("", "LBL_SETTINGS"));
                noLocTitleTxt.setText(generalFunc.retrieveLangLBl("Internet Connection", "LBL_NO_INTERNET_TITLE"));
                noLocMsgTxt.setText(generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_SUB_TITLE"));

                addView(noLocView, "NO_INTERNET", generalFunc);

                isViewExecutionLocked = false;
                return;
            }

            /*if (!generalFunc.isLocationEnabled()) {

                if (currentAct instanceof KioskLandingScreenActivity && (((KioskLandingScreenActivity) currentAct).pickUpLocation != null && ((KioskLandingScreenActivity) currentAct).pickUpLocation.getLatitude() != 0.0 && ((KioskLandingScreenActivity) currentAct).pickUpLocation.getLatitude() != 0.0)) {
                    closeView(generalFunc);
                    isViewExecutionLocked = false;
                    return;
                }

                noLocImgView.setImageResource(R.mipmap.ic_gps_off);

                settingsBtn.setText(generalFunc.retrieveLangLBl("", "LBL_TURN_ON_LOC_SERVICE"));
                noLocTitleTxt.setText(generalFunc.retrieveLangLBl("Enable Location Service", "LBL_LOCATION_SERVICES_TURNED_OFF"));
                noLocMsgTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LOCATION_SERVICES_TURNED_OFF_DETAILS"));

                addView(noLocView, "NO_LOCATION", generalFunc);

                isViewExecutionLocked = false;
                return;
            } else if (generalFunc.isLocationEnabled()) {

                if (currentAct instanceof KioskLandingScreenActivity && ((KioskLandingScreenActivity) currentAct).pickUpLocation != null && (((KioskLandingScreenActivity) currentAct).pickUpLocation.getLatitude() != 0.0 && ((KioskLandingScreenActivity) currentAct).pickUpLocation.getLatitude() != 0.0)) {
                    closeView(generalFunc);
                    isViewExecutionLocked = false;
                    return;
                }

                if (getLocUpdate != null) {
                    getLocUpdate.stopLocationUpdates();
                    getLocUpdate = null;
                }

                getLocUpdate = new GetLocationUpdates(currentAct, Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true, this);

            }*/

        } else {
//            Utils.printELog("AssertError", "ViewGroup OR Activity cannot be null");
        }
        isViewExecutionLocked = false;
    }


    public int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (currentAct.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, currentAct.getResources().getDisplayMetrics());
        } else {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, currentAct.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    private void addView(View noLocView, String type, GeneralFunctions generalFunc) {
        closeView(generalFunc);
        currentInst.noLocView = noLocView;

        View rootView = generalFunc.getCurrentView(currentAct);

        if (viewGroup.getChildCount() > 0) {
            rootView = viewGroup.getChildAt(0);
        }

        if (rootView instanceof DrawerLayout) {
            RelativeLayout childView = null;

            for (int i = 0; i < ((DrawerLayout) rootView).getChildCount(); i++) {
                View tmp_childView = ((DrawerLayout) rootView).getChildAt(i);
                DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) tmp_childView.getLayoutParams();
                if (params.gravity != GravityCompat.START && params.gravity != Gravity.START && tmp_childView instanceof RelativeLayout) {
                    childView = (RelativeLayout) tmp_childView;
                    break;
                }
            }

            if (childView != null) {
                noLocView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                childView.addView(noLocView);

                addNoLocationView(viewGroup, childView, noLocView);
                childView.bringChildToFront(noLocView);

            } else {
//                viewGroup.addView(noLocView);
                addNoLocationView(viewGroup, null, noLocView);
                viewGroup.bringChildToFront(noLocView);
            }
        } else {
            addNoLocationView(viewGroup, null, noLocView);
            viewGroup.bringChildToFront(noLocView);
//            viewGroup.addView(noLocView);
        }
    }

    private void addNoLocationView(ViewGroup viewGroup, RelativeLayout childView, View noLocView) {
        if (childView != null) {
            childView.addView(noLocView);
        } else {
            viewGroup.addView(noLocView);
        }
    }

    private void closeView(GeneralFunctions generalFunc) {

        if (noLocView != null || currentAct.findViewById(R.id.noLocView) != null) {
            try {
                View rootView = generalFunc.getCurrentView(currentAct);

                if (viewGroup.getChildCount() > 0) {
                    rootView = viewGroup.getChildAt(0);
                }

                if (rootView instanceof DrawerLayout) {
                    RelativeLayout childView = null;
                    for (int i = 0; i < ((DrawerLayout) rootView).getChildCount(); i++) {
                        View tmp_childView = ((DrawerLayout) rootView).getChildAt(i);
                        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) tmp_childView.getLayoutParams();
                        if ((params.gravity != GravityCompat.START && params.gravity != Gravity.START) && tmp_childView instanceof RelativeLayout) {
                            childView = (RelativeLayout) tmp_childView;
                            break;
                        }
                    }

                    if (childView != null) {
                        childView.removeView(noLocView);
                    } else {
                        viewGroup.removeView(noLocView);
                    }
                } else {
                    viewGroup.removeView(noLocView);
                }

                noLocView = null;
//                Utils.printELog("ViewRemove", ":Success:");

            } catch (Exception e) {
//                Utils.printELog("ViewRemove", ":Exception:" + e.getMessage());
            }
        }
    }

    @Override
    public void onLocationUpdate(Location location) {

        if (location == null) {
            return;
        }
        if (getLocUpdate != null) {
            getLocUpdate.stopLocationUpdates();
            getLocUpdate = null;
        }

        if (currentAct instanceof KioskLandingScreenActivity) {
            ((KioskLandingScreenActivity) currentAct).onLocationUpdate(location);
        }

    }
}

