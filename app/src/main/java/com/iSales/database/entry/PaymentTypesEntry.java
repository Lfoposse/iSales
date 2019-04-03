package com.iSales.database.entry;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by netserve on 12/02/2019.
 */

@Entity(tableName = "payment_types")
public class PaymentTypesEntry {
    @PrimaryKey(autoGenerate = true)
    private Long paymenttypes_id;
    private String id;
    private String code;
    private String type;
    private String label;

    public PaymentTypesEntry() {
    }

    public Long getPaymenttypes_id() {
        return paymenttypes_id;
    }

    public void setPaymenttypes_id(Long paymenttypes_id) {
        this.paymenttypes_id = paymenttypes_id;
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
