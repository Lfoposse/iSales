package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.UserEntry;
import com.iSales.interfaces.FindOrdersListener;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.OrderLine;
import com.iSales.remote.rest.FindOrderLinesREST;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 24/01/2019.
 */

public class FindOrderLinesTask extends AsyncTask<Void, Void, FindOrderLinesREST> {
    private static final String TAG = com.iSales.task.FindOrderLinesTask.class.getSimpleName();

    private FindOrdersListener task;
    private long cmde_ref;
    private long order_id;

    private UserEntry userEntry;

    private Context context;

    public FindOrderLinesTask(Context context, long order_id, long cmde_ref, FindOrdersListener taskComplete) {
        this.task = taskComplete;
        this.cmde_ref = cmde_ref;
        this.order_id = order_id;
        this.context = context;
        AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
        userEntry = mDb.userDao().getUser().get(0);
        Log.e(TAG, "FindOrderLinesTask: ");
    }

    @Override
    protected FindOrderLinesREST doInBackground(Void... voids) {
        Log.e(TAG, "doInBackground: ");

        String sqlfilters = "fk_user_author="+userEntry.getId();
//        Requete de connexion de l'internaute sur le serveur
        Call<ArrayList<OrderLine>> call = ApiUtils.getISalesService(context).findOrderLines(order_id);
        try {
            Response<ArrayList<OrderLine>> response = call.execute();
            if (response.isSuccessful()) {
                ArrayList<OrderLine> orderLinesArrayList = response.body();
                Log.e(TAG, "doInBackground: orderLinesArrayList=" + orderLinesArrayList.size());

                return new FindOrderLinesREST(orderLinesArrayList);
            } else {
                Log.e(TAG, "doInBackground: !isSuccessful");
                String error = null;
                FindOrderLinesREST findOrderLinesREST = new FindOrderLinesREST();
                findOrderLinesREST.setLines(null);
                findOrderLinesREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    findOrderLinesREST.setErrorBody(error);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return findOrderLinesREST;
            }
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FindOrderLinesREST findOrderLinesREST) {
        Log.e(TAG, "onPostExecute: ");
//        super.onPostExecute(findProductsREST);
        task.onFindOrderLinesTaskComplete(cmde_ref, order_id, findOrderLinesREST);
    }
}
