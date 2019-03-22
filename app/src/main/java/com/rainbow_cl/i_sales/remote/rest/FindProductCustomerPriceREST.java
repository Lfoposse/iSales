package com.rainbow_cl.i_sales.remote.rest;

import com.rainbow_cl.i_sales.remote.model.ProductCustomerPrice;

import java.util.ArrayList;
import java.util.List;

public class FindProductCustomerPriceREST extends ISalesREST {
    private List<ProductCustomerPrice> productCustomerPrices;
    private long customer_id;

    public FindProductCustomerPriceREST() {
    }

    public FindProductCustomerPriceREST(ArrayList<ProductCustomerPrice> productCustomerPrices, long customer_id) {
        this.productCustomerPrices = productCustomerPrices;
        this.customer_id = customer_id;
    }

    public List<ProductCustomerPrice> getProductCustomerPrices() {
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
