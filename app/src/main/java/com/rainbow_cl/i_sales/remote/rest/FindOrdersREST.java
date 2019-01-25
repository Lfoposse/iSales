package com.rainbow_cl.i_sales.remote.rest;

import com.rainbow_cl.i_sales.remote.model.Order;

import java.util.ArrayList;

/**
 * Created by netserve on 03/10/2018.
 */

public class FindOrdersREST extends ISalesREST {
    private ArrayList<Order> orders;

    public FindOrdersREST() {
    }

    public FindOrdersREST(ArrayList<Order> products) {
        this.orders = products;
    }

    public FindOrdersREST(int errorCode, String errorBody) {
        this.errorCode = errorCode;
        this.errorBody = errorBody;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }
}
