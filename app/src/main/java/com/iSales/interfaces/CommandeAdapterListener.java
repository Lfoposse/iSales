package com.iSales.interfaces;

import com.iSales.model.CommandeParcelable;

/**
 * Created by netserve on 15/09/2018.
 */

public interface CommandeAdapterListener {
    void onCommandeSelected(CommandeParcelable commandeParcelable);
    void onCommandeReStarted(CommandeParcelable commandeParcelable);
}
