package com.honu.giftwise.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by bdiegel on 3/7/15.
 */
public class GiftContentProvider extends ContentProvider {

    // database helper
    private DbHelper mDbHelper;

    // Uri matcher
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final int GIFT = 100;                           // gift
    private static final int GIFT_WITH_ID = 101;                   // gift by id
    private static final int GIFTS_BY_CONTACT = 102;               // gifts by raw contact id


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_GIFT, GIFT);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_GIFT + "/#", GIFT_WITH_ID);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_GIFT + "/" + GiftwiseContract.PATH_CONTACT + "/#", GIFTS_BY_CONTACT);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case GIFT_WITH_ID: {
                retCursor = getGiftById(uri,  projection, sortOrder);
                break;
            }
            case GIFTS_BY_CONTACT: {
                retCursor = getGiftsForRawContactId(uri, projection, sortOrder);
                break;
            }
            // "weather/*/*"
//            case WEATHER_WITH_LOCATION_AND_DATE:
//            {
//                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
//                break;
//            }
//            // "weather/*"
//            case WEATHER_WITH_LOCATION: {
//                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
//                break;
//            }
//            // "location/*"
//            case LOCATION_ID: {
//                long id = ContentUris.parseId(uri);
//                retCursor = mOpenHelper.getReadableDatabase().query(
//                      WeatherContract.LocationEntry.TABLE_NAME,
//                      projection,
//                      WeatherContract.LocationEntry._ID + " = '" + id + "'",
//                      null,
//                      null,
//                      null,
//                      sortOrder
//                );
//                break;
//            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        };

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case GIFT: {
                long _id = db.insert(GiftwiseContract.GiftEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = GiftwiseContract.GiftEntry.buildGiftUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;
        switch (match) {
            case GIFT_WITH_ID:
                rowsDeleted = db.delete(GiftwiseContract.GiftEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case GIFT_WITH_ID: {
                rowsUpdated = db.update(GiftwiseContract.GiftEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        // Returns the MIME type for this URI
        switch (match) {
            case GIFT_WITH_ID:
                return GiftwiseContract.GiftEntry.CONTENT_ITEM_TYPE;
            case GIFTS_BY_CONTACT:
                return GiftwiseContract.GiftEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    private Cursor getGiftById(Uri uri, String[] projection, String sortOrder) {
        long giftId = GiftwiseContract.GiftEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.GiftEntry.TABLE_NAME + "." + GiftwiseContract.GiftEntry._ID + " = ? ";
        String[] selectionArgs =  new String[]{  Long.toString(giftId) };

        return mDbHelper.getReadableDatabase().query(
              GiftwiseContract.GiftEntry.TABLE_NAME,
              projection,
              selection,
              selectionArgs,
              null,
              null,
              sortOrder
        );
    }

    private Cursor getGiftsForRawContactId(Uri uri, String[] projection, String sortOrder) {
        long rawContactId = GiftwiseContract.GiftEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.GiftEntry.TABLE_NAME + "." + GiftwiseContract.GiftEntry.COLUMN_GIFT_RAWCONTACT_ID + " = ? ";
        String[] selectionArgs =  new String[]{  Long.toString(rawContactId) };

        return mDbHelper.getReadableDatabase().query(
              GiftwiseContract.GiftEntry.TABLE_NAME,
              projection,
              selection,
              selectionArgs,
              null,
              null,
              sortOrder
        );
    }
}
