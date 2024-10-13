package com.zphr.kiosk;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.activity.ParentActivity;
import com.general.files.MyApp;
import com.utils.CommonUtilities;
import com.utils.LoadImage;
import com.utils.Utils;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONObject;

public class KioskBookingDetailsActivity extends ParentActivity {

    MTextView txtTimer;
    MTextView bookingmsgtxt;
    MTextView bookingidvaltxt;


    int maxProgressValue = 30;

    private long totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
    // milliseconds
    private long timeBlinkInMilliseconds = 10 * 1000; // start time of start blinking

    private boolean blink;
    ImageView backImgView;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kiosk_activity_booking_details);

        getBindView();
        setData();
        maxProgressValue = generalFunc.parseIntegerValue(30, generalFunc.getJsonValue("RIDER_REQUEST_ACCEPT_TIME", generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));
        totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
        txtTimer.setText("00" + generalFunc.retrieveLangLBl("", "LBL_SECONDS_TXT"));

        startTimer();

    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 1000) {
            // 1000 means, onTick function will be called at every 1000
            // milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                if ((seconds % 5) == 0) {
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    } catch (Exception e) {
                        Log.e("Exception ", e.toString());
                        e.printStackTrace();
                    }
                }

                String myString = /*String.format("%02d", seconds / 60) + ":" +*/ String.format("%02d", seconds % 60);
//                Utils.printLog("timer::", String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));
                txtTimer.setText(myString + " " + generalFunc.retrieveLangLBl("", "LBL_SECONDS_TXT"));
            }

            @Override
            public void onFinish() {

                txtTimer.setVisibility(View.VISIBLE);
                txtTimer.setText(generalFunc.retrieveLangLBl("", "LBL_TIMER_FINISHED_TXT"));
                MyApp.getInstance().restartWithGetDataApp();
            }

        }.start();

    }

    private void getBindView() {
        txtTimer = (MTextView) findViewById(R.id.txtTimer);
        txtTimer.setVisibility(View.VISIBLE);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        addToClickHandler(backImgView);
        bookingidvaltxt = (MTextView) findViewById(R.id.bookingidvaltxt);
        bookingmsgtxt = (MTextView) findViewById(R.id.bookingmsgtxt);
        bookingmsgtxt.setText(generalFunc.retrieveLangLBl("", "LBL_BOOKING_CONFIRMED"));
        ((MTextView) findViewById(R.id.bookinidtxt)).setText(generalFunc.retrieveLangLBl("", "LBL_YOUR_BOOKING_ID"));
        ((MTextView) findViewById(R.id.PickHtxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PICK_UP_FROM") + ": ");
        ((MTextView) findViewById(R.id.txtTimerTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_BOOKING_SCREEN_NOTE"));
    }

    public Context getActContext() {
        return KioskBookingDetailsActivity.this;
    }

    private void setData() {
        String message = getIntent().getStringExtra("message");

        Log.e("tripDataMap==>", "" + message);
        ((MTextView) findViewById(R.id.driver_name)).setText(generalFunc.getJsonValue("driverName", message));
        ((MTextView) findViewById(R.id.driver_mobile_no)).setText(generalFunc.getJsonValue("driverPhone", message));
        ((MTextView) findViewById(R.id.driver_car_name)).setText(generalFunc.getJsonValue("DriverVehicleMakeModel", message));
        ((MTextView) findViewById(R.id.numberPlate_txt)).setText(generalFunc.getJsonValue("DriverVehicleLicencePlate", message));
        ((MTextView) findViewById(R.id.bookingidvaltxt)).setText(generalFunc.getJsonValue("vRideNo", message));
        ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(generalFunc.parseFloatValue(0, generalFunc.getJsonValue("driverRating", message)));

        JSONObject obj_hotelProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.HOTEL_PROFILE_JSON));

        String hotelVPickupFrom = generalFunc.getJsonValueStr("vPickupFrom", obj_hotelProfile);
        String vPickupFrom = Utils.checkText(hotelVPickupFrom) ? hotelVPickupFrom : "--";

        String messageVPickupFrom = generalFunc.getJsonValue("vPickupFrom", message);
        ((MTextView) findViewById(R.id.Picktxt)).setText(Utils.checkText(messageVPickupFrom) ? messageVPickupFrom : vPickupFrom);

        System.out.println("ratingDriverRating:" + generalFunc.getJsonValue("driverRating", message));


        String vehicleIconPath = CommonUtilities.SERVER_URL + "webimages/icons/VehicleType/";
        String imgName = generalFunc.getImageName(getIntent().getStringExtra("vLogo1"), getActContext());
        String imgUrl = vehicleIconPath + getIntent().getStringExtra("iVehicleTypeId") + "/android/" + imgName;


        new LoadImage.builder(LoadImage.bind(imgUrl), findViewById(R.id.car_img)).setErrorImagePath(R.mipmap.ic_car_lux).setPlaceholderImagePath(R.mipmap.ic_car_lux).build();


        String driverImageName = generalFunc.getJsonValue("driverImage", message);

        if (driverImageName == null || driverImageName.equals("") || driverImageName.equals("NONE")) {
            ((SelectableRoundedImageView) findViewById(R.id.driverImgView)).setImageResource(R.mipmap.ic_no_pic_user);
        } else {
            new LoadImage.builder(LoadImage.bind(driverImageName), ((SelectableRoundedImageView) findViewById(R.id.driverImgView)))
                    .setErrorImagePath(R.mipmap.ic_no_pic_user).setPlaceholderImagePath(R.mipmap.ic_no_pic_user).build();
        }
    }


    public void onClick(View view) {
        int i = view.getId();

        if (i == backImgView.getId()) {

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            generalFunc.removeValue(Utils.iMemberId_KEY);
            MyApp.getInstance().restartWithGetDataApp();
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
