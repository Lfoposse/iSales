package com.rainbow_cl.i_sales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rainbow_cl.i_sales.interfaces.FindDocumentListener;
import com.rainbow_cl.i_sales.interfaces.FindProductsPosterListener;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindDolPhotoREST;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 10/10/2018.
 */

public class FindDocumentTask extends AsyncTask<Void, Void, FindDolPhotoREST> {
    private static final String TAG = FindDocumentTask.class.getSimpleName();

    private FindDocumentListener task;
    private String modulePart;
    private String originalFile;
    private int productsPosition;

    private Context context;

    public FindDocumentTask(Context context, FindDocumentListener taskComplete, String modulePart, String originalFile) {
        this.task = taskComplete;
        this.modulePart = modulePart;
        this.originalFile = originalFile;
        this.context = context;
    }

    @Override
    protected FindDolPhotoREST doInBackground(Void... voids) {

//        Requete de connexion de l'internaute sur le serveur
        Call<DolPhoto> call = ApiUtils.getISalesService(context).findDocument(modulePart, originalFile);
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

        task.onFindDocumentComplete(findDolPhotoREST);
    }
}
