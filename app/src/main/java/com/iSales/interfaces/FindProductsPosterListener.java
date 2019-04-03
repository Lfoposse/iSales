package com.iSales.interfaces;

import com.iSales.remote.rest.FindDolPhotoREST;

/**
 * Created by netserve on 30/08/2018.
 */

public interface FindProductsPosterListener {
    void onFindProductsPosterComplete(FindDolPhotoREST findDolPhotoREST, int productPosition);
}
