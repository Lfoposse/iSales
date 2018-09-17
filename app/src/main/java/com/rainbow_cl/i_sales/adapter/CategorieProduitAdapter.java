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
import android.widget.ImageView;
import android.widget.TextView;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.interfaces.CategorieProduitAdapterListener;
import com.rainbow_cl.i_sales.model.CategorieParcelable;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindDolPhotoREST;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 05/09/2018.
 */

public class CategorieProduitAdapter extends RecyclerView.Adapter<CategorieProduitAdapter.CategorieProduitViewHolder> {

    private static final String TAG = CategorieProduitAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<CategorieParcelable> categorieList;
    private ArrayList<CategorieParcelable> categorieListFiltered;
    private CategorieProduitAdapterListener mListener;

    private FindCategoriePosterTask mTask;

    //    ViewHolder de l'adapter
    public class CategorieProduitViewHolder extends RecyclerView.ViewHolder {
        public TextView label;
        public ImageView poster;

        public CategorieProduitViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.tv_label_catpdt);
            poster = view.findViewById(R.id.iv_poster_catpdt);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    if (mTask == null) {
                        mListener.onCategorieSelected(categorieListFiltered.get(getAdapterPosition()));
                    }
                }
            });
        }
    }


    public CategorieProduitAdapter(Context context, ArrayList<CategorieParcelable> categorieParcelables, CategorieProduitAdapterListener listener) {
        this.mContext = context;
        this.categorieList = categorieParcelables;
        this.mListener = listener;
        this.categorieListFiltered = categorieParcelables;
    }

    @Override
    public CategorieProduitAdapter.CategorieProduitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_categorie_produit, parent, false);

        return new CategorieProduitAdapter.CategorieProduitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorieProduitAdapter.CategorieProduitViewHolder holder, int position) {
        holder.label.setText(categorieListFiltered.get(position).getLabel().toUpperCase());

//        Si l'image existe dejá on ne le recupére plus
        if (categorieListFiltered.get(position).getPoster().getContent() == null) {
            Log.e(TAG, "onBindViewHolder: content=null");
//                    chargement de la photo dans la vue
//            holder.poster.setImageBitmap(mContext.getResources().getDrawable(R.drawable.logo_isales_small));
            holder.poster.setBackground(mContext.getResources().getDrawable(R.drawable.logo_isales_small));

//        recuperation de la photo poster du produit
            if (mTask == null) {
                mTask = new FindCategoriePosterTask(categorieListFiltered.get(position), holder);
                mTask.execute();
                mTask = null;
            }
        } else {
            Log.e(TAG, "onBindViewHolder: content=not null");
            byte[] decodedString = Base64.decode(categorieListFiltered.get(position).getPoster().getContent(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//            chargement de la photo dans la vue
            holder.poster.setBackground(new BitmapDrawable(decodedByte));
        }
    }

    @Override
    public int getItemCount() {
        return categorieListFiltered.size();
    }

    //    Filtre la liste des clients
    public void performFiltering(String searchString) {
        if (searchString.isEmpty()) {
            categorieListFiltered = categorieList;
        } else {
            ArrayList<CategorieParcelable> filteredList = new ArrayList<>();
            for (CategorieParcelable row : categorieList) {

                // name match condition. this might differ depending on your requirement
                // here we are looking for name or phone number match
                if (row.getLabel().toLowerCase().contains(searchString.toLowerCase())) {
                    filteredList.add(row);
                }
            }

            categorieListFiltered = filteredList;
        }

        notifyDataSetChanged();
    }

    private static class FindCategoriePosterTask extends AsyncTask<Void, Void, FindDolPhotoREST> {
        private static final String TAG = FindCategoriePosterTask.class.getSimpleName();
        private CategorieParcelable categorieParcelable;
        private CategorieProduitAdapter.CategorieProduitViewHolder holder;

        public FindCategoriePosterTask(CategorieParcelable categorieParcelable, CategorieProduitAdapter.CategorieProduitViewHolder holder) {
            this.categorieParcelable = categorieParcelable;
            this.holder = holder;
        }

        @Override
        protected FindDolPhotoREST doInBackground(Void... voids) {
            String original_file = categorieParcelable.getId() + "/0/"+ categorieParcelable.getId() + "/photos/" + categorieParcelable.getPoster().getFilename();
            String module_part = "category";

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

                    categorieParcelable.setPoster(findDolPhotoREST.getDolPhoto());
//                    chargement de la photo dans la vue
                    holder.poster.setBackground(new BitmapDrawable(decodedByte));
                }
            }
        }
    }
}