package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.UserEntry;
import com.iSales.interfaces.FindOrdersListener;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.Order;
import com.iSales.remote.rest.FindOrdersREST;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 03/10/2018.
 */

public class FindOrderTask extends AsyncTask<Void, Void, FindOrdersREST> {
    private static final String TAG = com.iSales.task.FindOrderTask.class.getSimpleName();

    private FindOrdersListener task;
    private String sortfield;
    private String sortorder;
    private long limit;
    private long page;
    private long thirdparty_ids;

    private UserEntry userEntry;

    private Context context;

    public FindOrderTask(Context context, FindOrdersListener taskComplete, String sortfield, String sortorder, long limit, long page) {
        this.task = taskComplete;
        this.sortfield = sortfield;
        this.sortorder = sortorder;
        this.limit = limit;
        this.page = page;
        this.context = context;
        AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
        userEntry = mDb.userDao().getUser().get(0);
        Log.e(TAG, "FindOrderTask: ");
    }

    @Override
    protected FindOrdersREST doInBackground(Void... voids) {
        Log.e(TAG, "doInBackground: ");

        String sqlfilters = "fk_user_author="+userEntry.getId();
//        Requete de connexion de l'internaute sur le serveur
        Call<ArrayList<Order>> call = ApiUtils.getISalesService(context).findOrders(sqlfilters, sortfield, sortorder, limit, page);
        try {
            Response<ArrayList<Order>> response = call.execute();
            if (response.isSuccessful()) {
                ArrayList<Order> orderArrayList = response.body();
                Log.e(TAG, "doInBackground: orderArrayList=" + orderArrayList.size());

                return new FindOrdersREST(orderArrayList);
            } else {
                Log.e(TAG, "doInBackground: !isSuccessful");
                String error = null;
                FindOrdersREST findOrdersREST = new FindOrdersREST();
                findOrdersREST.setOrders(null);
                findOrdersREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    findOrdersREST.setErrorBody(error);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return findOrdersREST;
            }
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FindOrdersREST findOrdersREST) {
        Log.e(TAG, "onPostExecute: ");
//        super.onPostExecute(findProductsREST);
        task.onFindOrdersTaskComplete(findOrdersREST);
    }
}
