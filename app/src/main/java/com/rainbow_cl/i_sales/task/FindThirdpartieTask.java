package com.rainbow_cl.i_sales.task;

import android.os.AsyncTask;
import android.util.Log;

import com.rainbow_cl.i_sales.interfaces.FindProductsListener;
import com.rainbow_cl.i_sales.interfaces.FindThirdpartieListener;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.Product;
import com.rainbow_cl.i_sales.remote.model.Thirdpartie;
import com.rainbow_cl.i_sales.remote.rest.FindProductsREST;
import com.rainbow_cl.i_sales.remote.rest.FindThirdpartieREST;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 07/09/2018.
 */

public class FindThirdpartieTask extends AsyncTask<Void, Void, FindThirdpartieREST> {
    private static final String TAG = FindThirdpartieTask.class.getSimpleName();

    private FindThirdpartieListener task;
    private long limit;
    private long page;
    private int mode;

    public FindThirdpartieTask(FindThirdpartieListener taskComplete, long limit, long page, int mode) {
        this.task = taskComplete;
        this.limit = limit;
        this.page = page;
        this.mode = mode;
    }

    @Override
    protected FindThirdpartieREST doInBackground(Void... voids) {
//        Requete de connexion de l'internaute sur le serveur
        Call<ArrayList<Thirdpartie>> call = ApiUtils.getISalesService().findThirdpartie(this.limit, this.page, this.mode);
        try {
            Response<ArrayList<Thirdpartie>> response = call.execute();
            if (response.isSuccessful()) {
                ArrayList<Thirdpartie> productArrayList = response.body();
                Log.e(TAG, "doInBackground: products=" + productArrayList.size());

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
