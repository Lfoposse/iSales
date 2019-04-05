package com.iSales.database.entry;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by netserve on 30/10/2018.
 */

@Entity(tableName = "client")
public class ClientEntry {
    @PrimaryKey(autoGenerate = true)
    private Long client_id;
    private Long id;
    private Long oid;
    private String name;
    private String name_alias;
    private String firstname;
    private String lastname;
    private String address;
    private String email;
    private String phone;
    private String region;
    private String departement;
    private String pays;
    private Long date_modification;
    private Long date_creation;
    private String town;
    private String logo;
    private String logo_content;
    private String code_client;
    private int is_synchro;
    private String note;
    private String note_private;
    private String note_public;
    private int is_current;

    public ClientEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_alias() {
        return name_alias;
    }

    public void setName_alias(String name_alias) {
        this.name_alias = name_alias;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public Long getDate_modification() {
        return date_modification;
    }

    public void setDate_modification(Long date_modification) {
        this.date_modification = date_modification;
    }

    public Long getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(Long date_creation) {
        this.date_creation = date_creation;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public int getIs_synchro() {
        return is_synchro;
    }

    public void setIs_synchro(int is_synchro) {
        this.is_synchro = is_synchro;
    }

    public String getLogo_content() {
        return logo_content;
    }

    public void setLogo_content(String logo_content) {
        this.logo_content = logo_content;
    }

    public String getCode_client() {
        return code_client;
    }

    public void setCode_client(String code_client) {
        this.code_client = code_client;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote_private() {
        return note_private;
    }

    public void setNote_private(String note_private) {
        this.note_private = note_private;
    }

    public String getNote_public() {
        return note_public;
    }

    public void setNote_public(String note_public) {
        this.note_public = note_public;
    }

    public int getIs_current() {
        return is_current;
    }

    public void setIs_current(int is_current) {
        this.is_current = is_current;
    }

    public Long getClient_id() {
        return client_id;
    }

    public void setClient_id(Long client_id) {
        this.client_id = client_id;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }
}
