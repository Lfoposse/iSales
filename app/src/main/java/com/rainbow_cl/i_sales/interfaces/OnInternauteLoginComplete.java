package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.LoginREST;

/**
 * Created by netserve on 27/08/2018.
 */

public interface OnInternauteLoginComplete {
    void onInternauteLoginTaskComplete(LoginREST loginREST);
}
