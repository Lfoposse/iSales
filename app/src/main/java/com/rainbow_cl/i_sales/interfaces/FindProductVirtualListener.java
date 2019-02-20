package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.FindProductVirtualREST;
import com.rainbow_cl.i_sales.remote.rest.FindProductsREST;

public interface FindProductVirtualListener {
    void onFindProductVirtualCompleted(FindProductVirtualREST findProductVirtualREST);
}
