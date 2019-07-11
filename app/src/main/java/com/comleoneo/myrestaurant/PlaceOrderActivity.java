package com.comleoneo.myrestaurant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Database.CartDataSource;
import com.comleoneo.myrestaurant.Database.CartDatabase;
import com.comleoneo.myrestaurant.Database.LocalCartDataSource;
import com.comleoneo.myrestaurant.Model.EventBust.SendTotalCashEvent;
import com.comleoneo.myrestaurant.Retrofit.IBraintreeAPI;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitBraintreeClient;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlaceOrderActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = PlaceOrderActivity.class.getSimpleName();

    private static final int REQUEST_BRAINTREE_CODE = 7777;

    @BindView(R.id.edt_date)
    EditText edt_date;
    @BindView(R.id.txt_total_cash)
    TextView txt_total_cash;
    @BindView(R.id.txt_user_phone)
    TextView txt_user_phone;
    @BindView(R.id.txt_user_address)
    TextView txt_user_address;
    @BindView(R.id.txt_new_address)
    TextView txt_new_address;
    @BindView(R.id.btn_add_new_address)
    Button btn_add_new_address;
    @BindView(R.id.chb_default_address)
    CheckBox chb_default_address;
    @BindView(R.id.rdi_cod)
    RadioButton rdi_cod;
    @BindView(R.id.rdi_online_payment)
    RadioButton rdi_online_payment;
    @BindView(R.id.btn_proceed)
    Button btn_proceed;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private IBraintreeAPI mIBraintreeAPI;
    private CartDataSource mCartDataSource;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    private boolean isSelectedDate = false;
    private boolean isAddNewAddress = false;

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
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        Log.d(TAG, "onCreate: started!!");

        init();
        initView();

    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        txt_user_phone.setText(Common.currentUser.getUserPhone());
        txt_user_address.setText(Common.currentUser.getAddress());

        toolbar.setTitle(getString(R.string.place_order));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_add_new_address.setOnClickListener(v -> {
            isAddNewAddress = true;
            chb_default_address.setChecked(false);

            View layout_add_new_address = LayoutInflater.from(PlaceOrderActivity.this)
                    .inflate(R.layout.layout_add_new_address, null);

            EditText edt_new_address = layout_add_new_address.findViewById(R.id.edt_add_new_address);
            edt_new_address.setText(txt_new_address.getText().toString());

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PlaceOrderActivity.this)
                    .setTitle("Add New Address")
                    .setView(layout_add_new_address)
                    .setNegativeButton("CANCEL", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton("ADD", (dialog, which) -> {
                        txt_new_address.setText(edt_new_address.getText().toString());
                    });

            androidx.appcompat.app.AlertDialog addNewAdressDialog = builder.create();
            addNewAdressDialog.show();
        });

        edt_date.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();

            DatePickerDialog dpd = DatePickerDialog.newInstance(PlaceOrderActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH));

            dpd.show(getSupportFragmentManager(), "Datepickerdialog");
        });

        btn_proceed.setOnClickListener(v -> {
            if (!isSelectedDate) {
                Toast.makeText(this, "Please select Date", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                String dateString = edt_date.getText().toString();
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    Date orderDate = df.parse(dateString);

                    // Get current date
                    Calendar calendar = Calendar.getInstance();

                    Date currentDate = df.parse(df.format(calendar.getTime()));

                    if (!DateUtils.isToday(orderDate.getTime())) {
                        if (orderDate.before(currentDate)) {
                            Toast.makeText(this, "Please choose current date or future day", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (!isAddNewAddress) {
                if (!chb_default_address.isChecked()) {
                    Toast.makeText(this, "Please choose default Adress or set new address", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (rdi_cod.isChecked()) {
                getOrderNumber(false);
            }
            else if (rdi_online_payment.isChecked()) {
                getOrderNumber(true);
            }
        });
    }

    private void getOrderNumber(boolean isOnlinePayment) {
        Log.d(TAG, "getOrderNumber: called!!");
        mDialog.show();
        if (!isOnlinePayment) {
            String address = chb_default_address.isChecked() ? txt_user_address.getText().toString() : txt_new_address.getText().toString();

            mCompositeDisposable.add(mCartDataSource.getAllCart(Common.currentUser.getFbid(),
                    Common.currentRestaurant.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItems -> {
                        // Get Order Number from Server
                        mCompositeDisposable.add(mIMyRestaurantAPI.createOrder(Common.API_KEY,
                                Common.currentUser.getFbid(),
                                Common.currentUser.getUserPhone(),
                                Common.currentUser.getName(),
                                address,
                                edt_date.getText().toString(),
                                Common.currentRestaurant.getId(),
                                "NONE",
                                true,
                                Double.valueOf(txt_total_cash.getText().toString()),
                                cartItems.size())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(createOrderModel -> {
                                    if (createOrderModel.isSuccess()) {
                                        // After have order number, we will update all item of this order to order Detail
                                        // First, select Cart items
                                        mCompositeDisposable.add(mIMyRestaurantAPI.updateOrder(Common.API_KEY,
                                                String.valueOf(createOrderModel.getResult().get(0).getOrderNumber()),
                                                new Gson().toJson(cartItems))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(updateOrderModel -> {

                                                    if (updateOrderModel.isSuccess()) {
                                                        // After update item, we will clear cart and show message success
                                                        mCartDataSource.cleanCart(Common.currentUser.getFbid(),
                                                                Common.currentRestaurant.getId())
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new SingleObserver<Integer>() {
                                                                    @Override
                                                                    public void onSubscribe(Disposable d) {

                                                                    }

                                                                    @Override
                                                                    public void onSuccess(Integer integer) {
                                                                        Toast.makeText(PlaceOrderActivity.this, "Order :Placed", Toast.LENGTH_SHORT).show();
                                                                        Intent homeActivity = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                        homeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                        startActivity(homeActivity);
                                                                        finish();
                                                                    }

                                                                    @Override
                                                                    public void onError(Throwable e) {
                                                                        Toast.makeText(PlaceOrderActivity.this, "[CLEAR CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }

                                                    if (mDialog.isShowing())
                                                        mDialog.dismiss();

                                                }, throwable -> {
                                                    mDialog.show();
                                                    Toast.makeText(this, "[UPDATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                }));
                                    } else {
                                        mDialog.dismiss();
                                        Toast.makeText(this, "[CREATE ORDER]" + createOrderModel.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }, throwable -> {
                                    mDialog.dismiss();
                                    Toast.makeText(this, "[CREATE ORDER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }));
                    }, throwable -> {
                        Toast.makeText(this, "[GET ALL CART]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
        }
        // If online payment, First get token
        else {
            mCompositeDisposable.add(mIBraintreeAPI.getToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(braintreeToken -> {

                if (braintreeToken.isSuccess()) {
                    DropInRequest dropInRequest = new DropInRequest().clientToken(braintreeToken.getClientToken());
                    startActivityForResult(dropInRequest.getIntent(PlaceOrderActivity.this), REQUEST_BRAINTREE_CODE);

                }
                else {
                    Toast.makeText(this, "Cannot get Token", Toast.LENGTH_SHORT).show();
                }
                
                mDialog.dismiss();

            }, throwable -> {
                mDialog.dismiss();
                Toast.makeText(this, "[GET TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BRAINTREE_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();

                // After have nonce, we just made a payment with API
                if (!TextUtils.isEmpty(txt_total_cash.getText().toString())) {
                    String amount = txt_total_cash.getText().toString();

                    if (!mDialog.isShowing()) {
                        mDialog.show();
                    }

                    String address = chb_default_address.isChecked() ? txt_user_address.getText().toString() : txt_new_address.getText().toString();

                    mCompositeDisposable.add(mIBraintreeAPI.submitPayment(amount, nonce.getNonce())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(braintreeTransaction -> {

                        if (braintreeTransaction.isSuccess()) {
                            if (!mDialog.isShowing()) mDialog.show();

                            // After we have transaction, just make order like COD payment
                            mCompositeDisposable.add(mCartDataSource.getAllCart(Common.currentUser.getFbid(),
                                    Common.currentRestaurant.getId())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(cartItems -> {
                                        // Get Order Number from Server
                                        mCompositeDisposable.add(mIMyRestaurantAPI.createOrder(Common.API_KEY,
                                                Common.currentUser.getFbid(),
                                                Common.currentUser.getUserPhone(),
                                                Common.currentUser.getName(),
                                                address,
                                                edt_date.getText().toString(),
                                                Common.currentRestaurant.getId(),
                                                braintreeTransaction.getTransaction().getId(),
                                                false,
                                                Double.valueOf(amount),
                                                cartItems.size())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(createOrderModel -> {
                                                    if (createOrderModel.isSuccess()) {
                                                        // After have order number, we will update all item of this order to order Detail
                                                        // First, select Cart items
                                                        mCompositeDisposable.add(mIMyRestaurantAPI.updateOrder(Common.API_KEY,
                                                                String.valueOf(createOrderModel.getResult().get(0).getOrderNumber()),
                                                                new Gson().toJson(cartItems))
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(updateOrderModel -> {

                                                                    if (updateOrderModel.isSuccess()) {
                                                                        // After update item, we will clear cart and show message success
                                                                        mCartDataSource.cleanCart(Common.currentUser.getFbid(),
                                                                                Common.currentRestaurant.getId())
                                                                                .subscribeOn(Schedulers.io())
                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                .subscribe(new SingleObserver<Integer>() {
                                                                                    @Override
                                                                                    public void onSubscribe(Disposable d) {

                                                                                    }

                                                                                    @Override
                                                                                    public void onSuccess(Integer integer) {
                                                                                        Toast.makeText(PlaceOrderActivity.this, "Order :Placed", Toast.LENGTH_SHORT).show();
                                                                                        Intent homeActivity = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                        homeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                        startActivity(homeActivity);
                                                                                        finish();
                                                                                    }

                                                                                    @Override
                                                                                    public void onError(Throwable e) {
                                                                                        Toast.makeText(PlaceOrderActivity.this, "[CLEAR CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                    }

                                                                    if (mDialog.isShowing()) mDialog.dismiss();
                                                                }, throwable -> {
                                                                    mDialog.show();
                                                                    Toast.makeText(PlaceOrderActivity.this, "[UPDATE ORDER]", Toast.LENGTH_SHORT).show();
                                                                }));
                                                    } else {
                                                        mDialog.dismiss();
                                                        Toast.makeText(PlaceOrderActivity.this, "[CREATE ORDER]" + createOrderModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }, throwable -> {
                                                    mDialog.dismiss();
                                                    Toast.makeText(PlaceOrderActivity.this, "[CREATE ORDER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                }));
                                    }, throwable -> {
                                        Toast.makeText(PlaceOrderActivity.this, "[GET ALL CART]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }
                        else {
                            mDialog.dismiss();
                        }


                    }, throwable -> {
                        if (mDialog.isShowing()) mDialog.dismiss();
                        Toast.makeText(PlaceOrderActivity.this, "[SUBMIT PAYMENT]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
                }
            }
        }
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        Log.d(TAG, "init: "+Common.currentRestaurant.getPaymentUrl());
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
        mIBraintreeAPI = RetrofitBraintreeClient.getInstance(Common.currentRestaurant.getPaymentUrl()).create(IBraintreeAPI.class);
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Log.d(TAG, "onDateSet: called!!");
        // Implement late
        isSelectedDate = true;
        edt_date.setText(new StringBuilder("")
                .append(monthOfYear+1)
                .append("/")
                .append(dayOfMonth)
                .append("/")
                .append(year));
    }

    // Event Bus

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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void setTotalCash(SendTotalCashEvent event) {
        txt_total_cash.setText(String.valueOf(event.getCash()));
    }
}
