package com.honu.giftwise.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by bdiegel on 3/13/15.
 */
public class Gift implements Parcelable {

    private String name;
    private long giftId = -1;
    private String giftwiseId = null;
    private double price = 0;
    private String url;
    private String notes;
    private String currencyCode;

    private byte[] bitmap;

    public Gift() {}

    public Gift(String giftwiseId) { this.giftwiseId = giftwiseId; }

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

    public String getGiftwiseId() { return giftwiseId; }

    public void setGiftwiseId(String giftwiseId) { this.giftwiseId = giftwiseId; }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getFormattedPrice() {

        String priceString = "";

        if (price != 0) {
            Currency currency = Currency.getInstance(Locale.getDefault());
            if (!TextUtils.isEmpty(currencyCode)) {
                currency = Currency.getInstance(currencyCode);
            }

            NumberFormat format = NumberFormat.getCurrencyInstance();
            format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
            format.setCurrency(currency);
            priceString = format.format(price);
        }

        return priceString;
    }

    public String getFormattedPriceNoCurrency() {

        String priceString = "";

        if (price != 0) {
            Currency currency = Currency.getInstance(Locale.getDefault());
            if (!TextUtils.isEmpty(currencyCode)) {
                currency = Currency.getInstance(currencyCode);
            }

            NumberFormat format = NumberFormat.getInstance();
            format.setMinimumFractionDigits(currency.getDefaultFractionDigits());
            format.setCurrency(currency);
            priceString = format.format(price);
        }

        return priceString;
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }


    protected Gift(Parcel in) {
        name = in.readString();
        giftId = in.readLong();
        giftwiseId = in.readString();
        price = in.readDouble();
        url = in.readString();
        notes = in.readString();
        currencyCode = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(giftId);
        dest.writeString(giftwiseId);
        dest.writeDouble(price);
        dest.writeString(url);
        dest.writeString(notes);
        dest.writeString(currencyCode);
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

    public static Gift createFromCursor(Cursor cursor) {
        int giftId = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID));
        String giftwiseId = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_GIFTWISE_ID));
        String giftName = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME));
        String giftNotes = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NOTES));
        String giftUrl = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_URL));
        double price = cursor.getDouble(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_PRICE));
        String currencyCode = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_CURRENCY_CODE));

        Gift gift = new Gift();
        gift.setName(giftName);
        gift.setGiftId(giftId);
        gift.setNotes(giftNotes);
        gift.setUrl(giftUrl);
        gift.setPrice(price);
        gift.setGiftwiseId(giftwiseId);
        gift.setCurrencyCode(currencyCode);

        return gift;
    }
}