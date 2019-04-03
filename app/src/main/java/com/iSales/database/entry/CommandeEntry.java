package com.iSales.database.entry;

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
    private String mode_reglement;
    private String mode_reglement_id;
    private String mode_reglement_code;
    private String note_public;
    private String note_private;
    private String user_author_id;
    private String user_valid;
    private String ref;
    private String total_ht;
    private String total_tva;
    private String total_ttc;
    private String statut;
    private String remise_absolue;
    private String remise_percent;
    private String remise;
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
}
