package com.comleoneo.myrestaurant.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.comleoneo.myrestaurant.Model.Addon;
import com.comleoneo.myrestaurant.Model.FavoriteOnlyId;
import com.comleoneo.myrestaurant.Model.Restaurant;
import com.comleoneo.myrestaurant.Model.User;
import com.comleoneo.myrestaurant.R;
import com.comleoneo.myrestaurant.Retrofit.IFCMService;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.comleoneo.myrestaurant.Services.MyFirebaseMessagingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {

    public static final String API_RESTAURANT_ENDPOINT = "http://192.168.0.13:3000/";

    // later, secure it with Firebase Remote Config
    public static final String API_KEY = "1234";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String REMEMBER_FBID = "REMEMBER_FBID";
    public static final String API_KEY_TAG = "API_KEY";
    public static final String NOTIFIC_TITLE = "title";
    public static final String NOTIFIC_CONTENT = "content";

    public static User currentUser;
    public static Restaurant currentRestaurant;
    public static Set<Addon> addonList = new HashSet<>();
    public static List<FavoriteOnlyId> currentFavOfRestaurant;

    public static IFCMService getFCMService() {
        return RetrofitClient.getInstance("https://fcm.googleapis.com/").create(IFCMService.class);
    }

    public static boolean checkFavorite(int id) {
        boolean result = false;
        for (FavoriteOnlyId item : currentFavOfRestaurant) {
            if (item.getFoodId() == id) {
                result = true;
            }
        }
        return result;
    }

    public static void removeFavorite(int id) {
        for (FavoriteOnlyId item : currentFavOfRestaurant) {
            if (item.getFoodId() == id) {
                currentFavOfRestaurant.remove(item);
            }
        }
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Cancelled";
        }
    }

    public static void showNotification(Context context, int notiId, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, notiId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String NOTIFICATION_CHANNEL_ID = "ydkim2110_restaurant";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "My restaurant Notification", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("My restaurant Client App");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        Notification mNotification = builder.build();
        notificationManager.notify(notiId, mNotification);


    }
}
