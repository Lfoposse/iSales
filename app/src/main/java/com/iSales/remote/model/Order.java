package com.iSales.remote.model;

import com.iSales.remote.model.OrderLine;

import java.util.List;

/**
 * Created by netserve on 02/10/2018.
 */

public class Order {
    public String socid;
    public String id;
    public String date;
    public String mode_reglement;
    public String mode_reglement_id;
    public String mode_reglement_code;
    public String note_public;
    public String note_private;
    public String date_commande;
    public String date_livraison;
    public String user_author_id;
    public String ref;
    public String total_ht;
    public String total_tva;
    public String total_ttc;
    public String statut;
    public List<OrderLine> lines;
    private String remise_absolue;
    private String remise_percent;
    private String remise;

    public Order() {
    }

    public String getSocid() {
        return socid;
    }

    public void setSocid(String socid) {
        this.socid = socid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate_commande() {
        return date_commande;
    }

    public void setDate_commande(String date_commande) {
        this.date_commande = date_commande;
    }

    public String getDate_livraison() {
        return date_livraison;
    }

    public void setDate_livraison(String date_livraison) {
        this.date_livraison = date_livraison;
    }

    public String getUser_author_id() {
        return user_author_id;
    }

    public void setUser_author_id(String user_author_id) {
        this.user_author_id = user_author_id;
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

    public List<OrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
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
