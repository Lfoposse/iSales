package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.model.ClientParcelable;

/**
 * Created by netserve on 25/09/2018.
 */

public interface DialogClientListener {
    void onClientDialogSelected(ClientParcelable clientParcelable, int position);
}
