package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.FindProductCustomerPriceREST;

public interface FindProductCustPriceListener {
    void onFindProductCustPriceCompleted(FindProductCustomerPriceREST findProductCustomerPriceREST);
}
