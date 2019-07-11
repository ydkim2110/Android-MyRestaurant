package com.comleoneo.myrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comleoneo.myrestaurant.Adapter.MyRestaurantAdapter;
import com.comleoneo.myrestaurant.Adapter.RestaurantSliderAdapter;
import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Model.EventBust.RestaurantLoadEvent;
import com.comleoneo.myrestaurant.Model.Restaurant;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.comleoneo.myrestaurant.Services.PicassoImageLoadingService;
import com.facebook.accountkit.AccountKit;
import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ss.com.bannerslider.Slider;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private TextView txt_user_name;
    private TextView txt_user_phone;

    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_restaurant)
    RecyclerView recycler_restaurant;

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private android.app.AlertDialog mDialog;

    private LayoutAnimationController mLayoutAnimationController;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: started!!");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        txt_user_name = headerView.findViewById(R.id.txt_user_name);
        txt_user_phone = headerView.findViewById(R.id.txt_user_phone);

        txt_user_name.setText(Common.currentUser.getName());
        txt_user_phone.setText(Common.currentUser.getUserPhone());
        
        init();
        initView();
        
        loadRestaurant();
    }

    private void loadRestaurant() {
        Log.d(TAG, "loadRestaurant: called!!");
        mDialog.show();
        mCompositeDisposable.add(mIMyRestaurantAPI.getRestaurant(Common.API_KEY)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(restaurantModel -> {

            // Here, we ill user EventBus to send local event set adapter and slider
            EventBus.getDefault().post(new RestaurantLoadEvent(true, restaurantModel.getResult()));
            mDialog.dismiss();

        }, throwable -> {
            mDialog.dismiss();
            EventBus.getDefault().post(new RestaurantLoadEvent(false, throwable.getMessage()));
            Toast.makeText(this, "[GET RESTAURANT]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_restaurant.setLayoutManager(layoutManager);
        recycler_restaurant.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        mLayoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IMyRestaurantAPI.class);

        Slider.init(new PicassoImageLoadingService());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_log_out) {
            signOut();
        } else if (id == R.id.nav_nearby) {
            startActivity(new Intent(HomeActivity.this, NearbyRestaurantActivity.class));
        } else if (id == R.id.nav_order_history) {
            startActivity(new Intent(HomeActivity.this, ViewOrderActivity.class));
        } else if (id == R.id.nav_update_info) {
            startActivity(new Intent(HomeActivity.this, UpdateInfoActivity.class));
        } else if (id == R.id.nav_fav) {
            startActivity(new Intent(HomeActivity.this, FavoriteActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        Log.d(TAG, "signOut: called!!");
        // Here we will made alert dialog to confirm
        AlertDialog confirmDialog = new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Do you really want to sign out?")
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("OK", (dialog, which) -> {
                    Common.currentUser = null;
                    Common.currentRestaurant = null;

                    AccountKit.logOut();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    // Clear all previous activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }).create();

        confirmDialog.show();
    }

    /**
     * REGISTER EVENT BUS
     */
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // Listen EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void processRestaurantLoadEvent(RestaurantLoadEvent event) {
        Log.d(TAG, "processRestaurantLoadEvent: called!!");
        if (event.isSuccess()) {
            displayBanner(event.getRestaurantList());
            displayRestaurant(event.getRestaurantList());
        }
        else {
            Toast.makeText(this, "[RESTAURANT LOAD]"+event.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mDialog.dismiss();
    }

    private void displayRestaurant(List<Restaurant> restaurantList) {
        Log.d(TAG, "displayRestaurant: called!!");
        MyRestaurantAdapter adapter = new MyRestaurantAdapter(this, restaurantList);
        recycler_restaurant.setAdapter(adapter);
        recycler_restaurant.setLayoutAnimation(mLayoutAnimationController);
    }

    private void displayBanner(List<Restaurant> restaurantList) {
        Log.d(TAG, "displayBanner: called!!");
        Log.d(TAG, "displayBanner: size: "+restaurantList.size());
        banner_slider.setAdapter(new RestaurantSliderAdapter(restaurantList));
    }
}
