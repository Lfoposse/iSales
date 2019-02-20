package com.rainbow_cl.i_sales.remote.rest;

import com.rainbow_cl.i_sales.remote.model.PaymentTypes;

import java.util.ArrayList;

/**
 * Created by netserve on 12/02/2019.
 */

public class FindPaymentTypesREST extends ISalesREST {
    private ArrayList<PaymentTypes> paymentTypes;

    public FindPaymentTypesREST(ArrayList<PaymentTypes> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    public FindPaymentTypesREST() {
    }

    public ArrayList<PaymentTypes> getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(ArrayList<PaymentTypes> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }
}
