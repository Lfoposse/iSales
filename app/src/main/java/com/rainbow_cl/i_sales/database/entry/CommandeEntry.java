package com.rainbow_cl.i_sales.database.entry;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by netserve on 03/10/2018.
 */

@Entity(tableName = "commande")
public class CommandeEntry {
    @PrimaryKey(autoGenerate = true)
    private Long commande_id;
    private Long id;
    private Long socid;
    private Long date;
    private Long date_commande;
    private Long date_livraison;
    private String user_author_id;
    private String user_valid;
    private String ref;
    private String total_ht;
    private String total_tva;
    private String total_ttc;
    private String statut;
    private int is_synchro;

    public CommandeEntry() {
    }

    public Long getCommande_id() {
        return commande_id;
    }

    public void setCommande_id(Long commande_id) {
        this.commande_id = commande_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSocid() {
        return socid;
    }

    public void setSocid(Long socid) {
        this.socid = socid;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getDate_commande() {
        return date_commande;
    }

    public void setDate_commande(Long date_commande) {
        this.date_commande = date_commande;
    }

    public Long getDate_livraison() {
        return date_livraison;
    }

    public void setDate_livraison(Long date_livraison) {
        this.date_livraison = date_livraison;
    }

    public String getUser_author_id() {
        return user_author_id;
    }

    public void setUser_author_id(String user_author_id) {
        this.user_author_id = user_author_id;
    }

    public String getUser_valid() {
        return user_valid;
    }

    public void setUser_valid(String user_valid) {
        this.user_valid = user_valid;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getTotal_ht() {
        return total_ht;
    }

    public void setTotal_ht(String total_ht) {
        this.total_ht = total_ht;
    }

    public String getTotal_tva() {
        return total_tva;
    }

    public void setTotal_tva(String total_tva) {
        this.total_tva = total_tva;
    }

    public String getTotal_ttc() {
        return total_ttc;
    }

    public void setTotal_ttc(String total_ttc) {
        this.total_ttc = total_ttc;
    }

    public int getIs_synchro() {
        return is_synchro;
    }

    public void setIs_synchro(int is_synchro) {
        this.is_synchro = is_synchro;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}