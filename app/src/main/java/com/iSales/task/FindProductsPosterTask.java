package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.interfaces.FindProductsPosterListener;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.DolPhoto;
import com.iSales.remote.rest.FindDolPhotoREST;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 30/08/2018.
 */

public class FindProductsPosterTask extends AsyncTask<Void, Void, FindDolPhotoREST> {
    private static final String TAG = com.iSales.task.FindProductsPosterTask.class.getSimpleName();

    private FindProductsPosterListener task;
    private String photoName;
    private String refProducts;
    private int productsPosition;

    private Context context;

    public FindProductsPosterTask(Context context, FindProductsPosterListener taskComplete, String photoName, String refProducts, int productsPosition) {
        this.task = taskComplete;
        this.photoName = photoName;
        this.refProducts = refProducts;
        this.productsPosition = productsPosition;
        this.context = context;
    }

    @Override
    protected FindDolPhotoREST doInBackground(Void... voids) {
        String original_file = refProducts + "%2F" + photoName;
        String module_part = "product";

//        Requete de connexion de l'internaute sur le serveur
        Call<DolPhoto> call = ApiUtils.getISalesService(context).findProductsPoster(module_part, original_file);
        try {
            Response<DolPhoto> response = call.execute();
            if (response.isSuccessful()) {
                DolPhoto dolPhoto = response.body();
                Log.e(TAG, "doInBackground: dolPhoto | Filename=" + dolPhoto.getFilename() + " content=" + dolPhoto.getContent());
                return new FindDolPhotoREST(dolPhoto);
            } else {
                String error = null;
                FindDolPhotoREST findDolPhotoREST = new FindDolPhotoREST();
                findDolPhotoREST.setDolPhoto(null);
                findDolPhotoREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
//                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    findDolPhotoREST.setErrorBody(error);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return findDolPhotoREST;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FindDolPhotoREST findDolPhotoREST) {
//        super.onPostExecute(findDolPhotoREST);
        if (task == null) {
            super.onPostExecute(findDolPhotoREST);
            return;
        }

        task.onFindProductsPosterComplete(findDolPhotoREST, productsPosition);
    }
}
