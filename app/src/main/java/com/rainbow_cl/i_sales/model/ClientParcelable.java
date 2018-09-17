package com.rainbow_cl.i_sales.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.rainbow_cl.i_sales.remote.model.DolPhoto;

/**
 * Created by netserve on 28/08/2018.
 */

public class ClientParcelable implements Parcelable {
    private String name;
    private String address;
    private String logo;
    private long id;

    private DolPhoto poster;

    public ClientParcelable() {
    }

    public ClientParcelable(String name, String address, String logo) {
        this.name = name;
        this.address = address;
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

    public DolPhoto getPoster() {
        return poster;
    }

    public void setPoster(DolPhoto poster) {
        this.poster = poster;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeString(logo);
    }
    private ClientParcelable(Parcel in) {

        name = in.readString();
        address = in.readString();
        logo = in.readString();
    }
    public static final Parcelable.Creator<ClientParcelable> CREATOR
            = new Parcelable.Creator<ClientParcelable>() {

        @Override
        public ClientParcelable createFromParcel(Parcel in) {
            return new ClientParcelable(in);
        }

        @Override
        public ClientParcelable[] newArray(int size) {
            return new ClientParcelable[size];
        }
    };
}
