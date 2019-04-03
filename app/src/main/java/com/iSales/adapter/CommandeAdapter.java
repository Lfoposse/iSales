package com.iSales.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.iSales.R;
import com.iSales.interfaces.CommandeAdapterListener;
import com.iSales.model.CommandeParcelable;
import com.iSales.utility.ISalesUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by netserve on 15/09/2018.
 */

public class CommandeAdapter extends RecyclerView.Adapter<com.iSales.adapter.CommandeAdapter.CommandeViewHolder> {
    private static final String TAG = com.iSales.adapter.CommandeAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<CommandeParcelable> commandeList;
    private ArrayList<CommandeParcelable> commandeListFiltered;
    private CommandeAdapterListener mListener;

    SimpleDateFormat mDaynumFormat = new SimpleDateFormat("dd");
    SimpleDateFormat mMonthFormat = new SimpleDateFormat("MMM yyyy");
    SimpleDateFormat mDayFormat = new SimpleDateFormat("EEEE");
    SimpleDateFormat mHeureFormat = new SimpleDateFormat("HH:mm");
//    private ClientsAdapter.FindPosterTask findPosterTask;

    //    ViewHolder de l'adapter
    public class CommandeViewHolder extends RecyclerView.ViewHolder {
        public TextView day, dayNum, month, hour, ref, client, countProduit, total;
        public ImageView synchro;
        public Button relancer;

        public CommandeViewHolder(View view) {
            super(view);
            day = view.findViewById(R.id.tv_commande_day);
            dayNum = view.findViewById(R.id.tv_commande_dd_num);
            month = view.findViewById(R.id.tv_commande_month);
            ref = view.findViewById(R.id.tv_commande_ref);
            client = view.findViewById(R.id.tv_commande_client);
            countProduit = view.findViewById(R.id.tv_commande_produits_count);
            total = view.findViewById(R.id.tv_commande_total);
            hour = view.findViewById(R.id.tv_commande_heure);
            relancer = view.findViewById(R.id.btn_commande_produits_relancer);
            synchro = view.findViewById(R.id.iv_commande_synchro);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    mListener.onCommandeSelected(commandeListFiltered.get(getAdapterPosition()));
                }
            });
            relancer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    mListener.onCommandeReStarted(commandeListFiltered.get(getAdapterPosition()));
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
    public com.iSales.adapter.CommandeAdapter.CommandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_commande, parent, false);

        return new com.iSales.adapter.CommandeAdapter.CommandeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull com.iSales.adapter.CommandeAdapter.CommandeViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder: commandeListFiltered.get(position).getDate()=" + commandeListFiltered.get(position).getDate() +
                " date_commande=" + commandeListFiltered.get(position).getDate_commande()+
        " getSocid="+commandeListFiltered.get(position).getSocid());

        holder.ref.setText(commandeListFiltered.get(position).getRef());
//        holder.client.setText(commandeListFiltered.get(position).getClient().getName());
        holder.countProduit.setText(String.format("%d Produit(s) • %s", commandeListFiltered.get(position).getProduits().size(),
                ISalesUtility.getStatutCmde(commandeListFiltered.get(position).getStatut()).toUpperCase()));
        holder.countProduit.setTextColor(mContext.getResources().getColor(ISalesUtility.getStatutCmdeColor(commandeListFiltered.get(position).getStatut())));

        holder.dayNum.setText(mDaynumFormat.format(commandeListFiltered.get(position).getDate_commande()));
        holder.day.setText(mDayFormat.format(commandeListFiltered.get(position).getDate_commande()));
        holder.hour.setText(mHeureFormat.format(commandeListFiltered.get(position).getDate_commande()));
        holder.month.setText(mMonthFormat.format(commandeListFiltered.get(position).getDate_commande()));
        holder.total.setText(String.format("%s %s",
                ISalesUtility.amountFormat2(commandeListFiltered.get(position).getTotal()),
                mContext.getString(R.string.symbole_euro)));
        holder.client.setText(commandeListFiltered.get(position).getClient().getName());

        if (commandeListFiltered.get(position).getIs_synchro() == 1) {
            holder.synchro.setBackgroundResource(R.drawable.ic_cloud_done_black_24dp);
        } else {
            holder.synchro.setBackgroundResource(R.drawable.ic_done_black_24dp);
        }

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

    //    Filtre la liste des clients
    public void performFilteringPreference(int synchro, boolean effectuer, boolean encours, boolean livrer, boolean nonpayer) {

        ArrayList<CommandeParcelable> filteredList = new ArrayList<>();
        for (CommandeParcelable row : commandeList) {

//            Log.e(TAG, "performFilteringPreference: statut="+row.getStatut()+" synchro="+row.getIs_synchro() );
            // Si mode de la commande est egale a 1 (online)
            if (synchro == 1 && row.getIs_synchro() == 1) {
                if (effectuer && row.getStatut() == 1) {
                    filteredList.add(row);
                }
                if (encours && row.getStatut() == 2) {
                    filteredList.add(row);
                }
                if (livrer && row.getStatut() == 3) {
                    filteredList.add(row);
                }
            } else if (synchro == 0 && row.getIs_synchro() == 0){
                filteredList.add(row);
            }
        }

        commandeListFiltered = filteredList;

        notifyDataSetChanged();
    }
}
