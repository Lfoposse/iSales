package com.iSales.adapter;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.iSales.R;
import com.iSales.database.AppDatabase;
import com.iSales.model.ProduitParcelable;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.DolPhoto;
import com.iSales.remote.rest.FindDolPhotoREST;
import com.iSales.utility.ISalesUtility;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 09/10/2018.
 */

public class CmdeProduitAdapter extends RecyclerView.Adapter<com.iSales.adapter.CmdeProduitAdapter.CmdeProduitViewHolder> {
    private static final String TAG = com.iSales.adapter.CmdeProduitAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<ProduitParcelable> produitsList;

    private com.iSales.adapter.CmdeProduitAdapter.FindPosterTask findPosterTask;

    private AppDatabase mDb;

    //    ViewHolder de l'adapter
    public class CmdeProduitViewHolder extends RecyclerView.ViewHolder {
        public TextView label, qtyPrice, total;
        public ImageView poster;
//        public ImageButton details;

        public CmdeProduitViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.tv_detailscmde_produit_label);
            qtyPrice = view.findViewById(R.id.tv_detailscmde_produit_qtyprice);
            total = view.findViewById(R.id.tv_detailscmde_produit_total);
            poster = view.findViewById(R.id.iv_detailscmde_produit);

//            fix image in view
            poster.setAdjustViewBounds(true);
            poster.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }


    public CmdeProduitAdapter(Context context, ArrayList<ProduitParcelable> produitParcelables) {
        this.mContext = context;
        this.produitsList = produitParcelables;
    }

    @Override
    public com.iSales.adapter.CmdeProduitAdapter.CmdeProduitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_detailscmde_produit, parent, false);

        return new com.iSales.adapter.CmdeProduitAdapter.CmdeProduitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final com.iSales.adapter.CmdeProduitAdapter.CmdeProduitViewHolder holder, int position) {

        holder.label.setText(produitsList.get(position).getLabel());
        holder.qtyPrice.setText(String.format("%s %s X %s  •  TVA %s %s  •  Remise %s %s",
                ISalesUtility.amountFormat2(produitsList.get(position).getPrice_ttc() == null || produitsList.get(position).getPrice_ttc().equals("") ? "0" : produitsList.get(position).getPrice_ttc()),
                ISalesUtility.CURRENCY,
                ISalesUtility.amountFormat2(produitsList.get(position).getQty()),
                ISalesUtility.amountFormat2(produitsList.get(position).getTva_tx() == null || produitsList.get(position).getTva_tx().equals("") ? "0" : produitsList.get(position).getTva_tx()),
                "%",
                ISalesUtility.amountFormat2(produitsList.get(position).getRemise_percent() == null || produitsList.get(position).getRemise_percent().equals("") ? "0" : produitsList.get(position).getRemise_percent()),
                "%"));
        holder.total.setText(String.format("%s %s",
                ISalesUtility.amountFormat2(produitsList.get(position).getTotal_ht()),
                ISalesUtility.CURRENCY) );

        String original_file = produitsList.get(position).getRef() + "/" + produitsList.get(position).getLocal_poster_path();
        String module_part = "produit";
        Log.e(TAG, "onBindViewHolder: getDownloadImg= "+ApiUtils.getDownloadImg(mContext, module_part, original_file));
        Picasso.with(mContext)
                .load(ApiUtils.getDownloadProductImg(mContext, produitsList.get(position).getRef()))
                .placeholder(R.drawable.isales_img_loading)
                .error(R.drawable.isales_no_image)
                .into(holder.poster);

        /*
        if (produitsList.get(position).getPoster() != null && produitsList.get(position).getPoster().getContent() == null) {
            Log.e(TAG, "onBindViewHolder: content=null");
//                    chargement de la photo dans la vue
//            holder.poster.setImageBitmap(mContext.getResources().getDrawable(R.drawable.logo_isales_small));
            holder.poster.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo_isales_small));

            if (findPosterTask == null) {
//        recuperation de la photo poster du produit
                findPosterTask = new CmdeProduitAdapter.FindPosterTask(mContext, produitsList.get(position), holder);
                findPosterTask.execute();
                findPosterTask = null;
            }
        } else {
            if (produitsList.get(position).getPoster() != null) {
                Log.e(TAG, "onBindViewHolder: content=not null");
                byte[] decodedString = Base64.decode(produitsList.get(position).getPoster().getContent(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//                    chargement de la photo dans la vue
                holder.poster.setImageBitmap(decodedByte);
            }
        } */
    }

    @Override
    public int getItemCount() {
        if (produitsList != null) {
            return produitsList.size();
        }
        return 0;
    }

    private class FindPosterTask extends AsyncTask<Void, Void, FindDolPhotoREST> {
        private ProduitParcelable produitParcelable;
        private com.iSales.adapter.CmdeProduitAdapter.CmdeProduitViewHolder holder;

        private Context context;

        public FindPosterTask(Context context, ProduitParcelable produitParcelable, com.iSales.adapter.CmdeProduitAdapter.CmdeProduitViewHolder holder) {
            this.produitParcelable = produitParcelable;
            this.holder = holder;
            this.context = context;
        }

        @Override
        protected FindDolPhotoREST doInBackground(Void... voids) {
            String original_file = produitParcelable.getRef() + "/" + produitParcelable.getPoster().getFilename();
            String module_part = "produit";

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

                    produitParcelable.setPoster(findDolPhotoREST.getDolPhoto());
//                    chargement de la photo dans la vue
                    holder.poster.setImageBitmap(decodedByte);
                }
            }
        }
    }
}
