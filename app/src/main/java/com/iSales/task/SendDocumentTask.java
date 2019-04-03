package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.Document;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by netserve on 08/11/2018.
 */

public class SendDocumentTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = SendDocumentTask.class.getSimpleName();
    private com.iSales.remote.model.Document mDocument;

    private Context mContext;

    public SendDocumentTask(Document document, Context context) {
        this.mDocument = document;
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Call<String> call = ApiUtils.getISalesService(mContext).uploadDocument(mDocument);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String responseBody = response.body();
                    Log.e(TAG, "onResponse: responseSignClientBody=" + responseBody);
                    return;
                } else {
                    try {
                        Log.e(TAG, "doInBackground onResponse document err: message=" + response.message() +
                                " | code=" + response.code() + " | code=" + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: message=" + e.getMessage());
                    }
                    return;
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                return;
            }
        });

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.e(TAG, "onPostExecute: ");
    }
}
