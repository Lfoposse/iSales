package com.iSales.interfaces;

import com.iSales.model.ClientParcelable;

/**
 * Created by netserve on 28/08/2018.
 */

public interface ClientsAdapterListener {
    void onClientsSelected(ClientParcelable clientParcelable, int position);
    void onClientsUpdated(ClientParcelable clientParcelable, int position);
}
