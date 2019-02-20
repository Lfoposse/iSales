package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.FindPaymentTypesREST;

/**
 * Created by netserve on 12/02/2019.
 */

public interface FindPaymentTypesListener {
    void onFindPaymentTypesComplete(FindPaymentTypesREST findPaymentTypesREST);
}
