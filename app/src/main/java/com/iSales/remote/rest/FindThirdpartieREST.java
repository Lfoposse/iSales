package com.iSales.remote.rest;

import com.iSales.remote.model.Thirdpartie;
import com.iSales.remote.rest.ISalesREST;

import java.util.ArrayList;

/**
 * Created by netserve on 07/09/2018.
 */

public class FindThirdpartieREST extends ISalesREST {
    private ArrayList<Thirdpartie> thirdparties;

    public FindThirdpartieREST() {
    }

    public FindThirdpartieREST(ArrayList<Thirdpartie> thirdparties) {
        this.thirdparties = thirdparties;
    }

    public FindThirdpartieREST(int errorCode, String errorBody) {
        this.errorCode = errorCode;
        this.errorBody = errorBody;
    }

    public ArrayList<Thirdpartie> getThirdparties() {
        return thirdparties;
    }

    public void setThirdparties(ArrayList<Thirdpartie> thirdparties) {
        this.thirdparties = thirdparties;
    }
}
