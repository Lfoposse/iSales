package com.rainbow_cl.i_sales.interfaces;

import com.rainbow_cl.i_sales.remote.model.ProductVirtual;

public interface ProductVirtualAdapterListener {
    void onProductVirtualClicked(ProductVirtual productVirtual, int position);

}
