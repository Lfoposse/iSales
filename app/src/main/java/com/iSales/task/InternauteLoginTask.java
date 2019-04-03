package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.interfaces.OnInternauteLoginComplete;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.Internaute;
import com.iSales.remote.model.InternauteSuccess;
import com.iSales.remote.rest.LoginREST;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 27/08/2018.
 */

public class InternauteLoginTask extends AsyncTask<Void, Void, com.iSales.remote.rest.LoginREST> {
    private static final String TAG = InternauteLoginTask.class.getSimpleName();

    private com.iSales.interfaces.OnInternauteLoginComplete taskComplete;
    private com.iSales.remote.model.Internaute internaute;

    private Context context;

    public InternauteLoginTask(Context context, OnInternauteLoginComplete task, Internaute internaute) {
        this.taskComplete = task;
        this.internaute = internaute;
        this.context = context;
    }

    @Override
    protected com.iSales.remote.rest.LoginREST doInBackground(Void... voids) {
//        Requete de connexion de l'internaute sur le serveur
        Call<com.iSales.remote.model.InternauteSuccess> call = ApiUtils.getISalesService(context).login(this.internaute);
        try {
            Response<com.iSales.remote.model.InternauteSuccess> response = call.execute();
            if (response.isSuccessful()) {
                InternauteSuccess internauteSuccess = response.body();
                Log.e(TAG, "doInBackground: internauteSuccess=" + internauteSuccess.getSuccess().getToken());
                return new com.iSales.remote.rest.LoginREST(internauteSuccess);
            } else {
                String error = null;
                com.iSales.remote.rest.LoginREST loginREST = new com.iSales.remote.rest.LoginREST();
                loginREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    loginREST.setErrorBody(error);

                } catch (IOException e) {

                    e.printStackTrace();
                }

                return loginREST;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(LoginREST loginREST) {
//        super.onPostExecute(internauteSuccess);
        this.taskComplete.onInternauteLoginTaskComplete(loginREST);
    }
}
