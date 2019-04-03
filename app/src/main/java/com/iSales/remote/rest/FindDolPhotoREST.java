package com.iSales.remote.rest;

import com.iSales.remote.model.DolPhoto;
import com.iSales.remote.rest.ISalesREST;

/**
 * Created by netserve on 30/08/2018.
 */

public class FindDolPhotoREST extends ISalesREST {
    private DolPhoto dolPhoto;

    public FindDolPhotoREST() {
    }

    public FindDolPhotoREST(DolPhoto dolPhoto) {
        this.dolPhoto = dolPhoto;
    }
    public FindDolPhotoREST(int errorCode, String errorBody) {
        this.errorCode = errorCode;
        this.errorBody = errorBody;
    }

    public DolPhoto getDolPhoto() {
        return dolPhoto;
    }

    public void setDolPhoto(DolPhoto dolPhoto) {
        this.dolPhoto = dolPhoto;
    }
}
