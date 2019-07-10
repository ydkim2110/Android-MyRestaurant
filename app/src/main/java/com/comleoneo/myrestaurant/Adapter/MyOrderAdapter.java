package com.comleoneo.myrestaurant.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Model.Order;
import com.comleoneo.myrestaurant.R;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder> {

    private Context mContext;
    private List<Order> mOrderList;
    private SimpleDateFormat mSimpleDateFormat;

    public MyOrderAdapter(Context context, List<Order> orderList) {
        mContext = context;
        mOrderList = orderList;
        mSimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_num_of_item.setText(new StringBuilder("Num of Items: ").append(mOrderList.get(position).getNumOfItem()));
        holder.txt_order_address.setText(new StringBuilder(mOrderList.get(position).getOrderAddress()));
        holder.txt_order_date.setText(new StringBuilder(mSimpleDateFormat.format(mOrderList.get(position).getOrderDate())));
        holder.txt_order_number.setText(new StringBuilder("Order Number : #").append(mOrderList.get(position).getOrderId()));
        holder.txt_order_phone.setText(new StringBuilder(mOrderList.get(position).getOrderPhone()));
        holder.txt_order_status.setText(Common.convertStatusdToString(mOrderList.get(position).getOrderStatus()));
        holder.txt_order_total_price.setText(new StringBuilder(mContext.getString(R.string.money_sign)).append(mOrderList.get(position).getTotalPrice()));

        if (mOrderList.get(position).isCod()) {
            holder.txt_payment_method.setText(new StringBuilder("Cash On Delivery"));
        } else {
            holder.txt_payment_method.setText(new StringBuilder("TransID: ").append(mOrderList.get(position).getTransactionId()));
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
}
