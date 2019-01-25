package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.model.ProduitParcelable;

/**
 * Created by netserve on 30/08/2018.
 */

public interface ProduitsAdapterListener {
    void onDetailsSelected(ProduitParcelable produitParcelable);
    void onShoppingSelected(ProduitParcelable produitParcelable, int position);
}
