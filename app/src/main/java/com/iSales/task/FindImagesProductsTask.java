package com.iSales.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ProduitEntry;
import com.iSales.interfaces.FindImagesProductsListener;
import com.iSales.remote.ApiUtils;
import com.iSales.utility.ISalesUtility;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FindImagesProductsTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = FindDocumentTask.class.getSimpleName();

    private FindImagesProductsListener task;
    private ProduitEntry produitEntry;
    private AppDatabase mDb;

    private Context context;

    public FindImagesProductsTask(Context context, FindImagesProductsListener taskComplete, ProduitEntry produit) {
        this.task = taskComplete;
        this.context = context;
        this.produitEntry = produit;
        this.mDb = AppDatabase.getInstance(context);
    }

    @Override
    protected String doInBackground(Void... voids) {
        return downloadBitmapAndSave(ApiUtils.getDownloadProductImg(context, produitEntry.getRef()));
    }

    /*
    private Target getTarget(final ProduitEntry produitEntry) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                progress++;
                success++;

                String pathFile = ISalesUtility.saveProduitImage(context, bitmap, produitEntry.getRef());
                Log.e(TAG, "doInBackground:onBitmapLoaded pathFile=" + pathFile+
                        " pdtRef="+produitEntry.getRef()+
                        " progress="+progress+
                        " success="+success);

//                    Modification du path de la photo du produit
                mDb.produitDao().updateLocalImgPath(produitEntry.getId(), pathFile);
                task.onFindImgesProductsProgress(total, progress);
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                progress++;
                failed++;
                Log.e(TAG, " progress="+progress+
                        " failed="+failed);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };
    } */


    private String downloadBitmapAndSave(String path) {
        Log.e(TAG, "downloadBitmapAndSave path="+path);
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(path);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                String pathFile = ISalesUtility.saveProduitImage(context, bitmap, produitEntry.getRef());
//                Log.e(TAG, "doInBackground:onBitmapLoaded pathFile=" + pathFile+
//                        " pdtRef="+produitEntry.getRef());

//                    Modification du path de la photo du produit
                mDb.produitDao().updateLocalImgPath(produitEntry.getId(), pathFile);

                return pathFile;
            }
        } catch (Exception e) {
            Log.d(TAG,"URLCONNECTIONERROR "+e.toString());
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
//            Log.w(TAG,"ImageDownloader "+ "Error downloading image from " + path);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();

            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String pathFile) {
//        super.onPostExecute(findDolPhotoREST);
        if (task == null) {
            super.onPostExecute(pathFile);
            return;
        }

        task.onFindImagesProductsComplete(pathFile);
    }
}
