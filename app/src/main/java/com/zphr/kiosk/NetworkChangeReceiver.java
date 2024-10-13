package com.zphr.kiosk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import com.general.files.MyApp;
import com.general.files.OpenNoLocationView;

/**
 * Created by Admin on 31-08-2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        checkNetworkSettings();

    }

    private void checkNetworkSettings() {
        Activity currentActivity = MyApp.getInstance().getCurrentAct();

        if (currentActivity != null) {

            if (currentActivity instanceof KioskLandingScreenActivity || currentActivity instanceof KioskBookNowActivity|| currentActivity instanceof KioskCabSelectionActivity) {

                ViewGroup viewGroup = (ViewGroup) currentActivity.findViewById(android.R.id.content);
                handleNetworkView(currentActivity, viewGroup);
            }
        }
    }

    private void handleNetworkView(Activity activity, ViewGroup viewGroup) {
        try {
            OpenNoLocationView.getInstance(activity, viewGroup).configView(true);
        } catch (Exception e) {

        }
    }
}
