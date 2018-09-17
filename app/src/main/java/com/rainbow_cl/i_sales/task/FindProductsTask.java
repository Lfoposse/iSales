package com.rainbow_cl.i_sales.task;

import android.os.AsyncTask;
import android.util.Log;

import com.rainbow_cl.i_sales.interfaces.FindProductsListener;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindProductsREST;
import com.rainbow_cl.i_sales.remote.model.Product;
import com.rainbow_cl.i_sales.utility.ISalesUtility;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 29/08/2018.
 */

public class FindProductsTask extends AsyncTask<Void, Void, FindProductsREST> {
    private static final String TAG = FindProductsTask.class.getSimpleName();

    private FindProductsListener task;
    private String sortfield;
    private String sortorder;
    private long limit;
    private long page;
    private long category;
    private int mode = 1;

    public FindProductsTask(FindProductsListener taskComplete, String sortfield, String sortorder, long limit, long page, long category) {
        this.task = taskComplete;
        this.sortfield = sortfield;
        this.sortorder = sortorder;
        this.limit = limit;
        this.page = page;
        this.category = category;
    }

    @Override
    protected FindProductsREST doInBackground(Void... voids) {
//        Requete de connexion de l'internaute sur le serveur
        Call<ArrayList<Product>> call = ApiUtils.getISalesService().findProducts(sortfield, this.sortorder, this.limit, this.page, this.category, this.mode);
        try {
            Response<ArrayList<Product>> response = call.execute();
            if (response.isSuccessful()) {
                ArrayList<Product> productArrayList = response.body();
                ArrayList<Product> products = new ArrayList<>();
                Log.e(TAG, "doInBackground: products=" + productArrayList.size());

                return new FindProductsREST(productArrayList);
            } else {
                String error = null;
                FindProductsREST findProductsREST = new FindProductsREST();
                findProductsREST.setProducts(null);
                findProductsREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    findProductsREST.setErrorBody(error);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return findProductsREST;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FindProductsREST findProductsREST) {
//        super.onPostExecute(findProductsREST);
        task.onFindProductsCompleted(findProductsREST);
    }
}
