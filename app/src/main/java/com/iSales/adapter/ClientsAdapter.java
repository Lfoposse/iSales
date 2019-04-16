package com.iSales.adapter;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.iSales.R;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.CommandeEntry;
import com.iSales.interfaces.ClientsAdapterListener;
import com.iSales.model.ClientParcelable;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.ConnectionManager;
import com.iSales.remote.model.DolPhoto;
import com.iSales.remote.rest.FindDolPhotoREST;
import com.iSales.utility.CircleTransform;
import com.iSales.utility.ISalesUtility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by netserve on 28/08/2018.
 */

public class ClientsAdapter extends RecyclerView.Adapter<com.iSales.adapter.ClientsAdapter.ClientsViewHolder> {
    private static final String TAG = com.iSales.adapter.ClientsAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<ClientParcelable> clientsList;
    private ArrayList<ClientParcelable> clientsListFiltered;
    private ClientsAdapterListener mListener;

    private FindPosterTask findPosterTask;

    //    database instance
    private AppDatabase mDb;

    //    ViewHolder de l'adapter
    public class ClientsViewHolder extends RecyclerView.ViewHolder {
        public TextView name, address;
        public ImageView thumbnail;
        public View viewBackground, viewForeground;
        private View statut;

        public ClientsViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_client_name);
            address = view.findViewById(R.id.tv_client_address);
            thumbnail = view.findViewById(R.id.iv_client_thumbnail);
            viewBackground = view.findViewById(R.id.view_item_client_background);
            viewForeground = view.findViewById(R.id.view_item_client_foreground);
            statut = view.findViewById(R.id.view_client_statut);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    mListener.onClientsSelected(clientsListFiltered.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }


    public ClientsAdapter(Context context, ArrayList<ClientParcelable> clientParcelables, ClientsAdapterListener listener) {
        this.mContext = context;
        this.clientsList = clientParcelables;
        this.mListener = listener;
        this.clientsListFiltered = clientParcelables;
        mDb = AppDatabase.getInstance(context.getApplicationContext());
    }

    @Override
    public ClientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_client, parent, false);

        return new ClientsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ClientsViewHolder holder, final int position) {
        holder.name.setText(clientsListFiltered.get(position).getName());
        holder.address.setText(String.format("%s • %s, %s", clientsListFiltered.get(position).getCode_client(), clientsListFiltered.get(position).getAddress(), clientsListFiltered.get(position).getTown()));

//                    chargement de la photo par defaut dans la vue
        holder.thumbnail.setBackground(mContext.getResources().getDrawable(R.drawable.default_avatar_client));

//                    Modification du path de la photo du produit
        List<CommandeEntry> cmdeStatut = mDb.commandeDao().getCmdeByClientOnStaut(clientsListFiltered.get(position).getId(), 1);
//        Log.e(TAG, "onBindViewHolder: cmdeStatutCount="+cmdeStatut.size()+" logo="+clientsListFiltered.get(position).getLogo());
        if (cmdeStatut.size() > 0) {
            holder.statut.setBackground(mContext.getResources().getDrawable(R.drawable.circle_red));
        } else {
            holder.statut.setBackground(mContext.getResources().getDrawable(R.drawable.circle_green));
        }

//            si le fichier existe dans la memoire locale
        if (clientsListFiltered.get(position).getPoster().getContent() != null) {
//            Log.e(TAG, "onBindViewHolder: clientImg=" + clientsListFiltered.get(position).getPoster().getContent());

            File imgFile = new File(clientsListFiltered.get(position).getPoster().getContent());
            if (imgFile.exists()) {
                Picasso.with(mContext)
                        .load(imgFile)
                        .transform(new CircleTransform())
                        .into(holder.thumbnail);
                return;

            } else {
//                    chargement de la photo par defaut dans la vue
                Picasso.with(mContext)
                        .load(R.drawable.default_avatar_client)
                        .transform(new CircleTransform())
                        .into(holder.thumbnail);
            }
        } else {
//                    chargement de la photo par defaut dans la vue
            Picasso.with(mContext)
                    .load(R.drawable.default_avatar_client)
                    .transform(new CircleTransform())
                    .into(holder.thumbnail);
        }

        String original_file = clientsListFiltered.get(position).getLogo();
        String module_part = "societe";
        Picasso.with(mContext)
                .load(ApiUtils.getDownloadImg(mContext, module_part, original_file))
                .transform(new CircleTransform())
                .placeholder(R.drawable.default_avatar_client)
                .error(R.drawable.default_avatar_client)
                .into(holder.thumbnail, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                        if (mContext != null) {
                            Bitmap imageBitmap = ((BitmapDrawable) holder.thumbnail.getDrawable()).getBitmap();

                            String filename = String.format("%s_%s", clientsListFiltered.get(position).getId(), clientsListFiltered.get(position).getName().replace(" ", "_"))
                                    .replace(" ", "_");
                            String pathFile = ISalesUtility.saveClientImage(mContext, imageBitmap, filename);

                            if (pathFile != null)
                                clientsListFiltered.get(position).getPoster().setContent(pathFile);

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
            ArrayList<ClientParcelable> filteredList = new ArrayList<>();
            for (ClientParcelable row : clientsList) {

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

    public void removeItem(final int position) {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(this.mContext)) {
            notifyItemChanged(position);
            Toast.makeText(this.mContext, this.mContext.getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            return;
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(mContext);
//        progressDialog.setTitle("Transfert d'Argent");
            progressDialog.setMessage(ISalesUtility.strCapitalize(mContext.getString(R.string.suppression_client_encours)));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.circular_progress_view));
            progressDialog.show();

            if(clientsListFiltered.get(position).getIs_synchro() == 0) {
                mDb.clientDao().deleteClientByClientId(clientsListFiltered.get(position).getClient_id());

                clientsListFiltered.remove(position);
                // notify the item removed by position
                // to perform recycler view delete animations
                // NOTE: don't call notifyDataSetChanged()
                notifyItemRemoved(position);

                progressDialog.dismiss();
                return;
            } else {
//        Requete de connexion de l'internaute sur le serveur
                Call<Long> call = ApiUtils.getISalesService(mContext).deleteThirdpartie(clientsListFiltered.get(position).getId());
                call.enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        if (response.isSuccessful()) {
                            Long responseBody = response.body();

                            mDb.clientDao().deleteClientByClientId(clientsListFiltered.get(position).getClient_id());

                            clientsListFiltered.remove(position);
                            // notify the item removed by position
                            // to perform recycler view delete animations
                            // NOTE: don't call notifyDataSetChanged()
                            notifyItemRemoved(position);

                            progressDialog.dismiss();
                            return;
                        } else {
                            String error = null;
                            try {
                                error = response.errorBody().string();
                                Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            notifyItemChanged(position);
                            progressDialog.dismiss();

                            Toast.makeText(mContext, mContext.getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        notifyItemChanged(position);
                        progressDialog.dismiss();

                        Toast.makeText(mContext, mContext.getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                });
            }
        }

    }

    //    recuperation de la photo de profil du client
    private class FindPosterTask extends AsyncTask<Void, Void, FindDolPhotoREST> {
        private ClientParcelable clientParcelable;
        private com.iSales.adapter.ClientsAdapter.ClientsViewHolder holder;

        private Context context;

        public FindPosterTask(Context context, ClientParcelable clientParcelable, com.iSales.adapter.ClientsAdapter.ClientsViewHolder holder) {
            this.clientParcelable = clientParcelable;
            this.holder = holder;
            this.context = context;
        }

        @Override
        protected FindDolPhotoREST doInBackground(Void... voids) {
//            String original_file = clientParcelable.getId() + "/logos/" + clientParcelable.getLogo();
            String original_file = clientParcelable.getLogo();
            String module_part = "societe";
//            Log.e(TAG, "doInBackground: client logo=" + clientParcelable.getLogo() + " id=" + clientParcelable.getId());

//        Requete de connexion de l'internaute sur le serveur
            Call<DolPhoto> call = ApiUtils.getISalesService(this.context).findProductsPoster(module_part, original_file);
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
//                        Log.e(TAG, "doInBackground: onResponse err: " + error + " code=" + response.code());
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
            if (findDolPhotoREST != null && findDolPhotoREST.getDolPhoto() != null) {

//                    conversion de la photo du Base64 en bitmap
                byte[] decodedString = Base64.decode(findDolPhotoREST.getDolPhoto().getContent(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                Log.e(TAG, "onPostExecute: updateString id=" + clientParcelable.getId() +
//                        " name=" + clientParcelable.getName() +
//                        " poster=" + findDolPhotoREST.getDolPhoto().getContent());

                String filename = String.format("%s_%s", clientParcelable.getId(), clientParcelable.getName().replace(" ", "_"))
                        .replace(" ", "_");
                String pathFile = ISalesUtility.saveClientImage(context, decodedByte, filename);

                if (pathFile != null) clientParcelable.getPoster().setContent(pathFile);

//                    Modification du path de la photo du produit
                mDb.clientDao().updateLogo_content(pathFile, clientParcelable.getId());

//                    chargement de la photo dans la vue
                holder.thumbnail.setBackground(new BitmapDrawable(ISalesUtility.getRoundedCornerBitmap(decodedByte)));
            }
        }
    }

}
