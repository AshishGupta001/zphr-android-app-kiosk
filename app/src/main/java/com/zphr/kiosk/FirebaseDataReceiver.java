package com.zphr.kiosk;

import android.content.Context;
import android.content.Intent;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.general.files.GeneralFunctions;

/**
 * Created by Admin on 09-08-2017.
 */

public class FirebaseDataReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        GeneralFunctions generalFunctions = new GeneralFunctions(context);

        generalFunctions.storeData("isnotification", true + "");
    }
}
