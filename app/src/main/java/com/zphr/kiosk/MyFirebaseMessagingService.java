package com.zphr.kiosk;

import com.general.files.FireTripStatusMsg;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.utils.Logger;
import com.utils.Utils;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String TAG1 = "MyFirebaseIIDService";


    String authorizedEntity; // Project id from Google Developer Console
    String scope = "GCM"; // e.g. communicating using GCM, but you can use any
    // URL-safe characters up to a maximum of 1000, or
    // you can also leave it blank.

    @Override
    public void onNewToken(String s) {

        if (!Utils.checkText(authorizedEntity)) {
            authorizedEntity = new GeneralFunctions(this).retrieveValue(Utils.APP_GCM_SENDER_ID_KEY);
        }
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {

            String refreshedToken = "";

            if (task.isSuccessful()) {
                refreshedToken = task.getResult();
            }

            Logger.d(TAG, "::" + refreshedToken);
            sendRegistrationToServer(refreshedToken);

        });
        super.onNewToken(s);
    }


    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (!Utils.checkText(authorizedEntity)) {
            authorizedEntity = new GeneralFunctions(this).retrieveValue(Utils.APP_GCM_SENDER_ID_KEY);
        }

        if (remoteMessage == null || remoteMessage.getData() == null/* || remoteMessage.getNotification().getBody() == null*/)
            return;


        String message = remoteMessage.getData().get("message");

        new FireTripStatusMsg(MyApp.getInstance() != null ? MyApp.getInstance().getCurrentAct() : getApplicationContext()).fireTripMsg(message);

    }

}
