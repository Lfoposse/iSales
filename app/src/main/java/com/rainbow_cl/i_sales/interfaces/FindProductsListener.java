package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.FindProductsREST;

/**
 * Created by netserve on 29/08/2018.
 */

public interface FindProductsListener {
    void onFindProductsCompleted(FindProductsREST findProductsREST);
}
