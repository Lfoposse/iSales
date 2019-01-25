package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.rest.FindDolPhotoREST;

/**
 * Created by netserve on 10/10/2018.
 */

public interface FindDocumentListener {
    void onFindDocumentComplete(FindDolPhotoREST findDolPhotoREST);
}
