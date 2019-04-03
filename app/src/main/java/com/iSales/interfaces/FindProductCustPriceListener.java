package com.iSales.interfaces;

import com.iSales.remote.rest.FindProductCustomerPriceREST;

public interface FindProductCustPriceListener {
    void onFindProductCustPriceCompleted(FindProductCustomerPriceREST findProductCustomerPriceREST);
}
