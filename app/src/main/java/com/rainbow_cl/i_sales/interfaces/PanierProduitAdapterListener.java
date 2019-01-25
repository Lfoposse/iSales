package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.database.entry.PanierEntry;

/**
 * Created by netserve on 24/09/2018.
 */

public interface PanierProduitAdapterListener {
    void onRemoveItemPanier(PanierEntry panierEntry, int position);
    void onChangeQuantityItemPanier(int position, int quantity);
}
