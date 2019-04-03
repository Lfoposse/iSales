package com.iSales.remote.model;

/**
 * Created by netserve on 02/10/2018.
 */

public class OrderLine {
    private String id;
    private String rowid;
    private String ref;
    private String product_ref;
    private String libelle;
    private String label;
    private String product_label;
    private String product_desc;
    private String qty;
    private String tva_tx;
    private String price;
    private String subprice;
    private String desc;
    private String total_ht;
    private String total_tva;
    private String total_ttc;
    private String description;
    private String remise;
    private String remise_percent;
    private long fk_commande;
    private long fk_product;
    private long commande_id;

    public OrderLine() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRowid() {
        return rowid;
    }

    public void setRowid(String rowid) {
        this.rowid = rowid;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getProduct_ref() {
        return product_ref;
    }

    public void setProduct_ref(String product_ref) {
        this.product_ref = product_ref;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getProduct_label() {
        return product_label;
    }

    public void setProduct_label(String product_label) {
        this.product_label = product_label;
    }

    public String getProduct_desc() {
        return product_desc;
    }

    public void setProduct_desc(String product_desc) {
        this.product_desc = product_desc;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubprice() {
        return subprice;
    }

    public void setSubprice(String subprice) {
        this.subprice = subprice;
    }

    public long getFk_commande() {
        return fk_commande;
    }

    public void setFk_commande(long fk_commande) {
        this.fk_commande = fk_commande;
    }

    public long getCommande_id() {
        return commande_id;
    }

    public void setCommande_id(long commande_id) {
        this.commande_id = commande_id;
    }

    public String getTva_tx() {
        return tva_tx;
    }

    public void setTva_tx(String tva_tx) {
        this.tva_tx = tva_tx;
    }

    public long getFk_product() {
        return fk_product;
    }

    public void setFk_product(long fk_product) {
        this.fk_product = fk_product;
    }

    public String getRemise() {
        return remise;
    }

    public void setRemise(String remise) {
        this.remise = remise;
    }

    public String getRemise_percent() {
        return remise_percent;
    }

    public void setRemise_percent(String remise_percent) {
        this.remise_percent = remise_percent;
    }
}
