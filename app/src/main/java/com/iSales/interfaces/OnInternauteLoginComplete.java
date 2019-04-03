package com.iSales.interfaces;

import com.iSales.remote.rest.LoginREST;

/**
 * Created by netserve on 27/08/2018.
 */

public interface OnInternauteLoginComplete {
    void onInternauteLoginTaskComplete(LoginREST loginREST);
}
