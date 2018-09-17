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
import android.widget.TextView;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.interfaces.CommandeAdapterListener;
import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.model.CommandeParcelable;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindDolPhotoREST;
import com.rainbow_cl.i_sales.utility.ISalesUtility;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by netserve on 15/09/2018.
 */

public class CommandeAdapter extends RecyclerView.Adapter<CommandeAdapter.CommandeViewHolder> {
    private static final String TAG = ClientsAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<CommandeParcelable> commandeList;
    private ArrayList<CommandeParcelable> commandeListFiltered;
    private CommandeAdapterListener mListener;

//    private ClientsAdapter.FindPosterTask findPosterTask;

    //    ViewHolder de l'adapter
    public class CommandeViewHolder extends RecyclerView.ViewHolder {
        public TextView day, dayNum, month, ref, client, countProduit, total;

        public CommandeViewHolder(View view) {
            super(view);
            day = view.findViewById(R.id.tv_commande_day);
            dayNum = view.findViewById(R.id.tv_commande_dd_num);
            month = view.findViewById(R.id.tv_commande_month);
            ref = view.findViewById(R.id.tv_commande_ref);
            client = view.findViewById(R.id.tv_commande_client);
            countProduit = view.findViewById(R.id.tv_commande_produits_count);
            total = view.findViewById(R.id.tv_commande_total);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    mListener.onCommandeSelected(commandeListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }


    public CommandeAdapter(Context context, ArrayList<CommandeParcelable> commandeParcelables, CommandeAdapterListener listener) {
        this.mContext = context;
        this.commandeList = commandeParcelables;
        this.mListener = listener;
        this.commandeListFiltered = commandeParcelables;
    }

    @Override
    public CommandeAdapter.CommandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_commande, parent, false);

        return new CommandeAdapter.CommandeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandeAdapter.CommandeViewHolder holder, int position) {
//        holder.ref.setText(commandeListFiltered.get(position).getRef());
//        holder.client.setText(commandeListFiltered.get(position).getClient().getName());
//        holder.countProduit.setText(String.format("%d Produit(s)", commandeListFiltered.get(position).getProduits().size()));

        /*
        if (commandeListFiltered.get(position).getPoster().getContent() == null) {
            Log.e(TAG, "onBindViewHolder: content=null");
//                    chargement de la photo dans la vue
//            holder.poster.setImageBitmap(mContext.getResources().getDrawable(R.drawable.logo_isales_small));
            holder.thumbnail.setBackground(mContext.getResources().getDrawable(R.drawable.logo_isales_small));

            if (findPosterTask == null) {
//        recuperation de la photo de profil du client
                findPosterTask = new ClientsAdapter.FindPosterTask(commandeListFiltered.get(position), holder);
                findPosterTask.execute();
                findPosterTask = null;
            }
        } else {
            Log.e(TAG, "onBindViewHolder: content=not null");
            byte[] decodedString = Base64.decode(commandeListFiltered.get(position).getPoster().getContent(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//                    chargement de la photo dans la vue
            holder.thumbnail.setBackground(new BitmapDrawable(ISalesUtility.getRoundedCornerBitmap(decodedByte)));
        } */
    }

    @Override
    public int getItemCount() {
        return commandeListFiltered.size();
    }

    //    Filtre la liste des clients
    public void performFiltering(String searchString) {
        if (searchString.isEmpty()) {
            commandeListFiltered = commandeList;
        } else {
            ArrayList<CommandeParcelable> filteredList = new ArrayList<>();
            for (CommandeParcelable row : commandeList) {

                // name match condition. this might differ depending on your requirement
                // here we are looking for name or phone number match
                if (row.getRef().toLowerCase().contains(searchString.toLowerCase())
                        || row.getClient().getName().toLowerCase().contains(searchString.toLowerCase())) {
                    filteredList.add(row);
                }
            }

            commandeListFiltered = filteredList;
        }

        notifyDataSetChanged();
    }

    private class FindPosterTask extends AsyncTask<Void, Void, FindDolPhotoREST> {
        private ClientParcelable clientParcelable;
        private ClientsAdapter.ClientsViewHolder holder;

        public FindPosterTask(ClientParcelable clientParcelable, ClientsAdapter.ClientsViewHolder holder) {
            this.clientParcelable = clientParcelable;
            this.holder = holder;
        }

        @Override
        protected FindDolPhotoREST doInBackground(Void... voids) {
            String original_file = clientParcelable.getId() + "/logos/" + clientParcelable.getLogo();
            String module_part = "societe";

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

                    clientParcelable.setPoster(findDolPhotoREST.getDolPhoto());
//                    chargement de la photo dans la vue
                    holder.thumbnail.setBackground(new BitmapDrawable(ISalesUtility.getRoundedCornerBitmap(decodedByte)));
                }
            }
        }
    }
}
