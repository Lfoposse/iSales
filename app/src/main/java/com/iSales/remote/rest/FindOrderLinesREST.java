package com.iSales.remote.rest;

import com.iSales.remote.model.OrderLine;
import com.iSales.remote.rest.ISalesREST;

import java.util.ArrayList;

/**
 * Created by netserve on 24/01/2019.
 */

public class FindOrderLinesREST extends ISalesREST {
    private ArrayList<OrderLine> lines;

    public FindOrderLinesREST() {
    }

    public FindOrderLinesREST(ArrayList<OrderLine> lines) {
        this.lines = lines;
    }

    public FindOrderLinesREST(int errorCode, String errorBody) {
        this.errorCode = errorCode;
        this.errorBody = errorBody;
    }

    public ArrayList<OrderLine> getLines() {
        return lines;
    }

    public void setLines(ArrayList<OrderLine> lines) {
        this.lines = lines;
    }
}
