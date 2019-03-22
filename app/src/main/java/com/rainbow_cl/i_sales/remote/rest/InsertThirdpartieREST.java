package com.rainbow_cl.i_sales.remote.rest;

import com.rainbow_cl.i_sales.remote.model.Thirdpartie;

import java.util.ArrayList;

public class InsertThirdpartieREST extends ISalesREST {
    private Long thirdpartie_id;

    public InsertThirdpartieREST() {
    }

    public Long getThirdpartie_id() {
        return thirdpartie_id;
    }

    public void setThirdpartie_id(Long thirdpartie_id) {
        this.thirdpartie_id = thirdpartie_id;
    }
}
