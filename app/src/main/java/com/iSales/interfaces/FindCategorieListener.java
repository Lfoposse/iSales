package com.iSales.interfaces;

import com.iSales.remote.rest.FindCategoriesREST;

/**
 * Created by netserve on 05/09/2018.
 */

public interface FindCategorieListener {
    void onFindCategorieCompleted(FindCategoriesREST findCategoriesREST);
}
