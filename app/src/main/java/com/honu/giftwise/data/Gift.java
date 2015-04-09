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
    private long rawContactId = -1;
    private double price = 0;
    private String url;
    private String notes;
    private String currencyCode;

    public Gift() {}

    public Gift(long rawContactId) {
        this.rawContactId = rawContactId;
    }

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


    protected Gift(Parcel in) {
        name = in.readString();
        giftId = in.readLong();
        rawContactId = in.readLong();
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
        dest.writeLong(rawContactId);
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

    public static final Gift createFromCursor(Cursor cursor) {
        int giftId = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID));
        int rawContactId = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_RAWCONTACT_ID));
        String giftName = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME));
        String giftNotes = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NOTES));
        String giftUrl = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_URL));
        double price = cursor.getDouble(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_PRICE));
        String currencyCode = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_CURRENCY_CODE));

        Gift gift = new Gift(giftName);
        gift.setGiftId(giftId);
        gift.setNotes(giftNotes);
        gift.setUrl(giftUrl);
        gift.setPrice(price);
        gift.setRawContactId(rawContactId);
        gift.setCurrencyCode(currencyCode);

        return gift;
    }
}