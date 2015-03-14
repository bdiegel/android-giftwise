package com.honu.giftwise.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bdiegel on 3/13/15.
 */
public class Gift implements Parcelable {

    private String name;
    private long giftId;
    private long rawContactId;
    private long price;
    private String url;
    private String notes;

    public Gift(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getGiftId() {
        return giftId;
    }

    public void setGiftId(long giftId) {
        this.giftId = giftId;
    }

    public long getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(long rawContactId) {
        this.rawContactId = rawContactId;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    protected Gift(Parcel in) {
        name = in.readString();
        giftId = in.readLong();
        rawContactId = in.readLong();
        price = in.readLong();
        url = in.readString();
        notes = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(giftId);
        dest.writeLong(rawContactId);
        dest.writeLong(price);
        dest.writeString(url);
        dest.writeString(notes);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Gift> CREATOR = new Parcelable.Creator<Gift>() {
        @Override
        public Gift createFromParcel(Parcel in) {
            return new Gift(in);
        }

        @Override
        public Gift[] newArray(int size) {
            return new Gift[size];
        }
    };
}