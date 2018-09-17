package com.rainbow_cl.i_sales.remote;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by netserve on 03/08/2018.
 */

public class RetrofitClient {

    public static final String TAG = RetrofitClient.class.getSimpleName();
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String url) {
        if (retrofit == null) {

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            HttpUrl originalHttpUrl = original.url();

//                            Log.e(TAG, "intercept:Before "+originalHttpUrl.toString());

//                            if it is login query, don't add dolapikey
                            if (originalHttpUrl.toString().contains("login")) {
                                return chain.proceed(original);
                            }

//                            Adding DOLIBARR API KEY to all queries
                            HttpUrl url = originalHttpUrl.newBuilder()
                                    .addQueryParameter(ApiUtils.DOLAPIKEY, ApiUtils.API_KEY)
                                    .build();
                            Log.e(TAG, "intercept:After url= "+url.toString());

                            // Request customization: add request headers
                            Request.Builder requestBuilder = original.newBuilder()
                                    .url(url);

                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url)
                    .build();
        }
        return retrofit;
    }
}