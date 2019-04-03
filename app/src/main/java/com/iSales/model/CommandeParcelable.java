package com.iSales.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.iSales.model.ClientParcelable;
import com.iSales.model.ProduitParcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by netserve on 15/09/2018.
 */

public class CommandeParcelable implements Parcelable {
    private String ref;
    private String total;
    private String mode_reglement;
    private String mode_reglement_id;
    private String mode_reglement_code;
    private String note_public;
    private String note_private;
    private long id;
    private long commande_id;
    private long socid;
    private long date;
    private long date_commande;
    private long date_livraison;
    private int statut;
    private int is_synchro;
    private String remise_absolue;
    private String remise_percent;
    private String remise;

    private ClientParcelable client;
    private ArrayList<ProduitParcelable> produits;

    public CommandeParcelable() {
    }

    public long getSocid() {
        return socid;
    }

    public void setSocid(long socid) {
        this.socid = socid;
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

    public long getCommande_id() {
        return commande_id;
    }

    public void setCommande_id(long commande_id) {
        this.commande_id = commande_id;
    }

    public int getIs_synchro() {
        return is_synchro;
    }

    public void setIs_synchro(int is_synchro) {
        this.is_synchro = is_synchro;
    }

    public int getStatut() {
        return statut;
    }

    public void setStatut(int statut) {
        this.statut = statut;
    }

    public String getMode_reglement() {
        return mode_reglement;
    }

    public void setMode_reglement(String mode_reglement) {
        this.mode_reglement = mode_reglement;
    }

    public String getMode_reglement_id() {
        return mode_reglement_id;
    }

    public void setMode_reglement_id(String mode_reglement_id) {
        this.mode_reglement_id = mode_reglement_id;
    }

    public String getMode_reglement_code() {
        return mode_reglement_code;
    }

    public void setMode_reglement_code(String mode_reglement_code) {
        this.mode_reglement_code = mode_reglement_code;
    }

    public String getNote_public() {
        return note_public;
    }

    public void setNote_public(String note_public) {
        this.note_public = note_public;
    }

    public String getNote_private() {
        return note_private;
    }

    public void setNote_private(String note_private) {
        this.note_private = note_private;
    }

    public String getRemise_absolue() {
        return remise_absolue;
    }

    public void setRemise_absolue(String remise_absolue) {
        this.remise_absolue = remise_absolue;
    }

    public String getRemise_percent() {
        return remise_percent;
    }

    public void setRemise_percent(String remise_percent) {
        this.remise_percent = remise_percent;
    }

    public String getRemise() {
        return remise;
    }

    public void setRemise(String remise) {
        this.remise = remise;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ref);
        dest.writeString(this.total);
        dest.writeString(this.mode_reglement);
        dest.writeString(this.mode_reglement_id);
        dest.writeString(this.mode_reglement_code);
        dest.writeString(this.note_public);
        dest.writeString(this.note_private);
        dest.writeLong(this.id);
        dest.writeLong(this.commande_id);
        dest.writeLong(this.socid);
        dest.writeLong(this.date);
        dest.writeLong(this.date_commande);
        dest.writeLong(this.date_livraison);
        dest.writeInt(this.statut);
        dest.writeInt(this.is_synchro);
        dest.writeString(this.remise_absolue);
        dest.writeString(this.remise_percent);
        dest.writeString(this.remise);
        dest.writeParcelable(this.client, flags);
        dest.writeTypedList(this.produits);
    }

    protected CommandeParcelable(Parcel in) {
        this.ref = in.readString();
        this.total = in.readString();
        this.mode_reglement = in.readString();
        this.mode_reglement_id = in.readString();
        this.mode_reglement_code = in.readString();
        this.note_public = in.readString();
        this.note_private = in.readString();
        this.id = in.readLong();
        this.commande_id = in.readLong();
        this.socid = in.readLong();
        this.date = in.readLong();
        this.date_commande = in.readLong();
        this.date_livraison = in.readLong();
        this.statut = in.readInt();
        this.is_synchro = in.readInt();
        this.remise_absolue = in.readString();
        this.remise_percent = in.readString();
        this.remise = in.readString();
        this.client = in.readParcelable(ClientParcelable.class.getClassLoader());
        this.produits = in.createTypedArrayList(ProduitParcelable.CREATOR);
    }

    public static final Creator<com.iSales.model.CommandeParcelable> CREATOR = new Creator<com.iSales.model.CommandeParcelable>() {
        @Override
        public com.iSales.model.CommandeParcelable createFromParcel(Parcel source) {
            return new com.iSales.model.CommandeParcelable(source);
        }

        @Override
        public com.iSales.model.CommandeParcelable[] newArray(int size) {
            return new com.iSales.model.CommandeParcelable[size];
        }
    };
}
