package com.iSales.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.iSales.R;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.PanierEntry;
import com.iSales.interfaces.PanierProduitAdapterListener;
import com.iSales.remote.ApiUtils;
import com.iSales.utility.ISalesUtility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by netserve on 10/09/2018.
 */

public class PanierProduitAdapter extends RecyclerView.Adapter<com.iSales.adapter.PanierProduitAdapter.PanierProduitsViewHolder> {
    private static final String TAG = com.iSales.adapter.PanierProduitAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<PanierEntry> panierList;
    private ArrayList<PanierEntry> panierListFiltered;
    private PanierProduitAdapterListener mListener;

    //    database instance
    private AppDatabase mDb;

    //    ViewHolder de l'adapter
    public class PanierProduitsViewHolder extends RecyclerView.ViewHolder {
        public TextView label, priceTTC, tva, total;
        public ImageView poster;
//        public EditText quantite;
        public ElegantNumberButton quantite;
        public ImageButton removeProduit;

        public PanierProduitsViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.tv_panier_produit_label);
            priceTTC = view.findViewById(R.id.tv_panier_produit_price_ttc);
            tva = view.findViewById(R.id.tv_panier_produit_tva);
            total = view.findViewById(R.id.tv_panier_produit_total);
            poster = view.findViewById(R.id.iv_panier_produit_poster);
//            quantite = view.findViewById(R.id.et_panier_quantite);
            quantite = view.findViewById(R.id.numbtn_panier_produit);
            removeProduit = view.findViewById(R.id.ib_panier_produit_delete);

//            fix image in view
            poster.setAdjustViewBounds(true);
            poster.setScaleType(ImageView.ScaleType.FIT_XY);
            quantite.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                @Override
                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
//                    Log.e(TAG, String.format("oldValue: %d   newValue: %d", oldValue, newValue));
//                    total.setText(String.format("%s %s",
//                            ISalesUtility.amountFormat2(""+Double.valueOf(panierListFiltered.get(getAdapterPosition()).getPrice_ttc())*newValue),
//                            mContext.getString(R.string.symbole_euro)));

                    if (panierListFiltered.get(getAdapterPosition()).getQuantity() != newValue) {
//                    mise a jour de la quantité du produit dans la liste
                        panierListFiltered.get(getAdapterPosition())
                                .setQuantity(newValue);

                        mListener.onChangeQuantityItemPanier(getAdapterPosition(), newValue);
                    }
                }
            });
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
        mDb = AppDatabase.getInstance(context.getApplicationContext());
    }

    @Override
    public com.iSales.adapter.PanierProduitAdapter.PanierProduitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_produit_panier, parent, false);

        return new com.iSales.adapter.PanierProduitAdapter.PanierProduitsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final com.iSales.adapter.PanierProduitAdapter.PanierProduitsViewHolder holder, final int position) {
        holder.label.setText(panierListFiltered.get(position).getLabel());
        holder.priceTTC.setText(String.format("%s %s TTC  •  %s %s HT", ISalesUtility.amountFormat2(panierListFiltered.get(position).getPrice_ttc()), mContext.getString(R.string.symbole_euro), ISalesUtility.amountFormat2(panierListFiltered.get(position).getPrice()), mContext.getString(R.string.symbole_euro)));
        holder.tva.setText(String.format("TVA %s %s  •  Remise %s %s",
                ISalesUtility.amountFormat2(panierListFiltered.get(position).getTva_tx()),
                "%",
                ISalesUtility.amountFormat2(panierListFiltered.get(position).getRemise_percent() == null || panierListFiltered.get(position).getRemise_percent().equals("") ? "0" : panierListFiltered.get(position).getRemise_percent()),
                "%"));
        holder.quantite.setNumber(String.valueOf(panierListFiltered.get(position).getQuantity()), true);
//        if (holder.quantite.getText().toString().equals("")) {
//            holder.quantite.setText(String.valueOf(panierListFiltered.get(position).getQuantity()));
//        }
        holder.total.setText(String.format("%s %s",
                ISalesUtility.amountFormat2("" + Double.valueOf(panierListFiltered.get(position).getPrice_ttc()) * panierListFiltered.get(position).getQuantity()),
                mContext.getString(R.string.symbole_euro)));

        /*
        holder.quantite.setFilters(new InputFilter[]{new InputFilterMinMax("1", "15")});
        holder.quantite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.e(TAG, "beforeTextChanged: charSequence=" + charSequence);
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
                    holder.quantite.setSelection(holder.quantite.getText().length());
                    return;
                }

                Log.e(TAG, "afterTextChanged: newValue=" + newValue+" id="+panierListFiltered.get(position).getId());

                mListener.onChangeQuantityItemPanier(position, newValue);
                panierListFiltered.get(position)
                        .setQuantity(newValue);
            }
        }); */

        if (panierListFiltered.get(position).getFile_content() != null) {
//            Log.e(TAG, "onBindViewHolder: getPoster_content"+panierListFiltered.get(position).getPoster_content());
//            si le fichier existe dans la memoire locale
            File imgFile = new File(panierListFiltered.get(position).getFile_content());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                holder.poster.setImageBitmap(myBitmap);
                /*Picasso.with(mContext)
                        .load(imgFile)
                        .placeholder(R.drawable.isales_no_image)
                        .into(holder.poster); */
                return;

            } else {
                holder.poster.setImageResource(R.drawable.isales_no_image);
                /*Picasso.with(mContext)
                        .load(R.drawable.isales_no_image)
                        .into(holder.poster);*/
//                holder.poster.setBackgroundResource(R.drawable.isales_no_image);
                return;
            }
        } else {

//        holder.poster.setBackgroundResource(R.drawable.isales_no_image);
//            String original_file = panierListFiltered.get(position).getRef() + "/" + panierListFiltered.get(position).getPoster_content();
//            String module_part = "produit";
//            Log.e(TAG, "onBindViewHolder:Panier downloadLinkImg=" + ApiUtils.getDownloadProductImg(mContext, panierListFiltered.get(position).getRef()));
            /*Picasso.with(mContext)
                    .load(ApiUtils.getDownloadProductImg(mContext, panierListFiltered.get(position).getRef()))
                    .placeholder(R.drawable.isales_img_loading)
                    .error(R.drawable.isales_no_image)
                    .into(holder.poster);*/
            return;
        }
    }

    @Override
    public int getItemCount() {
        if (panierListFiltered != null) {
            return panierListFiltered.size();
        }
        return 0;
    }

    public List<PanierEntry> getPanierItems() {
        return this.panierListFiltered;
    }
}
