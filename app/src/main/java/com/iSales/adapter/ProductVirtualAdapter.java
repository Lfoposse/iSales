package com.iSales.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iSales.R;
import com.iSales.interfaces.ProductVirtualAdapterListener;
import com.iSales.remote.model.ProductVirtual;
import com.iSales.utility.ISalesUtility;

import java.util.List;

public class ProductVirtualAdapter extends RecyclerView.Adapter<com.iSales.adapter.ProductVirtualAdapter.ProduitsVirtualViewHolder> {
    private static final String TAG = com.iSales.adapter.ProductVirtualAdapter.class.getSimpleName();

    private Context mContext;
    private List<ProductVirtual> productVirtuals;
    private ProductVirtualAdapterListener mListener;

    //    ViewHolder de l'adapter
    public class ProduitsVirtualViewHolder extends RecyclerView.ViewHolder {
        public TextView label, priceHT, priceTTC;
        public ImageView poster;
        public View itemView;
//        public ImageButton details;

        public ProduitsVirtualViewHolder(View view) {
            super(view);
            itemView = view.findViewById(R.id.view_productvirtual);
            label = view.findViewById(R.id.tv_productvirtual_label);
            priceHT = view.findViewById(R.id.tv_productvirtual_prix_ht);
            priceTTC = view.findViewById(R.id.tv_productvirtual_prix_ttc);

//            ecoute du clique sur le bouton de shopping(ajout dans le panier)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    mListener.onProductVirtualClicked(productVirtuals.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }


    public ProductVirtualAdapter(Context context, List<ProductVirtual> produitsList, ProductVirtualAdapterListener listener) {
        this.mContext = context;
        this.productVirtuals = produitsList;
        this.mListener = listener;
    }

    @Override
    public com.iSales.adapter.ProductVirtualAdapter.ProduitsVirtualViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_productvirtual, parent, false);

        return new com.iSales.adapter.ProductVirtualAdapter.ProduitsVirtualViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final com.iSales.adapter.ProductVirtualAdapter.ProduitsVirtualViewHolder holder, final int position) {
        Log.e(TAG, "onBindViewHolder: categorieId="+productVirtuals.get(position).getFk_product_pere()+
                " label="+productVirtuals.get(position).getLabel()+
                " id="+productVirtuals.get(position).getRowid()+
                " price="+productVirtuals.get(position).getPrice()+
                " qty="+productVirtuals.get(position).getQty());

        String[] label = productVirtuals.get(position).getLabel().split(" ");

        holder.label.setText(label[label.length-1]+" x "+productVirtuals.get(position).getQty());
        holder.priceHT.setText(String.format("%s %s HT",
                ISalesUtility.amountFormat2(productVirtuals.get(position).getPrice()),
                ISalesUtility.CURRENCY));
        holder.priceTTC.setText(String.format("%s %s TTC",
                ISalesUtility.amountFormat2(productVirtuals.get(position).getPrice_ttc()),
                ISalesUtility.CURRENCY));
    }

    @Override
    public int getItemCount() {
        if (productVirtuals != null) {
            return productVirtuals.size();
        }
        return 0;
    }
}
