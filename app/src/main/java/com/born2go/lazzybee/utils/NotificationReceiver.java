package com.born2go.lazzybee.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.born2go.lazzybee.R;
import com.born2go.lazzybee.activity.SplashScreen;
import com.born2go.lazzybee.gtools.ContainerHolderSingleton;
import com.born2go.lazzybee.shared.LazzyBeeShare;
import com.google.android.gms.tagmanager.Container;

/**
 * Created by Hue on 9/4/2015.
 */
public class NotificationReceiver extends BroadcastReceiver {


    private static final String TAG = "NotificationReceiver";
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String NOTIFICATION_WHEN = "when";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BroadcastReceiver::OnReceive() >>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //get id Notificaion
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);

        //get alertTime Notification
        long alertTime = intent.getLongExtra(NOTIFICATION_WHEN, 0);

        //Define intent and pendingInten MainActivity
        Intent intent1 = new Intent(context, SplashScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String message;
        try {
            Container container = ContainerHolderSingleton.getContainerHolder().getContainer();
            if (container == null) {
                message = context.getString(R.string.notification_message);
            } else {
                message = container.getString(LazzyBeeShare.NOTIFY_TEXT);
                if (message == null) {
                    message = context.getString(R.string.notification_message);
                }
            }
        } catch (Exception e) {
            message = context.getString(R.string.notification_message);
        }


        //Define notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_logo_green)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setWhen(alertTime)
                        .setSound(alarmSound);

        mBuilder.setContentIntent(pendingIntent);

        //Noti Notification
        notificationManager.notify(id, mBuilder.build());

    }

}