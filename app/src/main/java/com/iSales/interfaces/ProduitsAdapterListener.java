package com.iSales.interfaces;

import com.iSales.model.ProduitParcelable;

/**
 * Created by netserve on 30/08/2018.
 */

public interface ProduitsAdapterListener {
    void onDetailsSelected(ProduitParcelable produitParcelable);
    void onShoppingSelected(ProduitParcelable produitParcelable, int position);
}
