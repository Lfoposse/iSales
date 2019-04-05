package com.iSales.interfaces;

import com.iSales.remote.rest.FindProductsREST;

/**
 * Created by netserve on 29/08/2018.
 */

public interface FindProductsListener {
    void onFindProductsCompleted(FindProductsREST findProductsREST);
    void onFindAllProductsCompleted();
}
