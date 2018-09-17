package com.rainbow_cl.i_sales.remote;

/**
 * Created by netserve on 03/08/2018.
 */

public final class ApiUtils {
    public static final String BASE_URL = "http://dolibarr.bananafw.com/api/index.php/";

    //    Movies DB api key
    public static final String API_KEY = "9c524dc13288320153128086e6e69144fa743be3";

    //    body query properties
    public static final String DOLAPIKEY = "DOLAPIKEY";

    public static final String sortfield = "sortfield";
    public static final String sortorder = "sortorder";
    public static final String limit = "limit";
    public static final String page = "page";
    public static final String mode = "mode";
    public static final String category = "category";
    public static final String type = "type";
    public static final String module_part = "module_part";
    public static final String original_file = "original_file";

    //    Mode de recuperation des thirdpartie
    public static final int THIRDPARTIE_CLIENT = 1;

    private ApiUtils() {
    }

    //get an instance of Movies API services
    public static ISalesServicesRemote getISalesService() {
        return RetrofitClient.getClient(BASE_URL).create(ISalesServicesRemote.class);
    }

}
