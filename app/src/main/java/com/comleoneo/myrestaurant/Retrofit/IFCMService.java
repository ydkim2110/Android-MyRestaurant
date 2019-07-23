package com.comleoneo.myrestaurant.Retrofit;

import android.database.Observable;

import com.comleoneo.myrestaurant.Model.FCMResponse;
import com.comleoneo.myrestaurant.Model.FCMSendData;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA8nwh-i0:APA91bGslxk0j7U8Ta2-oRKyXHbvoihYXhC8KsNT0v1eofWG-JJmNFNlxtS7o_TTMtgXy3ojVjhaMrQPx9lR-HslS1xCbSGPJAE0p_nKbPH4f9wOFw9zjldG4z2mb_KtshSczZPw5zwPkvSGAacyQWu70t3iAWqIew"
    })

    @POST("fcm/send")
    Observable<FCMResponse> sendNotificiaton(@Body FCMSendData body);

}
