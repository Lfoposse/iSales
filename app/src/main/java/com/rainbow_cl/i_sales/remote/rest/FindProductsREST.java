package com.rainbow_cl.i_sales.remote.rest;

import com.rainbow_cl.i_sales.remote.model.Product;

import java.util.ArrayList;

/**
 * Created by netserve on 29/08/2018.
 */

public class FindProductsREST extends ISalesREST {
    private ArrayList<Product> products;

    public FindProductsREST() {
    }

    public FindProductsREST(ArrayList<Product> products) {
        this.products = products;
    }

    public FindProductsREST(int errorCode, String errorBody) {
        this.errorCode = errorCode;
        this.errorBody = errorBody;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

}
