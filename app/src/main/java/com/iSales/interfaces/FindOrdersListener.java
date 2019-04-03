package com.iSales.interfaces;

import com.iSales.remote.rest.FindOrderLinesREST;
import com.iSales.remote.rest.FindOrdersREST;

/**
 * Created by netserve on 03/10/2018.
 */

public interface FindOrdersListener {
    void onFindOrdersTaskComplete(FindOrdersREST findOrdersREST);
    void onFindOrderLinesTaskComplete(long commande_ref, long commande_id, FindOrderLinesREST findOrderLinesREST);
}
