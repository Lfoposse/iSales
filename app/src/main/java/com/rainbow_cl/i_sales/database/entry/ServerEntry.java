package com.rainbow_cl.i_sales.database.entry;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by netserve on 11/01/2019.
 */

@Entity(tableName = "server")
public class ServerEntry {
    @PrimaryKey(autoGenerate = false)
    private Long id;
    private String hostname;
    private String raison_sociale;
    private String adresse;
    private String code_postal;
    private String ville;
    private String departement;
    private String pays;
    private String devise;
    private String telephone;
    private String mail;
    private String website;
    private String note;
    private String title;
    private Boolean is_active;

    public ServerEntry() {

    }

    @Ignore
    public ServerEntry(String title, String hostname, Boolean is_active) {
        this.title = title;
        this.hostname = hostname;
        this.is_active = is_active;
    }

    @Ignore
    public ServerEntry(String hostname, String raison_sociale, String adresse, String code_postal, String ville, String departement, String pays, String devise, String telephone, String mail, String website, String note, String title, Boolean is_active) {
        this.hostname = hostname;
        this.raison_sociale = raison_sociale;
        this.adresse = adresse;
        this.code_postal = code_postal;
        this.ville = ville;
        this.departement = departement;
        this.pays = pays;
        this.devise = devise;
        this.telephone = telephone;
        this.mail = mail;
        this.website = website;
        this.note = note;
        this.title = title;
        this.is_active = is_active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
