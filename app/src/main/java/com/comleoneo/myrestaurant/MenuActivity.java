package com.comleoneo.myrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.Toast;

import com.comleoneo.myrestaurant.Adapter.MyCategoryAdapter;
import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Database.CartDataSource;
import com.comleoneo.myrestaurant.Database.CartDatabase;
import com.comleoneo.myrestaurant.Database.LocalCartDataSource;
import com.comleoneo.myrestaurant.Model.EventBust.MenuItemEvent;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.comleoneo.myrestaurant.Utils.SpaceItemDecoration;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nex3z.notificationbadge.NotificationBadge;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = MenuActivity.class.getSimpleName();

    @BindView(R.id.img_restaurant)
    KenBurnsView img_restaurant;
    @BindView(R.id.recycler_category)
    RecyclerView recycler_category;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton btn_cart;
    @BindView(R.id.badge)
    NotificationBadge badge;

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private android.app.AlertDialog mDialog;

    private MyCategoryAdapter mAdapter;
    private CartDataSource mCartDataSource;

    private LayoutAnimationController mLayoutAnimationController;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartByRestaurant();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Log.d(TAG, "onCreate: started!!");

        init();
        initView();

        countCartByRestaurant();
        loadFavoriteByRestaurant();

    }

    private void loadFavoriteByRestaurant() {
        Log.d(TAG, "loadFavoriteByRestaurant: called!!");
        mCompositeDisposable.add(mIMyRestaurantAPI.getFavoriteByRestaurant(Common.API_KEY,
                Common.currentUser.getFbid(), Common.currentRestaurant.getId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(favoriteOnlyIdModel -> {

            if (favoriteOnlyIdModel.isSuccess()) {
                if (favoriteOnlyIdModel.getResult() != null && favoriteOnlyIdModel.getResult().size() > 0) {
                    Common.currentFavOfRestaurant = favoriteOnlyIdModel.getResult();
                }
                else {
                    Common.currentFavOfRestaurant = new ArrayList<>();
                }
            }
            else {
                Toast.makeText(this, "[GET FAVORITE]"+favoriteOnlyIdModel.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }, throwable -> {
            Toast.makeText(this, "[GET FAVORITE]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void countCartByRestaurant() {
        Log.d(TAG, "countCartByRestaurant: called!!");
        mCartDataSource.countItemInCart(Common.currentUser.getFbid(),
                Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        badge.setText(String.valueOf(integer));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MenuActivity.this, "[COUNT CART}"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        mLayoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);

        btn_cart.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, CartListActivity.class));
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        // This code will select item view type
        // If item is last, it will set full width on Grid layout
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mAdapter != null) {
                    switch (mAdapter.getItemViewType(position)) {
                        case Common.DEFAULT_COLUMN_COUNT:
                            return 1;
                        case Common.FULL_WIDTH_COLUMN:
                            return 2;
                        default:
                            return -1;
                    }
                } else {
                    return -1;
                }
            }
        });
        recycler_category.setLayoutManager(layoutManager);
        recycler_category.addItemDecoration(new SpaceItemDecoration(8));
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IMyRestaurantAPI.class);

        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
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
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void loadMenuByRestaurant(MenuItemEvent event) {
        if (event.isSuccess()) {
            Picasso.get().load(event.getRestaurant().getImage()).into(img_restaurant);
            toolbar.setTitle(event.getRestaurant().getName());

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Request Category by restaurant Id
            mCompositeDisposable.add(mIMyRestaurantAPI.getCategories(Common.API_KEY, event.getRestaurant().getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(menuModel -> {

                        mAdapter = new MyCategoryAdapter(MenuActivity.this, menuModel.getResult());
                        recycler_category.setAdapter(mAdapter);
                        recycler_category.setLayoutAnimation(mLayoutAnimationController);

                    }, throwable -> {
                        Toast.makeText(this, "[GET CATEGORY]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
        }
    }
}
