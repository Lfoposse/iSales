package com.rainbow_cl.i_sales.task;

import android.os.AsyncTask;
import android.util.Log;

import com.rainbow_cl.i_sales.interfaces.FindCategorieListener;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.Categorie;
import com.rainbow_cl.i_sales.remote.model.Product;
import com.rainbow_cl.i_sales.remote.rest.FindCategoriesREST;
import com.rainbow_cl.i_sales.remote.rest.FindProductsREST;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 05/09/2018.
 */

public class FindCategorieTask extends AsyncTask<Void, Void, FindCategoriesREST> {
    private static final String TAG = FindCategorieTask.class.getSimpleName();

    private FindCategorieListener task;
    private String sortfield;
    private String sortorder;
    private long limit;
    private long page;
    private String type;

    public FindCategorieTask(FindCategorieListener taskComplete, String sortfield, String sortorder, long limit, long page, String type) {
        this.task = taskComplete;
        this.sortfield = sortfield;
        this.sortorder = sortorder;
        this.limit = limit;
        this.page = page;
        this.type = type;
    }

    @Override
    protected FindCategoriesREST doInBackground(Void... voids) {
//        Requete de connexion de l'internaute sur le serveur
        Call<ArrayList<Categorie>> call = ApiUtils.getISalesService().findCategories(sortfield, this.sortorder, this.limit, this.page, this.type);
        try {
            Response<ArrayList<Categorie>> response = call.execute();
            if (response.isSuccessful()) {
                ArrayList<Categorie> categorieArrayList = response.body();
                Log.e(TAG, "doInBackground: Categorie=" + categorieArrayList.size());

                return new FindCategoriesREST(categorieArrayList);
            } else {
                String error = null;
                FindCategoriesREST findCategoriesREST = new FindCategoriesREST();
                findCategoriesREST.setCategories(null);
                findCategoriesREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    findCategoriesREST.setErrorBody(error);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return findCategoriesREST;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FindCategoriesREST findCategoriesREST) {
//        super.onPostExecute(findProductsREST);
        task.onFindCategorieCompleted(findCategoriesREST);
    }
}
