package com.capacitorjs.plugins.pushnotifications;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MessagingService extends FirebaseMessagingService {

    static final String defaultSmallIconName = "ic_stat_name";
    static final String defaultLargeIconName = "ic_stat_name";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        PushNotificationsPlugin.sendRemoteMessage(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        PushNotificationsPlugin.onNewToken(s);
    }

    @Override
    public void handleIntent(Intent intent) {
        RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
        Map<String, String> data = remoteMessage.getData();
        if(data.containsKey("redirect_url")) {
            Bundle bundle = new Bundle();
            for (String key : data.keySet()) {
                bundle.putString(key, data.get(key));
            }
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            String id = null;
            String title = null;
            String body = null;
            String sound = null;
            String color = null;
            String icon = null;
            // TODO(developer): Handle FCM messages here.
            // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
            String messageType;
            String vibrate = null;
            String light = null;
            String channelId = "channel_news";
            String visibility = null;
            String priority = null;
            if(data.containsKey("notification_title")) title = data.get("notification_title");
            if(data.containsKey("notification_body")) body = data.get("notification_body");
            if(data.containsKey("notification_android_channel_id")) channelId = data.get("notification_android_channel_id");
            if(data.containsKey("notification_android_id")) id = data.get("notification_android_id");
            if(data.containsKey("notification_android_sound")) sound = data.get("notification_android_sound");
            if(data.containsKey("notification_android_vibrate")) vibrate = data.get("notification_android_vibrate");
            if(data.containsKey("notification_android_light")) light = data.get("notification_android_light"); //String containing hex ARGB color, miliseconds on, miliseconds off, example: '#FFFF00FF,1000,3000'
            if(data.containsKey("notification_android_color")) color = data.get("notification_android_color");
            if(data.containsKey("notification_android_icon")) icon = data.get("notification_android_icon");
            if(data.containsKey("notification_android_visibility")) visibility = data.get("notification_android_visibility");
            if(data.containsKey("notification_android_priority")) priority = data.get("notification_android_priority");
            bundle.putString("messageType", "notification");
            this.putKVInBundle("id", id, bundle);
            this.putKVInBundle("title", title, bundle);
            this.putKVInBundle("body", body, bundle);
            this.putKVInBundle("sound", sound, bundle);
            this.putKVInBundle("vibrate", vibrate, bundle);
            this.putKVInBundle("light", light, bundle);
            this.putKVInBundle("color", color, bundle);
            this.putKVInBundle("icon", icon, bundle);
            this.putKVInBundle("channel_id", channelId, bundle);
            this.putKVInBundle("priority", priority, bundle);
            this.putKVInBundle("visibility", visibility, bundle);
            this.putKVInBundle("show_notification", "true", bundle);
            this.putKVInBundle("from", remoteMessage.getFrom(), bundle);
            this.putKVInBundle("collapse_key", remoteMessage.getCollapseKey(), bundle);
            this.putKVInBundle("sent_time", String.valueOf(remoteMessage.getSentTime()), bundle);
            this.putKVInBundle("ttl", String.valueOf(remoteMessage.getTtl()), bundle);
            PendingIntent pendingIntent = this.getPendingIntent(data.get("redirect_url"));
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
            notificationBuilder
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            // Build notification


            // Icon
            int defaultSmallIconResID = getResources().getIdentifier(defaultSmallIconName, "drawable", getPackageName());
            int customSmallIconResID = 0;
            if(icon != null){
                customSmallIconResID = getResources().getIdentifier(icon, "drawable", getPackageName());
            }

            if (customSmallIconResID != 0) {
                notificationBuilder.setSmallIcon(customSmallIconResID);
                Log.d(TAG, "Small icon: custom="+icon);
            }else if (defaultSmallIconResID != 0) {
                Log.d(TAG, "Small icon: default="+defaultSmallIconName);
                notificationBuilder.setSmallIcon(defaultSmallIconResID);
            } else {
                Log.d(TAG, "Small icon: application");
                notificationBuilder.setSmallIcon(getApplicationInfo().icon);
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int defaultLargeIconResID = getResources().getIdentifier(defaultLargeIconName, "drawable", getPackageName());
                int customLargeIconResID = 0;
                if(icon != null){
                    customLargeIconResID = getResources().getIdentifier(icon+"_large", "drawable", getPackageName());
                }

                int largeIconResID;
                if (customLargeIconResID != 0 || defaultLargeIconResID != 0) {
                    if (customLargeIconResID != 0) {
                        largeIconResID = customLargeIconResID;
                        Log.d(TAG, "Large icon: custom="+icon);
                    }else{
                        Log.d(TAG, "Large icon: default="+defaultLargeIconName);
                        largeIconResID = defaultLargeIconResID;
                    }
                    notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), largeIconResID));
                }
            }

            if (TextUtils.isEmpty(id)) {
                Random rand = new Random();
                int n = rand.nextInt(50) + 1;
                id = Integer.toString(n);
            }


            // Display notification
            Notification notif = notificationBuilder.build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Log.d(TAG, "show notification: "+notif.toString());
            notificationManager.notify(id.hashCode(), notif);
        } else {
            super.handleIntent(intent);
        }
    }


    private PendingIntent getPendingIntent(String redirectUrl) {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse(redirectUrl));

        return PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

    }

    private void putKVInBundle(String k, String v, Bundle b){
        if(v != null && !b.containsKey(k)){
            b.putString(k, v);
        }
    }
}
