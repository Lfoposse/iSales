package com.rainbow_cl.i_sales.task;

import android.os.AsyncTask;
import android.util.Log;

import com.rainbow_cl.i_sales.interfaces.OnInternauteLoginComplete;
import com.rainbow_cl.i_sales.remote.rest.LoginREST;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.Internaute;
import com.rainbow_cl.i_sales.remote.model.InternauteSuccess;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 27/08/2018.
 */

public class InternauteLoginTask extends AsyncTask<Void, Void, LoginREST> {
    private static final String TAG = InternauteLoginTask.class.getSimpleName();

    private OnInternauteLoginComplete taskComplete;
    private Internaute internaute;

    public InternauteLoginTask(OnInternauteLoginComplete task, Internaute internaute) {
        this.taskComplete = task;
        this.internaute = internaute;
    }

    @Override
    protected LoginREST doInBackground(Void... voids) {
//        Requete de connexion de l'internaute sur le serveur
        Call<InternauteSuccess> call = ApiUtils.getISalesService().login(this.internaute);
        try {
            Response<InternauteSuccess> response = call.execute();
            if (response.isSuccessful()) {
                InternauteSuccess internauteSuccess = response.body();
                Log.e(TAG, "doInBackground: internauteSuccess=" + internauteSuccess.getSuccess().getToken());
                return new LoginREST(internauteSuccess);
            } else {
                String error = null;
                LoginREST loginREST = new LoginREST();
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
