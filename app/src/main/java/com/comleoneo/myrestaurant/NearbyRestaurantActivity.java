package com.comleoneo.myrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Model.EventBust.MenuItemEvent;
import com.comleoneo.myrestaurant.Model.Restaurant;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NearbyRestaurantActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = NearbyRestaurantActivity.class.getSimpleName();

    private GoogleMap mMap;

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location currentLocation;

    private Marker userMarker;
    private boolean isFirstLoad = false;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_restaurant);

        init();
        initView();

    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar.setTitle(getString(R.string.nearby_restaurant));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

        buildLocationRequest();
        buildLocationCallBack();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void buildLocationCallBack() {
        Log.d(TAG, "buildLocationCallBack: called!!");
        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();

                addMarkerAndMoveCamera(locationResult.getLastLocation());

                if (!isFirstLoad) {
                    isFirstLoad = !isFirstLoad;
                    requestNearbyRestaurant(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude(), 10);
                }

            }
        };
    }

    private void requestNearbyRestaurant(double latitude, double longitude, int distance) {
        Log.d(TAG, "requestNearbyRestaurant: called!!");

        mDialog.show();
        mCompositeDisposable.add(mIMyRestaurantAPI.getNearbyRestaurant(Common.API_KEY,
                latitude, longitude, distance).
                subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(restaurantModel -> {

                    if (restaurantModel.isSuccess()) {
                        addRestaurantMarker(restaurantModel.getResult());
                    } else {
                        Toast.makeText(this, "" + restaurantModel.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    mDialog.dismiss();

                }, throwable -> {
                    mDialog.dismiss();
                    Toast.makeText(this, "[NEARBY RESTAURANT]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void addRestaurantMarker(List<Restaurant> restaurantList) {
        Log.d(TAG, "addRestaurantMarker: called!!");
        for (Restaurant restaurant : restaurantList) {
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_marker))
                    .position(new LatLng(restaurant.getLat(), restaurant.getLng()))
                    .snippet(restaurant.getAddress())
                    .title(new StringBuilder().append(restaurant.getId())
                            .append(".")
                            .append(restaurant.getName()).toString()));
        }
    }

    private void addMarkerAndMoveCamera(Location lastLocation) {
        Log.d(TAG, "addMarkerAndMoveCamera: called!!");
        if (userMarker != null) {
            userMarker.remove();
        }

        LatLng userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title(Common.currentUser.getName()));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLatLng, 17);
        mMap.animateCamera(yourLocation);
    }

    private void buildLocationRequest() {
        Log.d(TAG, "buildLocationRequest: called!!");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setSmallestDisplacement(10f);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                Log.e(TAG, "onMapReady: Load Style Error");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "onMapReady: Resource Not Found");
        }

        mMap.setOnInfoWindowClickListener(marker -> {
            String id = marker.getTitle().substring(0, marker.getTitle().indexOf("."));
            if (!TextUtils.isEmpty(id)) {
                mCompositeDisposable.add(mIMyRestaurantAPI.getRestaurantById(Common.API_KEY, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(restaurantByIdModel -> {

                    if (restaurantByIdModel.isSuccess()) {
                        Common.currentRestaurant = restaurantByIdModel.getResult().get(0);
                        EventBus.getDefault().postSticky(new MenuItemEvent(true, Common.currentRestaurant));
                        startActivity(new Intent(NearbyRestaurantActivity.this, MenuActivity.class));
                        finish();
                    }
                    else {

                    }

                }, throwable -> {
                    Toast.makeText(this, "GET RESTAURANT BY ID"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
            }
        });
    }
}
