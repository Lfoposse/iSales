package com.iSales.remote.rest;

import com.iSales.remote.model.ProductCustomerPrice;

import java.util.ArrayList;
import java.util.List;

public class FindProductCustomerPriceREST extends ISalesREST {
    private List<com.iSales.remote.model.ProductCustomerPrice> productCustomerPrices;
    private long customer_id;

    public FindProductCustomerPriceREST() {
    }

    public FindProductCustomerPriceREST(ArrayList<com.iSales.remote.model.ProductCustomerPrice> productCustomerPrices, long customer_id) {
        this.productCustomerPrices = productCustomerPrices;
        this.customer_id = customer_id;
    }

    public List<com.iSales.remote.model.ProductCustomerPrice> getProductCustomerPrices() {
        return productCustomerPrices;
    }

    public void setProductCustomerPrices(List<ProductCustomerPrice> productCustomerPrices) {
        this.productCustomerPrices = productCustomerPrices;
    }

    public long getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(long customer_id) {
        this.customer_id = customer_id;
    }
}
