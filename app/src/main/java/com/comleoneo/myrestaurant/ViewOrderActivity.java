package com.comleoneo.myrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.comleoneo.myrestaurant.Adapter.MyOrderAdapter;
import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Interface.ILoadMore;
import com.comleoneo.myrestaurant.Model.Order;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrderActivity extends AppCompatActivity implements ILoadMore {

    private static final String TAG = ViewOrderActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_order)
    RecyclerView recycler_view_order;

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    private MyOrderAdapter mAdapter;
    private List<Order> mOrderList;
    private int maxData = 0;

    private LayoutAnimationController mLayoutAnimationController;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);
        Log.d(TAG, "onCreate: started!!");

        init();
        initView();

        //getAllOrder();
        getMaxOrder();
    }

    private void getMaxOrder() {
        Log.d(TAG, "getMaxOrder: called!!");
        mDialog.show();

        mCompositeDisposable.add(mIMyRestaurantAPI.getMaxOrder(Common.API_KEY,
                Common.currentUser.getFbid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(maxOrderModel -> {

                    if (maxOrderModel.isSuccess()) {
                        maxData = maxOrderModel.getResult().get(0).getMaxRowNum();
                        Log.d(TAG, "getMaxOrder: maxData: "+maxData);
                        mDialog.dismiss();

                        getAllOrder(0, 10);
                    }

                }, throwable -> {
                    mDialog.dismiss();
                    Toast.makeText(this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void getAllOrder(int from, int to) {
        Log.d(TAG, "getAllOrder: called!!");
        mDialog.show();

        mCompositeDisposable.add(mIMyRestaurantAPI.getOrder(Common.API_KEY,
                Common.currentUser.getFbid(), from, to)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(orderModel -> {

            if (orderModel.isSuccess()) {
                if (orderModel.getResult().size() > 0) {
                    if (mAdapter == null) {
                        mOrderList = new ArrayList<>();
                        mOrderList = (orderModel.getResult());
                        mAdapter = new MyOrderAdapter(this, mOrderList, recycler_view_order);
                        mAdapter.setILoadMore(this);
                        recycler_view_order.setAdapter(mAdapter);
                        recycler_view_order.setLayoutAnimation(mLayoutAnimationController);
                    }
                    else {
                        // Here we will remove null item after load done
                        // IF you don't remove it, loading view still available
                        mOrderList.remove(mOrderList.size()-1);
                        mOrderList = orderModel.getResult();
                        mAdapter.addItem(mOrderList);
                    }
                }
                mDialog.dismiss();
            }

        }, throwable -> {
            mDialog.dismiss();
            Toast.makeText(this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        mLayoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_view_order.setLayoutManager(layoutManager);
        recycler_view_order.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        toolbar.setTitle(getString(R.string.your_order));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

    @Override
    public void onLoadMore() {
        // When loadmore being call
        // First, we will check data count with max data
        if (mAdapter.getItemCount() < maxData) {
            // Add null object to List to tell adapter known show loading state
            mOrderList.add(null);
            mAdapter.notifyItemInserted(mOrderList.size()-1);

            getAllOrder(mAdapter.getItemCount()+1, mAdapter.getItemCount()+10);

            mAdapter.notifyDataSetChanged();
            mAdapter.setLoaded();
        }
        else {
            Toast.makeText(this, "Max Data to load", Toast.LENGTH_SHORT).show();
        }
    }
}
