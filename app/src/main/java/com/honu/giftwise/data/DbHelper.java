package com.honu.giftwise.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.honu.giftwise.data.GiftwiseContract.ColorEntry;
import com.honu.giftwise.data.GiftwiseContract.GiftEntry;
import com.honu.giftwise.data.GiftwiseContract.SizeEntry;

/**
 * Created by bdiegel on 3/7/15.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DbHelper.class.getSimpleName();

    // Increment the database version if you change the schema
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

        final String SQL_CREATE_COLOR_TABLE = "CREATE TABLE " + ColorEntry.TABLE_NAME + " (" +
              ColorEntry._ID + " INTEGER PRIMARY KEY, " +
              ColorEntry.COLUMN_COLOR_RAWCONTACT_ID + " INTEGER NOT NULL, " +
              ColorEntry.COLUMN_COLOR_VALUE + " INTEGER NOT NULL, " +
              ColorEntry.COLUMN_COLOR_LIKED + " INTEGER DEFAULT 1 " +
              ");";

        db.execSQL(SQL_CREATE_COLOR_TABLE);

        final String SQL_CREATE_SIZE_TABLE = "CREATE TABLE " + SizeEntry.TABLE_NAME + " (" +
              SizeEntry._ID + " INTEGER PRIMARY KEY, " +
              SizeEntry.COLUMN_SIZE_RAWCONTACT_ID + " INTEGER NOT NULL, " +
              SizeEntry.COLUMN_SIZE_ITEM_NAME + " TEXT NOT NULL, " +
              SizeEntry.COLUMN_SIZE_NAME + " TEXT NOT NULL, " +
              SizeEntry.COLUMN_SIZE_NOTES + " TEXT " +
              ");";

        db.execSQL(SQL_CREATE_SIZE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: handle upgrades as appropriate (this is destructive)
        Log.i(LOG_TAG, "Performing database upgrade from version: " + oldVersion + " to: " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + GiftEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ColorEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SizeEntry.TABLE_NAME);
        onCreate(db);
    }
}
