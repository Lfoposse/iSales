package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.model.CommandeParcelable;

/**
 * Created by netserve on 15/09/2018.
 */

public interface CommandeAdapterListener {
    void onCommandeSelected(CommandeParcelable commandeParcelable);
}
