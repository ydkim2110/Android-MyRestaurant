package com.comleoneo.myrestaurant;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comleoneo.myrestaurant.Adapter.MyFoodAdapter;
import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Model.Category;
import com.comleoneo.myrestaurant.Model.EventBust.FoodListEvent;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FoodListActivity extends AppCompatActivity {

    private static final String TAG = FoodListActivity.class.getSimpleName();

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private android.app.AlertDialog mDialog;

    @BindView(R.id.img_category)
    KenBurnsView img_category;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private MyFoodAdapter adapter;
    private MyFoodAdapter searchAdapter;
    private Category selectedCategory;

    private LayoutAnimationController mLayoutAnimationController;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        if (adapter != null) {
            adapter.onStop();
        }
        if (searchAdapter != null) {
            searchAdapter.onStop();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        Log.d(TAG, "onCreate: started!!");

        init();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem menuItem = menu.findItem(R.id.search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchFood(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Restore to original adapter when use close Search
                recycler_food_list.setAdapter(adapter);
                return true;
            }
        });

        return true;
    }

    private void startSearchFood(String query) {
        Log.d(TAG, "startSearchFood: called!!");
        mDialog.show();
        mCompositeDisposable.add(mIMyRestaurantAPI.searchFood(Common.API_KEY, query, selectedCategory.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(foodModel -> {

                    if (foodModel.isSuccess()) {
                        searchAdapter = new MyFoodAdapter(FoodListActivity.this, foodModel.getResult());
                        recycler_food_list.setAdapter(searchAdapter);
                        recycler_food_list.setLayoutAnimation(mLayoutAnimationController);
                    } else {
                        if (foodModel.getMessage().contains("Empty")) {
                            recycler_food_list.setAdapter(null);
                            Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    mDialog.dismiss();

                }, throwable -> {
                    Toast.makeText(FoodListActivity.this, "[SEARCH FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_food_list.setLayoutManager(layoutManager);
        recycler_food_list.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IMyRestaurantAPI.class);
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
    public void loadFoodListByCategory(FoodListEvent event) {
        Log.d(TAG, "loadFoodListByCategory: called!!");
        if (event.isSuccess()) {

            selectedCategory = event.getCategory();

            Picasso.get().load(event.getCategory().getImage()).into(img_category);
            toolbar.setTitle(event.getCategory().getName());

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            mDialog.show();

            mCompositeDisposable.add(mIMyRestaurantAPI.getFoodOfMenu(Common.API_KEY, event.getCategory().getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(foodModel -> {

                        if (foodModel.isSuccess()) {
                            adapter = new MyFoodAdapter(this, foodModel.getResult());
                            recycler_food_list.setAdapter(adapter);
                            recycler_food_list.setLayoutAnimation(mLayoutAnimationController);
                        } else {
                            Toast.makeText(this, "[GET FOOD RESULT]" + foodModel.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        mDialog.dismiss();

                    }, throwable -> {
                        mDialog.dismiss();
                        Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
        } else {

        }
    }
}
