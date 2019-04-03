package com.iSales.remote.rest;

/**
 * Created by netserve on 30/08/2018.
 */

public class ISalesREST {
    protected int errorCode;
    protected String errorBody;

    public ISalesREST() {
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(String errorBody) {
        this.errorBody = errorBody;
    }
}
