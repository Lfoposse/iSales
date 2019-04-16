package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.PaymentTypesEntry;
import com.iSales.interfaces.FindPaymentTypesListener;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.PaymentTypes;
import com.iSales.remote.rest.FindPaymentTypesREST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 12/02/2019.
 */

public class FindPaymentTypesTask extends AsyncTask<Void, Void, FindPaymentTypesREST> {
    private static final String TAG = com.iSales.task.FindPaymentTypesTask.class.getSimpleName();

    private FindPaymentTypesListener task;
    private int active;

    private Context context;
    private AppDatabase mDb;

    public FindPaymentTypesTask(Context context, FindPaymentTypesListener taskComplete) {
        this.task = taskComplete;
        this.context = context;
        this.mDb = AppDatabase.getInstance(context);
    }

    @Override
    protected FindPaymentTypesREST doInBackground(Void... voids) {
        Log.e(TAG, "doInBackground: ");

//        Requete de connexion de l'internaute sur le serveur
        Call<ArrayList<PaymentTypes>> call = ApiUtils.getISalesService(context).findPaymentTypes(1);
        try {
            Response<ArrayList<PaymentTypes>> response = call.execute();
            if (response.isSuccessful()) {
                ArrayList<PaymentTypes> paymentTypesArrayList = response.body();
                Log.e(TAG, "doInBackground: paymentTypesArrayList=" + paymentTypesArrayList.size());

                return new FindPaymentTypesREST(paymentTypesArrayList);
            } else {
                Log.e(TAG, "doInBackground: !isSuccessful");
                String error = null;
                FindPaymentTypesREST findPaymentTypesREST = new FindPaymentTypesREST();
                findPaymentTypesREST.setPaymentTypes(null);
                findPaymentTypesREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    findPaymentTypesREST.setErrorBody(error);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return findPaymentTypesREST;
            }
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FindPaymentTypesREST findPaymentTypesREST) {
        Log.e(TAG, "onPostExecute: ");

        if (findPaymentTypesREST != null && findPaymentTypesREST.getPaymentTypes() != null) {
//        Insertion dans la BD
            List<PaymentTypesEntry> paymentTypesEntries = new ArrayList<>();
            for (PaymentTypes paymentTypes : findPaymentTypesREST.getPaymentTypes()) {
                Log.e(TAG, "onPostExecute: paymentTypes item="+paymentTypes.getLabel());
                PaymentTypesEntry paymentTypesEntry = new PaymentTypesEntry();
                paymentTypesEntry.setId(paymentTypes.getId());
                paymentTypesEntry.setCode(paymentTypes.getCode());
                paymentTypesEntry.setLabel(paymentTypes.getLabel());
                paymentTypesEntry.setType(paymentTypes.getType());

                paymentTypesEntries.add(paymentTypesEntry);
            }
            mDb.paymentTypesDao().insertAllPayments(paymentTypesEntries);
        }

        if (task != null) {
            task.onFindPaymentTypesComplete(findPaymentTypesREST);
        }
//        super.onPostExecute(findProductsREST);
    }
}
