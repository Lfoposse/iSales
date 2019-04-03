package com.iSales.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ProductCustPriceEntry;
import com.iSales.interfaces.FindProductCustPriceListener;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.ProductCustomerPrice;
import com.iSales.remote.rest.FindProductCustomerPriceREST;
import com.iSales.task.FindProductVirtualTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FindProductCustomerPriceTask extends AsyncTask<Void, Void, FindProductCustomerPriceREST> {
    private static final String TAG = FindProductVirtualTask.class.getSimpleName();

    private FindProductCustPriceListener task;
    private long customerId;

    private Context context;
    private AppDatabase mDb;

    public FindProductCustomerPriceTask(Context context, long customerId, FindProductCustPriceListener taskComplete) {
        this.task = taskComplete;
        this.context = context;
        this.customerId = customerId;
        this.mDb = AppDatabase.getInstance(context);
    }

    @Override
    protected FindProductCustomerPriceREST doInBackground(Void... voids) {
        Log.e(TAG, "doInBackground: ");

//        Requete de connexion de l'internaute sur le serveur
        Call<List<ProductCustomerPrice>> call = ApiUtils.getISalesRYImg(context).ryFindProductPrice(this.customerId);
        try {
            Response<List<ProductCustomerPrice>> response = call.execute();
            if (response.isSuccessful()) {
                List<ProductCustomerPrice> productCustomerPrices = response.body();
                Log.e(TAG, "doInBackground: FindProductVirtualREST=" + productCustomerPrices.size());

                FindProductCustomerPriceREST rest = new FindProductCustomerPriceREST();
                rest.setProductCustomerPrices(productCustomerPrices);
                rest.setCustomer_id(this.customerId);

                return rest;
            } else {
                Log.e(TAG, "doInBackground: !isSuccessful");
                String error = null;
                FindProductCustomerPriceREST findProductCustomerPriceREST = new FindProductCustomerPriceREST();
                findProductCustomerPriceREST.setProductCustomerPrices(null);
                findProductCustomerPriceREST.setErrorCode(response.code());
                try {
                    error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                    Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                    findProductCustomerPriceREST.setErrorBody(error);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return findProductCustomerPriceREST;
            }
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FindProductCustomerPriceREST findProductCustomerPriceREST) {
//        Log.e(TAG, "onPostExecute: ");

        mDb.productCustPriceDao().deleteAllProductCustPrice();

        if (findProductCustomerPriceREST.getProductCustomerPrices() != null) {
//        Insertion dans la BD
            List<ProductCustPriceEntry> productCustPriceEntries = new ArrayList<>();
            for (ProductCustomerPrice itemProduct : findProductCustomerPriceREST.getProductCustomerPrices()) {
                Log.e(TAG, "onPostExecute: ProductCustomerPrice item="+itemProduct.getFk_product());
                ProductCustPriceEntry productCustPriceEntry = new ProductCustPriceEntry();
                productCustPriceEntry.setRowid(Long.parseLong(itemProduct.getRowid()));
                productCustPriceEntry.setEntity(itemProduct.getEntity());
                productCustPriceEntry.setDatec(itemProduct.getDatec());
                productCustPriceEntry.setTms(itemProduct.getTms());
                productCustPriceEntry.setFk_product(Long.parseLong(itemProduct.getFk_product()));
                productCustPriceEntry.setFk_soc(Long.parseLong(itemProduct.getFk_soc()));
                productCustPriceEntry.setPrice(itemProduct.getPrice());
                productCustPriceEntry.setPrice_ttc(itemProduct.getPrice_ttc());
                productCustPriceEntry.setPrice_min(itemProduct.getPrice_min());
                productCustPriceEntry.setPrice_min_ttc(itemProduct.getPrice_min_ttc());
                productCustPriceEntry.setPrice_base_type(itemProduct.getPrice_base_type());
                productCustPriceEntry.setDefault_vat_code(itemProduct.getDefault_vat_code());
                productCustPriceEntry.setTva_tx(itemProduct.getTva_tx());
                productCustPriceEntry.setRecuperableonly(itemProduct.getRecuperableonly());
                productCustPriceEntry.setLocaltax1_tx(itemProduct.getLocaltax1_tx());
                productCustPriceEntry.setLocaltax1_type(itemProduct.getLocaltax1_type());
                productCustPriceEntry.setLocaltax2_tx(itemProduct.getLocaltax2_tx());
                productCustPriceEntry.setLocaltax2_type(itemProduct.getLocaltax2_type());
                productCustPriceEntry.setLocaltax2_type(itemProduct.getLocaltax2_type());
                productCustPriceEntry.setFk_user(itemProduct.getFk_user());
                productCustPriceEntry.setImport_key(itemProduct.getImport_key());

                productCustPriceEntries.add(productCustPriceEntry);
            }

            mDb.productCustPriceDao().insertAllProducCustPrice(productCustPriceEntries);
        }

        if (task != null) {
            task.onFindProductCustPriceCompleted(findProductCustomerPriceREST);
        }
    }
}
