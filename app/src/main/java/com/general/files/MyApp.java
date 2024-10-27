package com.general.files;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.multidex.MultiDex;

import com.service.handler.ApiHandler;
import com.service.handler.AppService;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.zphr.kiosk.BuildConfig;
import com.zphr.kiosk.KioskBookNowActivity;
import com.zphr.kiosk.KioskLandingScreenActivity;
import com.zphr.kiosk.LauncherActivity;
import com.zphr.kiosk.MaintenanceActivity;
import com.zphr.kiosk.NetworkChangeReceiver;
import com.zphr.kiosk.R;

import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by Admin on 28-06-2016.
 */
public class MyApp extends Application {
    public static boolean ISLOCKED;
    private static MyApp mMyApp;

    GeneralFunctions generalFun;
    boolean isAppInBackground = true;

    private GpsReceiver mGpsReceiver;
    private ActRegisterReceiver actRegisterReceiver;
    private NetworkChangeReceiver mNetWorkReceiver = null;

    private Activity currentAct = null;
    public KioskBookNowActivity kioskBookNowActivity = null;
    private GenerateAlertBox generateSessionAlert;
    private long notification_permission_launch_time = -1;

    public static synchronized MyApp getInstance() {
        return mMyApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.SERVER_CONNECTION_URL = CommonUtilities.SERVER_URL;

        HashMap<String, String> storeData = new HashMap<>();
        storeData.put("SERVERURL", CommonUtilities.SERVER_URL);
        storeData.put("SERVERWEBSERVICEPATH", CommonUtilities.SERVER_WEBSERVICE_PATH);
        storeData.put("USERTYPE", BuildConfig.USER_TYPE);
        GeneralFunctions.storeData(storeData, this);

        try {
            Picasso.Builder builder = new Picasso.Builder(this);


            builder.downloader(new com.squareup.picasso.OkHttp3Downloader(getTrustedCertOkHttpClient()));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(false);
            built.setLoggingEnabled(false);
            Picasso.setSingletonInstance(built);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utils.IS_APP_IN_DEBUG_MODE = BuildConfig.DEBUG ? "Yes" : "No";
        Utils.userType = BuildConfig.USER_TYPE;
        Utils.app_type = BuildConfig.USER_TYPE;
        Utils.USER_ID_KEY = BuildConfig.USER_ID_KEY;
        Utils.IS_KIOSK_APP = BuildConfig.IS_KIOSK_APP;

        setScreenOrientation();
        mMyApp = (MyApp) this.getApplicationContext();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        generalFun = new GeneralFunctions(this);

        if (mGpsReceiver == null) {
            registerReceiver();
        }
        if (actRegisterReceiver == null) {
            registerActReceiver();
        }
//        Fabric.with(this, new Crashlytics());
    }

    public boolean isMyAppInBackGround() {
        return this.isAppInBackground;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        removePubSub();

        if (generalFun.containsKey(Utils.iServiceId_KEY)) {
            generalFun.removeValue(Utils.iServiceId_KEY);
        }

        if (generalFun.containsKey(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY)) {
            generalFun.removeValue(Utils.KIOSK_DESTINATION_LIST_JSON_DETAILS_KEY);
        }
    }

    public void removePubSub() {
        releaseGpsReceiver();
        releaseactReceiver();
        removeAllRunningInstances();
        AppService.destroy();
    }


    private void releaseGpsReceiver() {
        if (mGpsReceiver != null)
            this.unregisterReceiver(mGpsReceiver);
        this.mGpsReceiver = null;
    }


    private void registerReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        this.mGpsReceiver = new GpsReceiver();
        mIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                this.registerReceiver(this.mGpsReceiver, mIntentFilter, Context.RECEIVER_EXPORTED);
            } else {
                this.registerReceiver(this.mGpsReceiver, mIntentFilter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseactReceiver() {

        if (actRegisterReceiver != null)
            this.unregisterReceiver(actRegisterReceiver);
        this.actRegisterReceiver = null;
    }


    private void registerActReceiver() {
        if (actRegisterReceiver == null) {
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(String.format("%s%s%s%s%s", "Act", "ivi", "tyR", "egis", "ter"));
            this.actRegisterReceiver = new ActRegisterReceiver();
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    this.registerReceiver(this.actRegisterReceiver, mIntentFilter, Context.RECEIVER_EXPORTED);
                } else {
                    this.registerReceiver(this.actRegisterReceiver, mIntentFilter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeAllRunningInstances() {
        connectReceiver(false);
    }

    public GeneralFunctions getAppLevelGeneralFunc() {
        if (generalFun == null) {
            generalFun = new GeneralFunctions(this);
        }
        return generalFun;
    }

    private void registerNetWorkReceiver() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO && mNetWorkReceiver == null) {
            try {
                IntentFilter mIntentFilter = new IntentFilter();
                mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
                /*Extra Filter Started */
                mIntentFilter.addAction(ConnectivityManager.EXTRA_IS_FAILOVER);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_REASON);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_EXTRA_INFO);
                /*Extra Filter Ended */
//                mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//                mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");

                this.mNetWorkReceiver = new NetworkChangeReceiver();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    this.registerReceiver(this.mNetWorkReceiver, mIntentFilter, Context.RECEIVER_EXPORTED);
                } else {
                    this.registerReceiver(this.mNetWorkReceiver, mIntentFilter);
                }
            } catch (Exception e) {
            }
        }
    }

    private void unregisterNetWorkReceiver() {

        if (mNetWorkReceiver != null)
            try {
                this.unregisterReceiver(mNetWorkReceiver);
                this.mNetWorkReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

    }


    public void setScreenOrientation() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Utils.runGC();
                // new activity created; force its orientation to portrait
                activity.setRequestedOrientation(Utils.IS_KIOSK_APP ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                activity.setTitle(getResources().getString(R.string.app_name));
                setCurrentAct(activity);
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                if (activity instanceof KioskBookNowActivity) {
                    //Reset PubNub instance
                    configureAppServices();
                }
                if (Utils.IS_KIOSK_APP ? Utils.checkText(generalFun.getHotelId()) : (generalFun.isUserLoggedIn() && Utils.checkText(generalFun.getMemberId()))) {
                    if (!(activity instanceof LauncherActivity) && !(activity instanceof MaintenanceActivity) && activity.isTaskRoot()) {
                        openNotificationPermission();
                    }
                }

//                if (activity instanceof RatingActivity) {
//                    terminatePuSubInstance();
//                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Utils.runGC();
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Utils.runGC();
                setCurrentAct(activity);
                isAppInBackground = false;

                LocalNotification.clearAllNotifications();

                if (currentAct instanceof KioskBookNowActivity) {
                    ViewGroup viewGroup = (ViewGroup) currentAct.findViewById(android.R.id.content);
                    new Handler().postDelayed(() -> {
                        OpenNoLocationView.getInstance(currentAct, viewGroup).configView(false);
                    }, 1000);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Utils.runGC();
                isAppInBackground = true;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Utils.runGC();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                /*Called to retrieve per-instance state from an activity before being killed so that the state can be restored in onCreate(Bundle) or onRestoreInstanceState(Bundle) (the Bundle populated by this method will be passed to both).*/
                removeAllRunningInstances();
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Utils.runGC();
                Utils.hideKeyboard(activity);


                if (activity instanceof KioskBookNowActivity && kioskBookNowActivity == activity) {
                    kioskBookNowActivity = null;
                }


                if (activity instanceof KioskBookNowActivity && kioskBookNowActivity == activity) {

                    AppService.destroy();

                }
            }
        });
    }

    private void connectReceiver(boolean isConnect) {
        if (isConnect && mNetWorkReceiver == null) {
            registerNetWorkReceiver();
        } else if (!isConnect && mNetWorkReceiver != null) {
            unregisterNetWorkReceiver();
        }
    }


    public Activity getCurrentAct() {
        return currentAct;
    }

    private void setCurrentAct(Activity currentAct) {
        this.currentAct = currentAct;
        RegisterActivity();

        if (currentAct instanceof LauncherActivity) {
            kioskBookNowActivity = null;
        }

        if (currentAct instanceof KioskBookNowActivity) {
            kioskBookNowActivity = (KioskBookNowActivity) currentAct;
        }

        connectReceiver(true);
    }

    private void RegisterActivity() {
        sendBroadcast(new Intent(String.format("%s%s%s%s%s", "Act", "ivi", "tyR", "egis", "ter")));
    }

    private void configureAppServices() {
        AppService.getInstance().resetAppServices();
    }


    public void restartWithGetDataApp() {
        GetUserData objRefresh = new GetUserData(generalFun, MyApp.getInstance().getCurrentAct());
        objRefresh.getData();
    }

    public void refreshData() {
        GetUserData objRefresh = new GetUserData(generalFun, MyApp.getInstance().getCurrentAct());
        objRefresh.getLatestDataOnly();
    }

    public void restartWithGetDataApp(String tripId) {
        GetUserData objRefresh = new GetUserData(generalFun, MyApp.getInstance().getCurrentAct(), tripId);
        objRefresh.getData();
    }

    public void notifySessionTimeOut() {
        if (generateSessionAlert != null) {
            return;
        }


        generateSessionAlert = new GenerateAlertBox(MyApp.getInstance().getCurrentAct());


        generateSessionAlert.setContentMessage(generalFun.retrieveLangLBl("", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"),
                generalFun.retrieveLangLBl("Your session is expired. Please login again.", "LBL_SESSION_TIME_OUT"));
        generateSessionAlert.setPositiveBtn(generalFun.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateSessionAlert.setCancelable(false);
        generateSessionAlert.setBtnClickList(btn_id -> {

            if (btn_id == 1) {
                onTerminate();
                generalFun.logOutUser(MyApp.this);
                try {
                    generateSessionAlert = null;
                } catch (Exception e) {

                }
                (MyApp.getInstance().getGeneralFun(getCurrentAct())).restartApp();
            }
        });

        generateSessionAlert.showSessionOutAlertBox();
    }

    public GeneralFunctions getGeneralFun(Context mContext) {
        return new GeneralFunctions(mContext, R.id.backImgView);
    }

    public void logOutFromDevice(boolean isForceLogout, String password) {

        if (generalFun != null) {
            if (password.isEmpty() && Utils.IS_KIOSK_APP) {
                forceLogout();
            } else {
                final HashMap<String, String> parameters = new HashMap<String, String>();

                parameters.put("type", "callOnLogout");
                parameters.put("iMemberId", generalFun.getHotelId());
                parameters.put("UserType", Utils.userType);
                parameters.put("vPassword", password);

                ApiHandler.execute(getCurrentAct(), parameters, true, false, generalFun, responseString -> {

                    if (responseString != null && !responseString.equals("")) {

                        boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                        if (isDataAvail == true) {
                            forceLogout();
                        } else {
                            if (isForceLogout) {
                                /*generalFun.showGeneralMessage("",
                                        generalFun.retrieveLangLBl("", generalFun.getJsonValue(Utils.message_str, responseString)), buttonId -> (MyApp.getInstance().getGeneralFun(getCurrentAct())).restartApp());*/
                                showAlertDialog(responseString);
                            } else {
                                showAlertDialog(responseString);
                                generalFun.showGeneralMessage("",
                                        generalFun.retrieveLangLBl("", generalFun.getJsonValue(Utils.message_str, responseString)));
                            }
                        }
                    } else {
                        if (isForceLogout) {
                            showAlertDialog(responseString);
                            generalFun.showError(buttonId -> (MyApp.getInstance().getGeneralFun(getCurrentAct())).restartApp());
                        } else {
                            showAlertDialog(responseString);
                            generalFun.showError();
                        }
                    }
                });

            }
        }
    }

    private void showAlertDialog(String responseString) {
        new AlertDialog.Builder(getCurrentAct())
                .setTitle("Warning")
                .setMessage(generalFun.retrieveLangLBl("", generalFun.getJsonValue(Utils.message_str, responseString)))
                .setPositiveButton(generalFun.retrieveLangLBl("OK", "LBL_BTN_OK_TXT"),
                        (dialog, whichButton) -> dialog.dismiss()
                ).create().show();
    }

    public void forceLogout() {
        if (getCurrentAct() instanceof KioskBookNowActivity) {
            ((KioskBookNowActivity) getCurrentAct()).releaseScheduleNotificationTask();
        }

        if (getCurrentAct() instanceof KioskLandingScreenActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((KioskLandingScreenActivity) getCurrentAct()).removeScreenPin();
            }
        }

        endProcess();
    }

    public void endProcess() {
        onTerminate();
        generalFun.logOutUser(MyApp.this);

        (new GeneralFunctions(getCurrentAct())).restartApp();
    }

    public String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public String getVersionCode() {
        return BuildConfig.VERSION_CODE + "";
    }

    private static OkHttpClient getTrustedCertOkHttpClient() {
        try {
            OkHttpClient client = new OkHttpClient();
            client.hostnameVerifier();
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            client.sslSocketFactory();
            return client;
        } catch (Exception e) {
            return null;
        }
    }

    public void openNotificationPermission() {
        if (getCurrentAct() == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S || NotificationManagerCompat.from(getCurrentAct()).areNotificationsEnabled()) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            openNotificationPermissionDialogView();
            return;
        }

        ActivityResultLauncher<String> notificationActivityResult = ((ComponentActivity) getCurrentAct()).registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if ((System.currentTimeMillis() - notification_permission_launch_time < 1500) && !isGranted) {
                        openNotificationPermissionDialogView();
                    }
                }
        );
        notification_permission_launch_time = System.currentTimeMillis();
        notificationActivityResult.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    @SuppressLint("InflateParams")
    private void openNotificationPermissionDialogView() {

        GenerateAlertBox alert = new GenerateAlertBox(getCurrentAct());
        alert.setCustomView(R.layout.notification_permission_layout);

        MTextView titleTxt = (MTextView) alert.getView(R.id.titleTxt);
        MTextView btnAccept = (MTextView) alert.getView(R.id.btnAccept);
        MTextView btnReject = (MTextView) alert.getView(R.id.btnReject);

        String sourceString = generalFun.retrieveLangLBl("", "LBL_ALLOW_RUNTIME_NOTI_TXT").replace("#PROJECT_NAME#", "<b>" + getString(R.string.app_name) + "</b> ");
        titleTxt.setText(Html.fromHtml(sourceString));

        btnAccept.setText(generalFun.retrieveLangLBl("", "LBL_ALLOW"));
        btnReject.setText(generalFun.retrieveLangLBl("", "LBL_DONT_ALLOW_TXT"));

        btnAccept.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getCurrentAct().getPackageName());
                getCurrentAct().startActivity(intent);
                btnReject.performClick();
            }
        });
        btnReject.setOnClickListener(v -> alert.closeAlertBox());

        alert.showAlertBox();
        alert.alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}