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
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.interfaces.ProduitsAdapterListener;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindDolPhotoREST;
import com.rainbow_cl.i_sales.utility.BlurBuilder;
import com.rainbow_cl.i_sales.utility.ISalesUtility;
import com.squareup.picasso.Picasso;

import java.io.File;
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

//    private FindPosterTask findPosterTask;

    //    database instance
    private AppDatabase mDb;

    //    ViewHolder de l'adapter
    public class ProduitsViewHolder extends RecyclerView.ViewHolder {
        public TextView label, priceHT, priceTTC, stock;
        public ImageView poster;
        public ImageButton shooping;
//        public ImageButton details;

        public ProduitsViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.produit_item_label_tv);
            priceHT = view.findViewById(R.id.produit_item_price_ht_tv);
            priceTTC = view.findViewById(R.id.produit_item_price_ttc_tv);
            stock = view.findViewById(R.id.produit_item_stock_tv);
            poster = view.findViewById(R.id.produit_item_poster_iv);
            shooping = view.findViewById(R.id.produit_item_shopping_ib);
//            details = view.findViewById(R.id.produit_item_details_ib);

//            details.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // send selected contact in callback
//                    mListener.onDetailsSelected(produitsListFiltered.get(getAdapterPosition()));
//                }
//            });
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
                    mListener.onShoppingSelected(produitsListFiltered.get(getAdapterPosition()), getAdapterPosition());
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
        mDb = AppDatabase.getInstance(context.getApplicationContext());
    }

    @Override
    public ProduitsAdapter.ProduitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_produit, parent, false);

        return new ProduitsAdapter.ProduitsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProduitsAdapter.ProduitsViewHolder holder, final int position) {
//        Log.e(TAG, "onBindViewHolder: categorieId="+produitsListFiltered.get(position).getCategorie_id()+" label="+produitsListFiltered.get(position).getLabel()+" id="+produitsListFiltered.get(position).getId());

        holder.label.setText(produitsListFiltered.get(position).getLabel());
        holder.priceHT.setText(String.format("%s %s HT",
                ISalesUtility.amountFormat2(produitsListFiltered.get(position).getPrice()),
                ISalesUtility.CURRENCY));
        holder.priceTTC.setText(String.format("%s %s TTC",
                ISalesUtility.amountFormat2(produitsListFiltered.get(position).getPrice_ttc()),
                ISalesUtility.CURRENCY));
        holder.stock.setText(String.format("%s unités en stock  •  TVA: %s %s", produitsListFiltered.get(position).getStock_reel(), ISalesUtility.amountFormat2(produitsListFiltered.get(position).getTva_tx()), "%"));

//                    chargement de la photo dans la vue
//        holder.poster.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.isales_no_image));

        if (produitsListFiltered.get(position).getLocal_poster_path() != null) {
//            si le fichier existe dans la memoire locale
            File imgFile = new File(produitsListFiltered.get(position).getLocal_poster_path());
            if (imgFile.exists()) {
                Picasso.with(mContext)
                        .load(imgFile)
                        .into(holder.poster);
                return;

            } else {
//                    chargement de la photo dans la vue
                Picasso.with(mContext)
                        .load(R.drawable.isales_no_image)
                        .into(holder.poster);
            }

        } else {
//                    chargement de la photo dans la vue
            Picasso.with(mContext)
                    .load(R.drawable.isales_no_image)
                    .into(holder.poster);
        }
        Log.e(TAG, "onBindViewHolder: getFilename="+produitsListFiltered.get(position).getPoster().getFilename());
        String original_file = produitsListFiltered.get(position).getRef() + "/" + produitsListFiltered.get(position).getPoster().getFilename();
        String module_part = "produit";
//        Log.e(TAG, "onBindViewHolder: downloadLinkImg="+ApiUtils.getDownloadImg(mContext, module_part, original_file));
        Picasso.with(mContext)
                .load(ApiUtils.getDownloadImg(mContext, module_part, original_file))
                .placeholder(R.drawable.isales_no_image)
                .error(R.drawable.isales_no_image)
                .into(holder.poster, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                        if (mContext != null) {
                            Bitmap imageBitmap = ((BitmapDrawable) holder.poster.getDrawable()).getBitmap();

                            String pathFile = ISalesUtility.saveProduitImage(mContext, imageBitmap, produitsListFiltered.get(position).getRef());
                            Log.e(TAG, "onPostExecute: pathFile=" + pathFile);

                            if (pathFile != null) produitsListFiltered.get(position).setLocal_poster_path(pathFile);
//                    produitParcelable.setPoster(findDolPhotoREST.getDolPhoto());

//                    Modification du path de la photo du produit
                            mDb.produitDao().updateLocalImgPath(produitsListFiltered.get(position).getId(), pathFile);
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    @Override
    public int getItemCount() {
        if (produitsListFiltered != null) {
            return produitsListFiltered.size();
        }
        return 0;
    }

    /*
    private class FindPosterTask extends AsyncTask<Void, Void, FindDolPhotoREST> {
        private ProduitParcelable produitParcelable;
        private ProduitsAdapter.ProduitsViewHolder holder;

        private Context context;

        public FindPosterTask(Context context, ProduitParcelable produitParcelable, ProduitsViewHolder holder) {
            this.produitParcelable = produitParcelable;
            this.holder = holder;
            this.context = context;
        }

        @Override
        protected FindDolPhotoREST doInBackground(Void... voids) {
            String original_file = produitParcelable.getRef() + "/" + produitParcelable.getPoster().getFilename();
            String module_part = "produit";
            http://localhost:8888/Images.iSales/download.php?module_part=produit&original_file=cheese_cake/cheese_cake-Cheese_cake.jpg&DOLAPIKEY=9c524dc13288320153128086e6e69144fa743be3
//        Requete de connexion de l'internaute sur le serveur
            Call<DolPhoto> call = ApiUtils.getISalesService(context).findProductsPoster(module_part, original_file);
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

                    String pathFile = ISalesUtility.saveProduitImage(context, decodedByte, produitParcelable.getRef());
                    Log.e(TAG, "onPostExecute: pathFile=" + pathFile);

                    if (pathFile != null) produitParcelable.setLocal_poster_path(pathFile);
//                    produitParcelable.setPoster(findDolPhotoREST.getDolPhoto());

//                    Modification du path de la photo du produit
                    mDb.produitDao().updateLocalImgPath(produitParcelable.getId(), pathFile);

//                    chargement de la photo dans la vue
                    holder.poster.setImageBitmap(decodedByte);
                }
            }
        }
    }
    */
}
