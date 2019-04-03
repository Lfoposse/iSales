package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.Order;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by netserve on 08/11/2018.
 */

public class SendOrderTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = com.iSales.task.SendOrderTask.class.getSimpleName();
    private Order mOrder;

    private Context mContext;

    public SendOrderTask(Order order, Context context) {
        this.mOrder = order;
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

//        enregistrement de la commande dans le serveur
        Call<Long> callSave = ApiUtils.getISalesService(mContext).saveCustomerOrder(mOrder);
        callSave.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {

                if (response.isSuccessful()) {
                    final Long responseBody = response.body();
                    Log.e(TAG, "doInBackground: saveCustomerOrder orderId=" + responseBody);

//                    Validation de la commande sur le serveur
                    Call<Order> callValidate = ApiUtils.getISalesService(mContext).validateCustomerOrder(responseBody);
                    callValidate.enqueue(new Callback<Order>() {
                        @Override
                        public void onResponse(Call<Order> call, Response<Order> responseValidate) {
                            if (responseValidate.isSuccessful()) {
                                Order responseValiBody = responseValidate.body();

                                Log.e(TAG, "doInBackground:onResponse: validateCustomerOrder orderRef=" + responseValiBody.getRef() +
                                        " orderId=" + responseValiBody.getId());
                                return;
                            } else {

                                try {
                                    Log.e(TAG, "doInBackground: onResponse err: message=" + responseValidate.message() + " | code=" + responseValidate.code() + " | code=" + responseValidate.errorBody().string());
                                } catch (IOException e) {
                                    Log.e(TAG, "doInBackgroundonResponse: message=" + e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Order> call, Throwable t) {
                            Log.e(TAG, "onFailure: "+t.getMessage());

                            return;
                        }
                    });
                } else {

                    try {
                        Log.e(TAG, "doInBackground: onResponse err: message=" + response.message() + " | code=" + response.code() + " | code=" + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "doInBackgroundonResponse: message=" + e.getMessage());
                    }
                }
                return;
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e(TAG, "onFailure: "+t.getMessage());
                return;
            }
        });
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.e(TAG, "onPostExecute: ");
    }
}
