package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.interfaces.FindThirdpartieListener;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.Thirdpartie;
import com.iSales.remote.rest.FindThirdpartieREST;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 07/09/2018.
 */

public class FindThirdpartieTask extends AsyncTask<Void, Void, FindThirdpartieREST> {
    private static final String TAG = com.iSales.task.FindThirdpartieTask.class.getSimpleName();

    private FindThirdpartieListener task;
    private long limit;
    private long page;
    private int mode;
    private Context context;

    public FindThirdpartieTask(Context context, FindThirdpartieListener taskComplete, long limit, long page, int mode) {
        this.task = taskComplete;
        this.limit = limit;
        this.page = page;
        this.mode = mode;
        this.context = context;
    }

    @Override
    protected FindThirdpartieREST doInBackground(Void... voids) {
//        Requete de connexion de l'internaute sur le serveur
        Call<ArrayList<Thirdpartie>> call = ApiUtils.getISalesService(context).findThirdpartie(this.limit, this.page, this.mode);
        try {
            Response<ArrayList<Thirdpartie>> response = call.execute();
            if (response.isSuccessful()) {
                ArrayList<Thirdpartie> productArrayList = response.body();
//                Log.e(TAG, "doInBackground: products=" + productArrayList.size());

                return new FindThirdpartieREST(productArrayList);
            } else {
                String error = null;
                FindThirdpartieREST findThirdpartieREST = new FindThirdpartieREST();
                findThirdpartieREST.setThirdparties(null);
                findThirdpartieREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    findThirdpartieREST.setErrorBody(error);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return findThirdpartieREST;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FindThirdpartieREST findThirdpartieREST) {
//        super.onPostExecute(findProductsREST);
        task.onFindThirdpartieCompleted(findThirdpartieREST);
    }
}
