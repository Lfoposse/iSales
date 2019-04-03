package com.iSales.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iSales.R;
import com.iSales.database.entry.PanierEntry;
import com.iSales.utility.ISalesUtility;

import java.util.ArrayList;

/**
 * Created by netserve on 01/10/2018.
 */

public class RecapPanierAdapter extends RecyclerView.Adapter<com.iSales.adapter.RecapPanierAdapter.RecapPanierViewHolder> {
    private static final String TAG = com.iSales.adapter.RecapPanierAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<PanierEntry> panierList;

    //    ViewHolder de l'adapter
    public class RecapPanierViewHolder extends RecyclerView.ViewHolder {
        public TextView label, price, quantite;

        public RecapPanierViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.tv_boncmde_produit_label);
            price = view.findViewById(R.id.tv_boncmde_produit_price);
            quantite = view.findViewById(R.id.tv_boncmde_produit_quantite);

        }
    }


    public RecapPanierAdapter(Context context, ArrayList<PanierEntry> panierEntries) {
        this.mContext = context;
        this.panierList = panierEntries;
    }

    @Override
    public com.iSales.adapter.RecapPanierAdapter.RecapPanierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_boncmde_produit, parent, false);

        return new com.iSales.adapter.RecapPanierAdapter.RecapPanierViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final com.iSales.adapter.RecapPanierAdapter.RecapPanierViewHolder holder, int position) {
        final PanierEntry panierEntry = panierList.get(position);
//        Log.e(TAG, "onBindViewHolder: remise="+ panierEntry.getRemise()+" remise_per="+panierEntry.getRemise_percent());
        holder.label.setText(panierEntry.getLabel());
        holder.price.setText(String.format("%s %s", ISalesUtility.amountFormat2(panierEntry.getPrice_ttc()), mContext.getString(R.string.symbole_euro)));
        holder.quantite.setText(String.format("%d ", panierEntry.getQuantity()));
    }

    @Override
    public int getItemCount() {
        if (panierList != null) {
            return panierList.size();
        }
        return 0;
    }
}
