package com.iSales.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.iSales.remote.model.DolPhoto;

/**
 * Created by netserve on 28/08/2018.
 */

public class ClientParcelable implements Parcelable {
    private String name;
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
    private String code_client;
    private long id;
    private long client_id;
    private long oid;
    private int is_synchro;
    private String note;
    private String note_private;
    private String note_public;

    private DolPhoto poster;
    private int is_current;

    public ClientParcelable() {}

    public ClientParcelable(String name, String address, String town, String logo) {
        this.name = name;
        this.address = address;
        this.town = town;
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public DolPhoto getPoster() {
        return poster;
    }

    public void setPoster(DolPhoto poster) {
        this.poster = poster;
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

    public int getIs_synchro() {
        return is_synchro;
    }

    public void setIs_synchro(int is_synchro) {
        this.is_synchro = is_synchro;
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

    public long getClient_id() {
        return client_id;
    }

    public void setClient_id(long client_id) {
        this.client_id = client_id;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.firstname);
        dest.writeString(this.lastname);
        dest.writeString(this.address);
        dest.writeString(this.email);
        dest.writeString(this.phone);
        dest.writeString(this.region);
        dest.writeString(this.departement);
        dest.writeString(this.pays);
        dest.writeValue(this.date_modification);
        dest.writeValue(this.date_creation);
        dest.writeString(this.town);
        dest.writeString(this.logo);
        dest.writeString(this.code_client);
        dest.writeLong(this.id);
        dest.writeLong(this.client_id);
        dest.writeLong(this.oid);
        dest.writeInt(this.is_synchro);
        dest.writeString(this.note);
        dest.writeString(this.note_private);
        dest.writeString(this.note_public);
        dest.writeParcelable(this.poster, flags);
        dest.writeInt(this.is_current);
    }

    protected ClientParcelable(Parcel in) {
        this.name = in.readString();
        this.firstname = in.readString();
        this.lastname = in.readString();
        this.address = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
        this.region = in.readString();
        this.departement = in.readString();
        this.pays = in.readString();
        this.date_modification = (Long) in.readValue(Long.class.getClassLoader());
        this.date_creation = (Long) in.readValue(Long.class.getClassLoader());
        this.town = in.readString();
        this.logo = in.readString();
        this.code_client = in.readString();
        this.id = in.readLong();
        this.client_id = in.readLong();
        this.oid = in.readLong();
        this.is_synchro = in.readInt();
        this.note = in.readString();
        this.note_private = in.readString();
        this.note_public = in.readString();
        this.poster = in.readParcelable(DolPhoto.class.getClassLoader());
        this.is_current = in.readInt();
    }

    public static final Creator<ClientParcelable> CREATOR = new Creator<ClientParcelable>() {
        @Override
        public ClientParcelable createFromParcel(Parcel source) {
            return new ClientParcelable(source);
        }

        @Override
        public ClientParcelable[] newArray(int size) {
            return new ClientParcelable[size];
        }
    };
}
