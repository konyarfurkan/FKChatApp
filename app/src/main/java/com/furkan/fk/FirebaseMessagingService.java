package com.furkan.fk;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by furkan on 28.05.2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title=remoteMessage.getNotification().getTitle();
        String notification_message=remoteMessage.getNotification().getBody();


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int notificationId=(int)System.currentTimeMillis();

        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId,mBuilder.build());

    }
}
