package com.iSales.remote.model;

/**
 * Created by netserve on 12/02/2019.
 */

public class PaymentTypes {
    public String id;
    public String code;
    public String type;
    public String label;

    public PaymentTypes() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
