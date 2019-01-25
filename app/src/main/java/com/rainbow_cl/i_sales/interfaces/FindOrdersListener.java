package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.FindOrderLinesREST;
import com.rainbow_cl.i_sales.remote.rest.FindOrdersREST;

/**
 * Created by netserve on 03/10/2018.
 */

public interface FindOrdersListener {
    void onFindOrdersTaskComplete(FindOrdersREST findOrdersREST);
    void onFindOrderLinesTaskComplete(long commande_ref, long commande_id, FindOrderLinesREST findOrderLinesREST);
}
