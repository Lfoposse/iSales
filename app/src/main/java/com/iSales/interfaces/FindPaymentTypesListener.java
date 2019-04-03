package com.iSales.interfaces;

import com.iSales.remote.rest.FindPaymentTypesREST;

/**
 * Created by netserve on 12/02/2019.
 */

public interface FindPaymentTypesListener {
    void onFindPaymentTypesComplete(FindPaymentTypesREST findPaymentTypesREST);
}
