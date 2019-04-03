package com.iSales.adapter;

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
import android.widget.RadioButton;
import android.widget.TextView;

import com.iSales.database.AppDatabase;
import com.iSales.interfaces.ClientsAdapterListener;
import com.iSales.model.ClientParcelable;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.DolPhoto;
import com.iSales.remote.rest.FindDolPhotoREST;
import com.iSales.utility.CircleTransform;
import com.iSales.utility.ISalesUtility;
import com.iSales.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 26/09/2018.
 */

public class ClientsRadioAdapter extends RecyclerView.Adapter<ClientsRadioAdapter.ClientsRadioViewHolder> {
    private static final String TAG = ClientsRadioAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<com.iSales.model.ClientParcelable> clientsList;
    private ArrayList<com.iSales.model.ClientParcelable> clientsListFiltered;
    private int lastItemCheckedPosition = -1;


    private com.iSales.interfaces.ClientsAdapterListener mListener;

    //    database instance
    private com.iSales.database.AppDatabase mDb;

    private ClientsRadioAdapter.FindPosterTask findPosterTask;
    //    ViewHolder de l'adapter
    public class ClientsRadioViewHolder extends RecyclerView.ViewHolder {
        public TextView name, address;
        public ImageView thumbnail;
        public RadioButton radioButton;

        public ClientsRadioViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_clientradio_name);
            address = view.findViewById(R.id.tv_clientradio_address);
            thumbnail = view.findViewById(R.id.iv_clientradio_thumbnail);
            radioButton = view.findViewById(R.id.rbtn_clientradio_radio);

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "onClick: getAdapterPosition="+getAdapterPosition());
                    lastItemCheckedPosition = getAdapterPosition();
                    // send selected contact in callback
                    mListener.onClientsSelected(clientsListFiltered.get(getAdapterPosition()), getAdapterPosition());
                    notifyDataSetChanged();
                }
            };
            view.setOnClickListener(clickListener);
            radioButton.setOnClickListener(clickListener);
        }
    }


    public ClientsRadioAdapter(Context context, ArrayList<com.iSales.model.ClientParcelable> clientParcelables, ClientsAdapterListener listener) {
        this.mContext = context;
        this.clientsList = clientParcelables;
        this.mListener = listener;
        this.clientsListFiltered = clientParcelables;
        mDb = AppDatabase.getInstance(context.getApplicationContext());
    }

    @Override
    public ClientsRadioAdapter.ClientsRadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_client_radio, parent, false);

        return new ClientsRadioAdapter.ClientsRadioViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ClientsRadioAdapter.ClientsRadioViewHolder holder, final int position) {
        holder.name.setText(clientsListFiltered.get(position).getName());
        holder.address.setText(String.format("%s, %s", clientsListFiltered.get(position).getAddress(), clientsListFiltered.get(position).getTown()));
        holder.radioButton.setChecked(position == lastItemCheckedPosition);

//                    chargement de la photo par defaut dans la vue
        holder.thumbnail.setBackground(mContext.getResources().getDrawable(R.drawable.default_avatar_client));

//            si le fichier existe dans la memoire locale
        if (clientsListFiltered.get(position).getPoster().getContent() != null) {
            Log.e(TAG, "onBindViewHolder: clientImg="+clientsListFiltered.get(position).getPoster().getContent() );

            File imgFile = new File(clientsListFiltered.get(position).getPoster().getContent());
            if (imgFile.exists()) {

                Picasso.with(mContext)
                        .load(imgFile)
                        .transform(new com.iSales.utility.CircleTransform())
                        .placeholder(R.drawable.default_avatar_client)
                        .error(R.drawable.default_avatar_client)
                        .into(holder.thumbnail);
                return;
            } else {
//                    chargement de la photo par defaut dans la vue
                Picasso.with(mContext)
                        .load(R.drawable.default_avatar_client)
                        .transform(new com.iSales.utility.CircleTransform())
                        .into(holder.thumbnail);
            }
        } else {
//                    chargement de la photo par defaut dans la vue
            Picasso.with(mContext)
                    .load(R.drawable.default_avatar_client)
                    .transform(new com.iSales.utility.CircleTransform())
                    .into(holder.thumbnail);
        }

        String original_file = clientsListFiltered.get(position).getLogo();
        String module_part = "societe";
        Picasso.with(mContext)
                .load(com.iSales.remote.ApiUtils.getDownloadImg(mContext, module_part, original_file))
                .transform(new CircleTransform())
                .placeholder(R.drawable.isales_no_image)
                .error(R.drawable.isales_no_image)
                .into(holder.thumbnail, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                        if (mContext != null) {
                            Bitmap imageBitmap = ((BitmapDrawable) holder.thumbnail.getDrawable()).getBitmap();

                            String filename = String.format("%s_%s", clientsListFiltered.get(position).getId(), clientsListFiltered.get(position).getName().replace(" ", "_"))
                                    .replace(" ", "_");
                            String pathFile = com.iSales.utility.ISalesUtility.saveClientImage(mContext, imageBitmap, filename);

                            if (pathFile != null) clientsListFiltered.get(position).getPoster().setContent(pathFile);

//                    Modification du path de la photo du produit
                            mDb.clientDao().updateLogo_content(pathFile, clientsListFiltered.get(position).getId());
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return clientsListFiltered.size();
    }

    //    Filtre la liste des clients
    public void performFiltering(String searchString) {
        if (searchString.isEmpty()) {
            clientsListFiltered = clientsList;
        } else {
            ArrayList<com.iSales.model.ClientParcelable> filteredList = new ArrayList<>();
            for (com.iSales.model.ClientParcelable row : clientsList) {

                // name match condition. this might differ depending on your requirement
                // here we are looking for name or phone number match
                if (row.getName().toLowerCase().contains(searchString.toLowerCase())
                        || row.getAddress().toLowerCase().contains(searchString.toLowerCase())) {
                    filteredList.add(row);
                }
            }

            clientsListFiltered = filteredList;
        }

        notifyDataSetChanged();
    }

    //    recuperation de la photo de profil du client
    private class FindPosterTask extends AsyncTask<Void, Void, com.iSales.remote.rest.FindDolPhotoREST> {
        private com.iSales.model.ClientParcelable clientParcelable;
        private ClientsRadioAdapter.ClientsRadioViewHolder holder;

        private Context context;

        public FindPosterTask(Context context, ClientParcelable clientParcelable, ClientsRadioAdapter.ClientsRadioViewHolder holder) {
            this.clientParcelable = clientParcelable;
            this.holder = holder;
            this.context = context;
        }

        @Override
        protected com.iSales.remote.rest.FindDolPhotoREST doInBackground(Void... voids) {
            String original_file = clientParcelable.getLogo();
            String module_part = "societe";

//        Requete de connexion de l'internaute sur le serveur
            Call<com.iSales.remote.model.DolPhoto> call = ApiUtils.getISalesService(context).findProductsPoster(module_part, original_file);
            try {
                Response<com.iSales.remote.model.DolPhoto> response = call.execute();
                if (response.isSuccessful()) {
                    DolPhoto dolPhoto = response.body();
//                    Log.e(TAG, "doInBackground: dolPhoto | Filename=" + dolPhoto.getFilename() + " content=" + dolPhoto.getContent());
                    return new com.iSales.remote.rest.FindDolPhotoREST(dolPhoto);
                } else {
                    String error = null;
                    com.iSales.remote.rest.FindDolPhotoREST findDolPhotoREST = new com.iSales.remote.rest.FindDolPhotoREST();
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
                    Log.e(TAG, "onPostExecute: dolPhoto | Filename=" + findDolPhotoREST.getDolPhoto().getFilename() + " content=" + findDolPhotoREST.getDolPhoto().getContent());

//                    conversion de la photo du Base64 en bitmap
                    byte[] decodedString = Base64.decode(findDolPhotoREST.getDolPhoto().getContent(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


                    String filename = String.format("%s%s%s", clientParcelable.getId(), clientParcelable.getFirstname(), clientParcelable.getLastname())
                            .replace(" ", "_");
                    String pathFile = com.iSales.utility.ISalesUtility.saveClientImage(context, decodedByte, filename);

                    if (pathFile != null) clientParcelable.getPoster().setContent(pathFile);

//                    Modification du path de la photo du produit
                    mDb.clientDao().updateLogo_content(pathFile, clientParcelable.getId());

//                    chargement de la photo dans la vue
                    holder.thumbnail.setBackground(new BitmapDrawable(ISalesUtility.getRoundedCornerBitmap(decodedByte)));
                }
            }
        }
    }

}
