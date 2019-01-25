package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.model.Thirdpartie;
import com.rainbow_cl.i_sales.remote.rest.FindThirdpartieREST;

/**
 * Created by netserve on 07/09/2018.
 */

public interface FindThirdpartieListener {
    void onFindThirdpartieCompleted(FindThirdpartieREST findThirdpartieREST);
    void onFindThirdpartieByIdCompleted(Thirdpartie thirdpartie);
}
