package com.comleoneo.myrestaurant.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Interface.ILoadMore;
import com.comleoneo.myrestaurant.Model.Order;
import com.comleoneo.myrestaurant.R;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private Context mContext;
    private List<Order> mOrderList;
    private SimpleDateFormat mSimpleDateFormat;

    private RecyclerView mRecyclerView;
    private ILoadMore mILoadMore;

    private boolean isLoading = false;
    private int totalItemCount = 0;
    private int lastVisibleItem = 0;
    private int visibleThreshold = 10;

    public MyOrderAdapter(Context context, List<Order> orderList, RecyclerView recyclerView) {
        mContext = context;
        mOrderList = orderList;
        mRecyclerView = recyclerView;
        mSimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        // Init
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= lastVisibleItem+visibleThreshold) {
                    if (mILoadMore != null) {
                        mILoadMore.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void setLoaded() {
        isLoading = false;
    }

    public void addItem(List<Order> addItems) {
        int startInsertedIndex = mOrderList.size();
        mOrderList.addAll(addItems);
        notifyItemInserted(startInsertedIndex);
    }

    public void setILoadMore(ILoadMore ILoadMore) {
        mILoadMore = ILoadMore;
    }

    @Override
    public int getItemViewType(int position) {
        // If we meet 'null' value in List, we will understand this is loading state
        if (mOrderList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        }
        else {
            return VIEW_TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_order, parent, false);
            return new MyViewHolder(view);
        }
        else {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_loading_item, parent, false);
            return new MyLoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
       if (holder instanceof MyViewHolder) {
           MyViewHolder myViewHolder = (MyViewHolder) holder;

           myViewHolder.txt_num_of_item.setText(new StringBuilder("Num of Items: ").append(mOrderList.get(position).getNumOfItem()));
           myViewHolder.txt_order_address.setText(new StringBuilder(mOrderList.get(position).getOrderAddress()));
           myViewHolder.txt_order_date.setText(new StringBuilder(mSimpleDateFormat.format(mOrderList.get(position).getOrderDate())));
           myViewHolder.txt_order_number.setText(new StringBuilder("Order Number : #").append(mOrderList.get(position).getOrderId()));
           myViewHolder.txt_order_phone.setText(new StringBuilder(mOrderList.get(position).getOrderPhone()));
           myViewHolder.txt_order_status.setText(Common.convertStatusToString(mOrderList.get(position).getOrderStatus()));
           myViewHolder.txt_order_total_price.setText(new StringBuilder(mContext.getString(R.string.money_sign)).append(mOrderList.get(position).getTotalPrice()));

           if (mOrderList.get(position).isCod()) {
               myViewHolder.txt_payment_method.setText(new StringBuilder("Cash On Delivery"));
           } else {
               myViewHolder.txt_payment_method.setText(new StringBuilder("TransID: ").append(mOrderList.get(position).getTransactionId()));
           }
       }
       else if (holder instanceof MyLoadingViewHolder) {
           MyLoadingViewHolder myLoadingViewHolder = (MyLoadingViewHolder) holder;

           myLoadingViewHolder.progress_bar.setIndeterminate(true);
       }
    }

    @Override
    public int getItemCount() {
        return mOrderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_order_phone)
        TextView txt_order_phone;
        @BindView(R.id.txt_order_address)
        TextView txt_order_address;
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @BindView(R.id.txt_order_total_price)
        TextView txt_order_total_price;
        @BindView(R.id.txt_num_of_item)
        TextView txt_num_of_item;
        @BindView(R.id.txt_payment_method)
        TextView txt_payment_method;

        Unbinder mUnbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);
        }
    }

    public class MyLoadingViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.progress_bar)
        ProgressBar progress_bar;

        Unbinder mUnbinder;

        public MyLoadingViewHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);
        }
    }
}
