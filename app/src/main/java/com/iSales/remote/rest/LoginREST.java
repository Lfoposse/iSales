package com.iSales.remote.rest;

import com.iSales.remote.model.InternauteSuccess;
import com.iSales.remote.rest.ISalesREST;

/**
 * Created by netserve on 28/08/2018.
 */

public class LoginREST extends ISalesREST {
    private InternauteSuccess internauteSuccess;

    public LoginREST() {
    }

    public LoginREST(InternauteSuccess internauteSuccess) {
        this.internauteSuccess = internauteSuccess;
    }

    public LoginREST(int errorCode, String errorBody) {
        this.errorCode = errorCode;
        this.errorBody = errorBody;
    }

    public InternauteSuccess getInternauteSuccess() {
        return internauteSuccess;
    }

    public void setInternauteSuccess(InternauteSuccess internauteSuccess) {
        this.internauteSuccess = internauteSuccess;
    }

}
