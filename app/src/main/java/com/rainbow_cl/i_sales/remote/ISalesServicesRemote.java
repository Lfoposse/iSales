package com.rainbow_cl.i_sales.remote;

import com.rainbow_cl.i_sales.remote.model.Categorie;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.model.Internaute;
import com.rainbow_cl.i_sales.remote.model.InternauteSuccess;
import com.rainbow_cl.i_sales.remote.model.Product;
import com.rainbow_cl.i_sales.remote.model.Thirdpartie;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by netserve on 03/08/2018.
 */

public interface ISalesServicesRemote {

//    COnnexion d'un internaute
    @POST("login")
    Call<InternauteSuccess> login(@Body Internaute internaute);

    //  Recupération de la liste des produits
    @GET("products")
    Call<ArrayList<Product>> findProducts(@Query(ApiUtils.sortfield) String sortfield,
                                           @Query(ApiUtils.sortorder) String sortorder,
                                           @Query(ApiUtils.limit) long limit,
                                           @Query(ApiUtils.page) long page,
                                           @Query(ApiUtils.category) long category,
                                           @Query(ApiUtils.mode) int mode);

    //  Recupération de la liste des thirdpartie(client, prospect, autre)
    @GET("thirdparties")
    Call<ArrayList<Thirdpartie>> findThirdpartie(@Query(ApiUtils.limit) long limit,
                                                 @Query(ApiUtils.page) long page,
                                                 @Query(ApiUtils.mode) int mode);

    //  Recupération de la liste des categories
    @GET("categories")
    Call<ArrayList<Categorie>> findCategories(@Query(ApiUtils.sortfield) String sortfield,
                                              @Query(ApiUtils.sortorder) String sortorder,
                                              @Query(ApiUtils.limit) long limit,
                                              @Query(ApiUtils.page) long page,
                                              @Query(ApiUtils.type) String type);

    //  Recupération du poster d'un produit
    @GET("documents/download")
    Call<DolPhoto> findProductsPoster(@Query(ApiUtils.module_part) String module_part,
                                      @Query(ApiUtils.original_file) String original_file);
    /*
    //  Getting the discover movies from the movie database
    @GET("movie/{sortOrder}")
    Call<Discover> discoverMovie(@Path("sortOrder") String sort_by,
                                 @Query(ApiUtils.page) int page,
                                 @Query(ApiUtils.language) String language);

    //  Getting the details of a movie from the movie database
    @GET("movie/{movieId}")
    Call<Movie> movieDetails(@Path("movieId") long movieId,
                             @Query(ApiUtils.language) String language);
    */

}
