package com.rainbow_cl.i_sales.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.interfaces.ProduitsAdapterListener;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindDolPhotoREST;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 10/09/2018.
 */

public class PanierProduitAdapter extends RecyclerView.Adapter<PanierProduitAdapter.PanierProduitsViewHolder> {
    private static final String TAG = ProduitsAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<ProduitParcelable> produitsList;
    private ArrayList<ProduitParcelable> produitsListFiltered;

    //    ViewHolder de l'adapter
    public class PanierProduitsViewHolder extends RecyclerView.ViewHolder {
        public TextView label, price;
        public ImageView poster;
        public ElegantNumberButton numberButton;

        public PanierProduitsViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.tv_panier_produit_label);
            price = view.findViewById(R.id.tv_panier_produit_price);
            poster = view.findViewById(R.id.iv_panier_produit_poster);
            numberButton = view.findViewById(R.id.numbtn_panier_produit);

            numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                @Override
                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
//                    Log.e(TAG, String.format("oldValue: %d   newValue: %d", oldValue, newValue));
                }
            });

        }
    }

    //    Filtre la liste des produits
    public void performFiltering(String searchString) {
        if (searchString.isEmpty()) {
            produitsListFiltered = produitsList;
        } else {
            ArrayList<ProduitParcelable> filteredList = new ArrayList<>();
            for (ProduitParcelable row : produitsList) {

                // name match condition. this might differ depending on your requirement
                // here we are looking for name or phone number match
                if (row.getLabel().toLowerCase().contains(searchString.toLowerCase())
                        || row.getPrice().toLowerCase().contains(searchString.toLowerCase())) {
                    filteredList.add(row);
                }
            }

            produitsListFiltered = filteredList;
        }

        notifyDataSetChanged();
    }


    public PanierProduitAdapter(Context context, ArrayList<ProduitParcelable> produitParcelables) {
        this.mContext = context;
        this.produitsList = produitParcelables;
        this.produitsListFiltered = produitParcelables;
    }

    @Override
    public PanierProduitAdapter.PanierProduitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_produit_panier, parent, false);

        return new PanierProduitAdapter.PanierProduitsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PanierProduitAdapter.PanierProduitsViewHolder holder, int position) {
        final ProduitParcelable produitParcelable = produitsListFiltered.get(position);
        holder.label.setText(produitParcelable.getLabel());
        holder.price.setText(String.format("%s %s", produitParcelable.getPrice(), mContext.getString(R.string.symbole_euro)));

//        recuperation de la photo poster du produit
//        ProduitsAdapter.FindPosterTask findPosterTask = new ProduitsAdapter.FindPosterTask(produitParcelable, holder);
//        findPosterTask.execute();
    }

    @Override
    public int getItemCount() {
        if (produitsListFiltered != null) {
            return produitsListFiltered.size();
        }
        return 0;
    }

    private class FindPosterTask extends AsyncTask<Void, Void, FindDolPhotoREST> {
        private ProduitParcelable produitParcelable;
        private ProduitsAdapter.ProduitsViewHolder holder;

        public FindPosterTask(ProduitParcelable produitParcelable, ProduitsAdapter.ProduitsViewHolder holder) {
            this.produitParcelable = produitParcelable;
            this.holder = holder;
        }

        @Override
        protected FindDolPhotoREST doInBackground(Void... voids) {
            String original_file = produitParcelable.getRef() + "/" + produitParcelable.getPoster().getFilename();
            String module_part = "produit";

//        Requete de connexion de l'internaute sur le serveur
            Call<DolPhoto> call = ApiUtils.getISalesService().findProductsPoster(module_part, original_file);
            try {
                Response<DolPhoto> response = call.execute();
                if (response.isSuccessful()) {
                    DolPhoto dolPhoto = response.body();
//                    Log.e(TAG, "doInBackground: dolPhoto | Filename=" + dolPhoto.getFilename() + " content=" + dolPhoto.getContent());
                    return new FindDolPhotoREST(dolPhoto);
                } else {
                    String error = null;
                    FindDolPhotoREST findDolPhotoREST = new FindDolPhotoREST();
                    findDolPhotoREST.setDolPhoto(null);
                    findDolPhotoREST.setErrorCode(response.code());
                    try {
                        error = response.errorBody().string();
//                    JSONObject jsonObjectError = new JSONObject(error);
//                    String errorCode = jsonObjectError.getString("errorCode");
//                    String errorDetails = jsonObjectError.getString("errorDetails");
                        Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
                        findDolPhotoREST.setErrorBody(error);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return findDolPhotoREST;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(FindDolPhotoREST findDolPhotoREST) {
//        super.onPostExecute(findDolPhotoREST);
            if (findDolPhotoREST != null) {
                if (findDolPhotoREST.getDolPhoto() != null) {
//                    Log.e(TAG, "onPostExecute: dolPhoto | Filename=" + findDolPhotoREST.getDolPhoto().getFilename() + " content=" + findDolPhotoREST.getDolPhoto().getContent());

//                    conversion de la photo du Base64 en bitmap
                    byte[] decodedString = Base64.decode(findDolPhotoREST.getDolPhoto().getContent(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//                    chargement de la photo dans la vue
                    holder.poster.setImageBitmap(decodedByte);
                }
            }
        }
    }
}
