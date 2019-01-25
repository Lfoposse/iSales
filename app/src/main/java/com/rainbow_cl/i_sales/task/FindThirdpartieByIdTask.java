package com.rainbow_cl.i_sales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rainbow_cl.i_sales.interfaces.FindThirdpartieListener;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.Thirdpartie;
import com.rainbow_cl.i_sales.remote.rest.FindThirdpartieREST;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 09/10/2018.
 */

public class FindThirdpartieByIdTask extends AsyncTask<Void, Void, Thirdpartie> {
    private static final String TAG = FindThirdpartieTask.class.getSimpleName();

    private FindThirdpartieListener task;
    private long thirdpartieId;
    private Context context;

    public FindThirdpartieByIdTask(Context context, FindThirdpartieListener taskComplete, long thirdpartieId) {
        this.task = taskComplete;
        this.thirdpartieId = thirdpartieId;
        this.context = context;
    }

    @Override
    protected Thirdpartie doInBackground(Void... voids) {
//        Requete de connexion de l'internaute sur le serveur
        Call<Thirdpartie> call = ApiUtils.getISalesService(context).findThirdpartieById(this.thirdpartieId);
        try {
            Response<Thirdpartie> response = call.execute();
            if (response.isSuccessful()) {
                Thirdpartie thirdpartie = response.body();
                Log.e(TAG, "doInBackground: firstName=" + thirdpartie.getFirstname());

                return thirdpartie;
            } else {
                String error = null;
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Thirdpartie thirdpartie) {
//        super.onPostExecute(findProductsREST);
        task.onFindThirdpartieByIdCompleted(thirdpartie);
    }
}
