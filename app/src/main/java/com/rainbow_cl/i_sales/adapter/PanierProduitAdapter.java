package com.rainbow_cl.i_sales.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.interfaces.PanierProduitAdapterListener;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.utility.ISalesUtility;
import com.rainbow_cl.i_sales.utility.InputFilterMinMax;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by netserve on 10/09/2018.
 */

public class PanierProduitAdapter extends RecyclerView.Adapter<PanierProduitAdapter.PanierProduitsViewHolder> {
    private static final String TAG = PanierProduitAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<PanierEntry> panierList;
    private ArrayList<PanierEntry> panierListFiltered;
    private PanierProduitAdapterListener mListener;

    //    ViewHolder de l'adapter
    public class PanierProduitsViewHolder extends RecyclerView.ViewHolder {
        public TextView label, priceTTC, tva, total;
        public ImageView poster;
        public EditText numberButton;
        public ImageButton removeProduit;

        public PanierProduitsViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.tv_panier_produit_label);
            priceTTC = view.findViewById(R.id.tv_panier_produit_price_ttc);
            tva = view.findViewById(R.id.tv_panier_produit_tva);
            total = view.findViewById(R.id.tv_panier_produit_total);
            poster = view.findViewById(R.id.iv_panier_produit_poster);
            numberButton = view.findViewById(R.id.et_panier_quantite);
            removeProduit = view.findViewById(R.id.ib_panier_produit_delete);

            /*
            numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                @Override
                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
//                    Log.e(TAG, String.format("oldValue: %d   newValue: %d", oldValue, newValue));
                    total.setText(String.format("%s %s",
                            ISalesUtility.amountFormat2(""+Double.valueOf(panierListFiltered.get(getAdapterPosition()).getPrice_ttc())*newValue),
                            mContext.getString(R.string.symbole_euro)));

//                    mise a jour de la quantité du produit dans la liste
                    panierListFiltered.get(getAdapterPosition())
                            .setQuantity(newValue);

                    mListener.onChangeQuantityItemPanier(getAdapterPosition(), newValue);
                }
            }); */
            removeProduit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onRemoveItemPanier(panierListFiltered.get(getAdapterPosition()), getAdapterPosition());
                }
            });

        }
    }

    //    Filtre la liste des produits
    public void performFiltering(String searchString) {
        if (searchString.isEmpty()) {
            panierListFiltered = panierList;
        } else {
            ArrayList<PanierEntry> filteredList = new ArrayList<>();
            for (PanierEntry row : panierList) {

                // name match condition. this might differ depending on your requirement
                // here we are looking for name or phone number match
                if (row.getLabel().toLowerCase().contains(searchString.toLowerCase())
                        || row.getPrice().toLowerCase().contains(searchString.toLowerCase())) {
                    filteredList.add(row);
                }
            }

            panierListFiltered = filteredList;
        }

        notifyDataSetChanged();
    }


    public PanierProduitAdapter(Context context, ArrayList<PanierEntry> panierEntries, PanierProduitAdapterListener listener) {
        this.mContext = context;
        this.panierList = panierEntries;
        this.panierListFiltered = panierEntries;
        this.mListener = listener;
    }

    @Override
    public PanierProduitAdapter.PanierProduitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_produit_panier, parent, false);

        return new PanierProduitAdapter.PanierProduitsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PanierProduitAdapter.PanierProduitsViewHolder holder, final int position) {
        final PanierEntry panierEntry = panierListFiltered.get(position);
        holder.label.setText(panierEntry.getLabel());
        holder.priceTTC.setText(String.format("%s %s TTC  •  %s %s HT", ISalesUtility.amountFormat2(panierEntry.getPrice_ttc()), mContext.getString(R.string.symbole_euro), ISalesUtility.amountFormat2(panierEntry.getPrice()), mContext.getString(R.string.symbole_euro)));
        holder.tva.setText(String.format("TVA : %s %s", ISalesUtility.amountFormat2(panierEntry.getTva_tx()), "%"));
//        holder.numberButton.setNumber(String.valueOf(panierEntry.getQuantity()), true);
        holder.numberButton.setText(String.valueOf(panierEntry.getQuantity()));
        holder.total.setText(String.format("%s %s",
                ISalesUtility.amountFormat2("" + Double.valueOf(panierEntry.getPrice_ttc()) * panierEntry.getQuantity()),
                mContext.getString(R.string.symbole_euro)));

        holder.numberButton.setFilters(new InputFilter[]{new InputFilterMinMax("1", "15")});
        holder.numberButton.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e(TAG, "beforeTextChanged: charSequence=" + charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                Log.e(TAG, "afterTextChanged: editable=" + editable.toString());

                if (editable.toString().equals("")) {
//                    Log.e(TAG, "afterTextChanged: editable empty");
                    return;
                }

                int newValue = Integer.parseInt(editable.toString());
//                teste s'il s'agit d'un quantité differente
                if (panierListFiltered.get(position).getQuantity() == newValue) {
//                    Log.e(TAG, "afterTextChanged: same quantity");
                    holder.numberButton.setSelection(holder.numberButton.getText().length());
                    return;
                }

//                Log.e(TAG, "afterTextChanged: newValue=" + newValue);

//                    mise a jour de la quantité du produit dans la liste
                panierListFiltered.get(position)
                        .setQuantity(newValue);

                holder.total.setText(String.format("%s %s",
                        ISalesUtility.amountFormat2("" + Double.valueOf(panierListFiltered.get(position).getPrice_ttc()) * newValue),
                        mContext.getString(R.string.symbole_euro)));

                mListener.onChangeQuantityItemPanier(position, newValue);

                holder.numberButton.setSelection(holder.numberButton.getText().length());
            }
        });

        if (panierEntry.getFile_content() != null) {
            Log.e(TAG, "onBindViewHolder: getPoster_content"+panierEntry.getFile_content());
//            si le fichier existe dans la memoire locale
            File imgFile = new File(panierEntry.getFile_content());
            if (imgFile.exists()) {
                Picasso.with(mContext)
                        .load(imgFile)
                        .into(holder.poster);
                return;

            } else {
                Picasso.with(mContext)
                        .load(R.drawable.isales_no_image)
                        .into(holder.poster);
//                holder.poster.setBackgroundResource(R.drawable.isales_no_image);
            }
        } else {
//            holder.poster.setBackgroundResource(R.drawable.isales_no_image);
            Picasso.with(mContext)
                    .load(R.drawable.isales_no_image)
                    .into(holder.poster);
        }

//        holder.poster.setBackgroundResource(R.drawable.isales_no_image);
        String original_file = panierEntry.getRef() + "/" + panierEntry.getPoster_content();
        String module_part = "produit";
        Log.e(TAG, "onBindViewHolder: downloadLinkImg=" + ApiUtils.getDownloadImg(mContext, module_part, original_file));
        Picasso.with(mContext)
                .load(ApiUtils.getDownloadImg(mContext, module_part, original_file))
                .placeholder(R.drawable.isales_no_image)
                .error(R.drawable.isales_no_image)
                .into(holder.poster);
    }

    @Override
    public int getItemCount() {
        if (panierListFiltered != null) {
            return panierListFiltered.size();
        }
        return 0;
    }
}
