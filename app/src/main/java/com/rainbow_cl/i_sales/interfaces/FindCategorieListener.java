package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.FindCategoriesREST;

/**
 * Created by netserve on 05/09/2018.
 */

public interface FindCategorieListener {
    void onFindCategorieCompleted(FindCategoriesREST findCategoriesREST);
}
