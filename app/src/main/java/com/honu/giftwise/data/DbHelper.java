package com.honu.giftwise.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.honu.giftwise.data.GiftwiseContract.GiftEntry;

/**
 * Created by bdiegel on 3/7/15.
 */
public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "giftwise.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_GIFT_TABLE = "CREATE TABLE " + GiftEntry.TABLE_NAME + " (" +
              GiftEntry._ID + " INTEGER PRIMARY KEY, " +
              GiftEntry.COLUMN_GIFT_RAWCONTACT_ID + " INTEGER NOT NULL, " +
              GiftEntry.COLUMN_GIFT_NAME + " TEXT NOT NULL, " +
              GiftEntry.COLUMN_GIFT_URL + " TEXT, " +
              GiftEntry.COLUMN_GIFT_PRICE + " REAL, " +
              GiftEntry.COLUMN_GIFT_CURRENCY_CODE + " TEXT, " +
              GiftEntry.COLUMN_GIFT_IMAGE + " BLOB, " +
              GiftEntry.COLUMN_GIFT_NOTES + " TEXT " +
              ");";

        db.execSQL(SQL_CREATE_GIFT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: handle upgrades as appropriate (this is destructive)
        db.execSQL("DROP TABLE IF EXISTS " + GiftEntry.TABLE_NAME);
        onCreate(db);
    }
}
