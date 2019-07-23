package com.comleoneo.myrestaurant.Services;

import android.widget.Toast;

import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IMyRestaurantAPI.class);
        mCompositeDisposable = new CompositeDisposable();

        Paper.init(this);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onNewToken(String newToken) {
        super.onNewToken(newToken);
        // Here we will update token
        // To update token, we need FBID
        // But this is services, so Common.currentUser will null
        // So, we need save signed FBID by Paper and get it back when we need

        String fbid = Paper.book().read(Common.REMEMBER_FBID);
        String apiKey = Paper.book().read(Common.API_KEY_TAG);
        mCompositeDisposable.add(mIMyRestaurantAPI.updateTokenToServer(apiKey, fbid, newToken)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tokenModel -> {

            // Do nothing

        }, throwable -> {
            Toast.makeText(this, "[REFRESH TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Get notification object from FCM
        // Because we want to retrieve notification while app killed, so we must use Data payload
        Map<String, String> dataRecv = remoteMessage.getData();
        if (dataRecv != null) {
            Common.showNotification(this,
                    new Random().nextInt(),
                    dataRecv.get(Common.NOTIFIC_TITLE),
                    dataRecv.get(Common.NOTIFIC_CONTENT),
                    null);
        }
    }
}
