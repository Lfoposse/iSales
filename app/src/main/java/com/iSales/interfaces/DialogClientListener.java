package com.iSales.interfaces;

import com.iSales.model.ClientParcelable;

/**
 * Created by netserve on 25/09/2018.
 */

public interface DialogClientListener {
    void onClientDialogSelected(ClientParcelable clientParcelable, int position);
}
