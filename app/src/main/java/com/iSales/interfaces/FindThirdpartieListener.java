package com.iSales.interfaces;

import com.iSales.remote.model.Thirdpartie;
import com.iSales.remote.rest.FindThirdpartieREST;

/**
 * Created by netserve on 07/09/2018.
 */

public interface FindThirdpartieListener {
    void onFindThirdpartieCompleted(FindThirdpartieREST findThirdpartieREST);
    void onFindThirdpartieByIdCompleted(Thirdpartie thirdpartie);
}
