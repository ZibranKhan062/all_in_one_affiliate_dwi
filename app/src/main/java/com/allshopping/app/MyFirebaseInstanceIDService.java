package com.allshopping.app;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseInstanceIDService  extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";
    private static final String SUBSCRIBE_TO = "Appusers";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle incoming messages here
        Log.d(TAG, "Message received: " + remoteMessage.getData());
    }

    @Override
    public void onNewToken(String token) {
        // Handle token refresh here
        Log.d(TAG, "Refreshed token: " + token);

        // Once the token is generated, subscribe to topic with the userId
        FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TO);
    }
}