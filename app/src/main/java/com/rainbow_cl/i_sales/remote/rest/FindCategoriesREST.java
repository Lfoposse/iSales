package com.rainbow_cl.i_sales.remote.rest;

import com.rainbow_cl.i_sales.remote.model.Categorie;

import java.util.ArrayList;

/**
 * Created by netserve on 05/09/2018.
 */

public class FindCategoriesREST extends ISalesREST {
    private ArrayList<Categorie> categories;

    public FindCategoriesREST() {
    }

    public FindCategoriesREST(ArrayList<Categorie> categories) {
        this.categories = categories;
    }

    public FindCategoriesREST(int errorCode, String errorBody) {
        this.errorCode = errorCode;
        this.errorBody = errorBody;
    }

    public ArrayList<Categorie> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Categorie> categories) {
        this.categories = categories;
    }
}
