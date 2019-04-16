package com.iSales.remote;

import android.content.Context;
import android.util.Log;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ServerEntry;
import com.iSales.database.entry.TokenEntry;

/**
 * Created by netserve on 03/08/2018.
 */

public final class ApiUtils {
    private static final String TAG = ApiUtils.class.getSimpleName();

    public static final String BASE_URL = "http://dolibarr.bananafw.com/api/index.php/";
//    public static final String BASE_URL = "http://srv.apps-dev.fr/api/index.php/";

    public static final String BASE_URL_IMG = "http://isales-img.rainbowcl.net/download.php";

    //    Movies DB api key
    public static final String API_KEY = "9c524dc13288320153128086e6e69144fa743be3";

    //    body query properties
    public static final String DOLAPIKEY = "DOLAPIKEY";

    public static final String sortfield = "sortfield";
    public static final String active = "active";
    public static final String sortorder = "sortorder";
    public static final String sqlfilters = "sqlfilters";
    public static final String limit = "limit";
    public static final String page = "page";
    public static final String mode = "mode";
    public static final String category = "category";
    public static final String type = "type";
    public static final String module_part = "module_part";
    public static final String original_file = "original_file";
    public static final String thirdparty_ids = "thirdparty_ids";
    public static final String id = "id";
    public static final String soc_id = "soc_id";

    //    Mode de recuperation des thirdpartie
    public static final int THIRDPARTIE_CLIENT = 1;

    private ApiUtils() {
    }

    //get an instance of Movies API services
    public static com.iSales.remote.ISalesServicesRemote getISalesService(Context context) {
        com.iSales.database.AppDatabase mDb = com.iSales.database.AppDatabase.getInstance(context.getApplicationContext());

        com.iSales.database.entry.ServerEntry serverEntry = mDb.serverDao().getActiveServer(true);

        if (serverEntry == null) {
            return com.iSales.remote.RetrofitClient.getClient(context, "/").create(com.iSales.remote.ISalesServicesRemote.class);
        }
//        Log.e(TAG, "getISalesService: serverEntry="+serverEntry.getHostname());
//        return RetrofitClient.getClient(context, BASE_URL).create(ISalesServicesRemote.class);
        return com.iSales.remote.RetrofitClient.getClient(context, serverEntry.getHostname()+"/").create(com.iSales.remote.ISalesServicesRemote.class);
    }

    //get an instance of Movies API services
    public static String getDownloadImg(Context context, String module_part, String original_file) {
        com.iSales.database.AppDatabase mDb = com.iSales.database.AppDatabase.getInstance(context.getApplicationContext());
        com.iSales.database.entry.TokenEntry tokenEntry = mDb.tokenDao().getAllToken().get(0);
        com.iSales.database.entry.ServerEntry serverEntry = mDb.serverDao().getActiveServer(true);
//        http://localhost:8888/Images.iSales/download.php?module_part=produit&original_file=cheese_cake/cheese_cake-Cheese_cake.jpg&DOLAPIKEY=9c524dc13288320153128086e6e69144fa743be3
        String url = String.format("%s/download.php?module_part=%s&original_file=%s&DOLAPIKEY=%s", serverEntry.getHostname_img(), module_part, original_file, tokenEntry.getToken());
//        Log.e(TAG, "getDownloadImg: url="+url);
        return url;
    }

    // Renvoit l'url de recuperation des images de produit
    public static String getDownloadProductImg(Context context, String ref) {
        com.iSales.database.AppDatabase mDb = com.iSales.database.AppDatabase.getInstance(context.getApplicationContext());
        TokenEntry tokenEntry = mDb.tokenDao().getAllToken().get(0);
        com.iSales.database.entry.ServerEntry serverEntry = mDb.serverDao().getActiveServer(true);
//        http://localhost:8888/Images.iSales/download.php?module_part=produit&original_file=cheese_cake/cheese_cake-Cheese_cake.jpg&DOLAPIKEY=9c524dc13288320153128086e6e69144fa743be3
        String url = String.format("%s/product.php?ref=%s&DOLAPIKEY=%s", serverEntry.getHostname_img(), ref, tokenEntry.getToken());
//        Log.e(TAG, "getDownloadImg: url="+url);
        return url;
    }

    // Renvoit l'url de recuperation des produits virtuels
    public static com.iSales.remote.ISalesServicesRemote getISalesRYImg(Context context) {

        com.iSales.database.AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());

        ServerEntry serverEntry = mDb.serverDao().getActiveServer(true);

        if (serverEntry == null) {
            return com.iSales.remote.RetrofitClient.getClient(context, "/").create(com.iSales.remote.ISalesServicesRemote.class);
        }
        String url = String.format("%s/", serverEntry.getHostname_img());
//        Log.e(TAG, "getISalesProductVirtual: url="+url);

        return RetrofitClient.getClient(context, url).create(ISalesServicesRemote.class);
    }

}
