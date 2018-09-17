package com.rainbow_cl.i_sales.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
 * Created by netserve on 29/08/2018.
 */

public class ProduitsAdapter extends RecyclerView.Adapter<ProduitsAdapter.ProduitsViewHolder> {
    private static final String TAG = ProduitsAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<ProduitParcelable> produitsList;
    private ArrayList<ProduitParcelable> produitsListFiltered;
    private ProduitsAdapterListener mListener;

    private FindPosterTask findPosterTask;
    //    ViewHolder de l'adapter
    public class ProduitsViewHolder extends RecyclerView.ViewHolder {
        public TextView label, price;
        public ImageView poster;
        public ImageButton shooping, details;

        public ProduitsViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.produit_item_label_tv);
            price = view.findViewById(R.id.produit_item_price_tv);
            poster = view.findViewById(R.id.produit_item_poster_iv);
            shooping = view.findViewById(R.id.produit_item_shopping_ib);
            details = view.findViewById(R.id.produit_item_details_ib);

            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    mListener.onDetailsSelected(produitsListFiltered.get(getAdapterPosition()));
                }
            });
            poster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    mListener.onDetailsSelected(produitsListFiltered.get(getAdapterPosition()));
                }
            });

//            ecoute du clique sur le bouton de shopping(ajout dans le panier)
            shooping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    mListener.onShoppingSelected(produitsListFiltered.get(getAdapterPosition()));
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


    public ProduitsAdapter(Context context, ArrayList<ProduitParcelable> produitParcelables, ProduitsAdapterListener listener) {
        this.mContext = context;
        this.produitsList = produitParcelables;
        this.mListener = listener;
        this.produitsListFiltered = produitParcelables;
    }

    @Override
    public ProduitsAdapter.ProduitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_produit, parent, false);

        return new ProduitsAdapter.ProduitsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProduitsAdapter.ProduitsViewHolder holder, int position) {

        holder.label.setText(produitsListFiltered.get(position).getLabel());
        holder.price.setText(produitsListFiltered.get(position).getPrice());

        if (produitsListFiltered.get(position).getPoster().getContent() == null) {
            Log.e(TAG, "onBindViewHolder: content=null");
//                    chargement de la photo dans la vue
//            holder.poster.setImageBitmap(mContext.getResources().getDrawable(R.drawable.logo_isales_small));
            holder.poster.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo_isales_small));

            if (findPosterTask == null) {
//        recuperation de la photo poster du produit
                findPosterTask = new FindPosterTask(produitsListFiltered.get(position), holder);
                findPosterTask.execute();
                findPosterTask = null;
            }
        } else {
            Log.e(TAG, "onBindViewHolder: content=not null");
            byte[] decodedString = Base64.decode(produitsListFiltered.get(position).getPoster().getContent(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//                    chargement de la photo dans la vue
            holder.poster.setImageBitmap(decodedByte);
        }
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

        public FindPosterTask(ProduitParcelable produitParcelable, ProduitsViewHolder holder) {
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

                    produitParcelable.setPoster(findDolPhotoREST.getDolPhoto());
//                    chargement de la photo dans la vue
                    holder.poster.setImageBitmap(decodedByte);
                }
            }
        }
    }
}
