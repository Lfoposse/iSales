package com.rainbow_cl.i_sales.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rainbow_cl.i_sales.remote.model.DolPhoto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by netserve on 15/09/2018.
 */

public class CommandeParcelable implements Parcelable {
    private String ref;
    private String total;
    private long id;
    private long date;
    private long date_commande;
    private long date_livraison;

    private ClientParcelable client;
    private ArrayList<ProduitParcelable> produits;

    public CommandeParcelable() {
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDate_commande() {
        return date_commande;
    }

    public void setDate_commande(long date_commande) {
        this.date_commande = date_commande;
    }

    public long getDate_livraison() {
        return date_livraison;
    }

    public void setDate_livraison(long date_livraison) {
        this.date_livraison = date_livraison;
    }

    public ClientParcelable getClient() {
        return client;
    }

    public void setClient(ClientParcelable client) {
        this.client = client;
    }

    public List<ProduitParcelable> getProduits() {
        return produits;
    }

    public void setProduits(ArrayList<ProduitParcelable> produits) {
        this.produits = produits;
    }

    public static Creator<CommandeParcelable> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ref);
        parcel.writeString(total);
        parcel.writeLong(id);
        parcel.writeLong(date);
        parcel.writeLong(date_commande);
        parcel.writeLong(date_livraison);
        parcel.writeParcelable(client, i);
    }

    protected CommandeParcelable(Parcel in) {
        ref = in.readString();
        total = in.readString();
        id = in.readLong();
        date = in.readLong();
        date_commande = in.readLong();
        date_livraison = in.readLong();
        client = in.readParcelable(ClientParcelable.class.getClassLoader());
    }

    public static final Creator<CommandeParcelable> CREATOR = new Creator<CommandeParcelable>() {
        @Override
        public CommandeParcelable createFromParcel(Parcel in) {
            return new CommandeParcelable(in);
        }

        @Override
        public CommandeParcelable[] newArray(int size) {
            return new CommandeParcelable[size];
        }
    };
}
