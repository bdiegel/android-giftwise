package com.honu.giftwise.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Size implements Parcelable {

    private String item;

    private String size;

    private String notes;

    private long rawContactId = -1;

    private long sizeId = -1;

    public Size(String item, String size, String notes) {
        this.item = item;
        this.size = size;
        this.notes = notes;
    }

    public Size(long rawContactId) {
        this.rawContactId = rawContactId;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(long rawContactId) {
        this.rawContactId = rawContactId;
    }

    public long getSizeId() {
        return sizeId;
    }

    public void setSizeId(long sizeId) {
        this.sizeId = sizeId;
    }

    protected Size(Parcel in) {
        item = in.readString();
        size = in.readString();
        notes = in.readString();
        rawContactId = in.readLong();
        sizeId = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(item);
        dest.writeString(size);
        dest.writeString(notes);
        dest.writeLong(rawContactId);
        dest.writeLong(sizeId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Size> CREATOR = new Parcelable.Creator<Size>() {
        @Override
        public Size createFromParcel(Parcel in) {
            return new Size(in);
        }

        @Override
        public Size[] newArray(int size) {
            return new Size[size];
        }
    };

    public static final Size createFromCursor(Cursor cursor) {
        int sizeId = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.SizeEntry._ID));
        int rawContactId = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_RAWCONTACT_ID));
        String item = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_ITEM_NAME));
        String sizeName = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_NAME));
        String notes = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_NOTES));

        Size size = new Size(item, sizeName, notes);
        size.setSizeId(sizeId);
        size.setRawContactId(rawContactId);

        return size;
    }
}