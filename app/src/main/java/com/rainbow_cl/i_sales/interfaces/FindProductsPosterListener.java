package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.FindDolPhotoREST;

/**
 * Created by netserve on 30/08/2018.
 */

public interface FindProductsPosterListener {
    void onFindProductsPosterComplete(FindDolPhotoREST findDolPhotoREST, int productPosition);
}
