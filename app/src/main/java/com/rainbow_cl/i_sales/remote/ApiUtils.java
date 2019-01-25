package com.rainbow_cl.i_sales.remote;

import android.content.Context;
import android.util.Log;

import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.entry.ServerEntry;
import com.rainbow_cl.i_sales.database.entry.TokenEntry;

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

    //    Mode de recuperation des thirdpartie
    public static final int THIRDPARTIE_CLIENT = 1;

    private ApiUtils() {
    }

    //get an instance of Movies API services
    public static ISalesServicesRemote getISalesService(Context context) {
        AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());

        ServerEntry serverEntry = mDb.serverDao().getActiveServer(true);

        Log.e(TAG, "getISalesService: serverEntry="+serverEntry.getHostname());
//        return RetrofitClient.getClient(context, BASE_URL).create(ISalesServicesRemote.class);
        return RetrofitClient.getClient(context, serverEntry.getHostname()+"/").create(ISalesServicesRemote.class);
    }

    //get an instance of Movies API services
    public static String getDownloadImg(Context context, String module_part, String original_file) {
        AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
        TokenEntry tokenEntry = mDb.tokenDao().getAllToken().get(0);
//        http://localhost:8888/Images.iSales/download.php?module_part=produit&original_file=cheese_cake/cheese_cake-Cheese_cake.jpg&DOLAPIKEY=9c524dc13288320153128086e6e69144fa743be3
        return String.format("%s?module_part=%s&original_file=%s&DOLAPIKEY=%s", BASE_URL_IMG, module_part, original_file, tokenEntry.getToken());
    }

}
