package com.honu.giftwise.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class GiftContentProvider extends ContentProvider {

    // database helper
    private DbHelper mDbHelper;

    // Uri matcher
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Gift URIs
    private static final int GIFT = 100;                           // gift
    private static final int GIFT_WITH_ID = 101;                   // gift by id
    private static final int GIFTS_BY_CONTACT = 102;               // gifts by raw contact id

    // Color URIs
    private static final int COLOR = 200;                           // color
    private static final int COLOR_WITH_ID = 201;                   // color by id
    private static final int COLORS_BY_CONTACT = 202;               // colors by raw contact id

    // Size URIs
    private static final int SIZE = 300;                           // size
    private static final int SIZE_WITH_ID = 301;                   // size by id
    private static final int SIZES_BY_CONTACT = 302;               // sizes by raw contact id


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_GIFT, GIFT);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_GIFT + "/#", GIFT_WITH_ID);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_GIFT + "/" + GiftwiseContract.PATH_CONTACT + "/#", GIFTS_BY_CONTACT);

        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_COLOR, COLOR);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_COLOR + "/#", COLOR_WITH_ID);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_COLOR + "/" + GiftwiseContract.PATH_CONTACT + "/#", COLORS_BY_CONTACT);

        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_SIZE, SIZE);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_SIZE + "/#", SIZE_WITH_ID);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_SIZE + "/" + GiftwiseContract.PATH_CONTACT + "/#", SIZES_BY_CONTACT);
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
            case GIFTS_BY_CONTACT: {
                retCursor = getGiftsForRawContactId(uri, projection, sortOrder);
                break;
            }
            case COLORS_BY_CONTACT: {
                retCursor = getColorsForRawContactId(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            case SIZES_BY_CONTACT: {
                retCursor = getSizesForRawContactId(uri, projection, sortOrder);
                break;
            }
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

        switch (match) {
            case GIFTS_BY_CONTACT: {
                long _id = db.insert(GiftwiseContract.GiftEntry.TABLE_NAME, null, values);
                break;
            }
            case COLORS_BY_CONTACT: {
                Log.i("DbHelper", "Insert value: " + values);
                long _id = db.insert(GiftwiseContract.ColorEntry.TABLE_NAME, null, values);
                break;
            }
            case SIZES_BY_CONTACT: {
                long _id = db.insert(GiftwiseContract.SizeEntry.TABLE_NAME, null, values);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return uri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        // TODO: cleanup URIs
        int rowsDeleted;
        switch (match) {
            case GIFT_WITH_ID:
                rowsDeleted = deleteGiftById(uri);
                break;
            case COLOR_WITH_ID:
                rowsDeleted = deleteColorById(uri);
                break;
            case COLORS_BY_CONTACT:
                rowsDeleted = db.delete(GiftwiseContract.ColorEntry.TABLE_NAME, where, whereArgs);
                break;
            case SIZE_WITH_ID:
                rowsDeleted = deleteSizeById(uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (where == null || rowsDeleted != 0) {
            if (match == GIFT_WITH_ID)
                getContext().getContentResolver().notifyChange(GiftwiseContract.GiftEntry.GIFT_URI, null);
            else if (match == COLOR_WITH_ID)
                getContext().getContentResolver().notifyChange(uri, null);
            else if (match == SIZE_WITH_ID)
                getContext().getContentResolver().notifyChange(GiftwiseContract.SizeEntry.SIZE_URI, null);
        }

        if (match == COLORS_BY_CONTACT && rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case GIFTS_BY_CONTACT: {
                rowsUpdated = db.update(GiftwiseContract.GiftEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case SIZES_BY_CONTACT: {
                rowsUpdated = db.update(GiftwiseContract.SizeEntry.TABLE_NAME, values, selection, selectionArgs);
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
            case COLOR_WITH_ID:
                return GiftwiseContract.GiftEntry.CONTENT_ITEM_TYPE;
            case COLORS_BY_CONTACT:
                return GiftwiseContract.GiftEntry.CONTENT_TYPE;
            case SIZE_WITH_ID:
                return GiftwiseContract.GiftEntry.CONTENT_ITEM_TYPE;
            case SIZES_BY_CONTACT:
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

    private int deleteGiftById(Uri uri) {
        long giftId = GiftwiseContract.GiftEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.GiftEntry.TABLE_NAME + "." + GiftwiseContract.GiftEntry._ID + " = ? ";
        String[] selectionArgs =  new String[]{  Long.toString(giftId) };

        return mDbHelper.getReadableDatabase().delete(
              GiftwiseContract.GiftEntry.TABLE_NAME,
              selection,
              selectionArgs
        );
    }

    private Cursor getColorById(Uri uri, String[] projection, String sortOrder) {
        long giftId = GiftwiseContract.ColorEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.ColorEntry.TABLE_NAME + "." + GiftwiseContract.ColorEntry._ID + " = ? ";
        String[] selectionArgs =  new String[]{  Long.toString(giftId) };

        return mDbHelper.getReadableDatabase().query(
              GiftwiseContract.ColorEntry.TABLE_NAME,
              projection,
              selection,
              selectionArgs,
              null,
              null,
              sortOrder
        );
    }

    private Cursor getSizeById(Uri uri, String[] projection, String sortOrder) {
        long giftId = GiftwiseContract.SizeEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.SizeEntry.TABLE_NAME + "." + GiftwiseContract.SizeEntry._ID + " = ? ";
        String[] selectionArgs =  new String[]{  Long.toString(giftId) };

        return mDbHelper.getReadableDatabase().query(
              GiftwiseContract.SizeEntry.TABLE_NAME,
              projection,
              selection,
              selectionArgs,
              null,
              null,
              sortOrder
        );
    }

    private Cursor getColorsForRawContactId(Uri uri, String[] projection, String selection, String[] args, String sortOrder) {
        long rawContactId = GiftwiseContract.ColorEntry.getIdFromUri(uri);


        String selectionClause = GiftwiseContract.ColorEntry.TABLE_NAME + "." + GiftwiseContract.ColorEntry.COLUMN_COLOR_RAWCONTACT_ID + " = ?  ";
        if (!TextUtils.isEmpty(selection)) {
            selectionClause = selectionClause + " AND " + selection;
        }
        String[] selectionArgs =  new String[args.length + 1];
        selectionArgs[0] = Long.toString(rawContactId);
        for (int i=0; i<args.length; i++) {
            selectionArgs[i+1] = args[i];
        }

        return mDbHelper.getReadableDatabase().query(
              GiftwiseContract.ColorEntry.TABLE_NAME,
              projection,
              selectionClause,
              selectionArgs,
              null,
              null,
              sortOrder
        );
    }

    private Cursor getSizesForRawContactId(Uri uri, String[] projection, String sortOrder) {
        long rawContactId = GiftwiseContract.SizeEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.SizeEntry.TABLE_NAME + "." + GiftwiseContract.SizeEntry.COLUMN_SIZE_RAWCONTACT_ID + " = ? ";
        String[] selectionArgs =  new String[]{  Long.toString(rawContactId) };

        return mDbHelper.getReadableDatabase().query(
              GiftwiseContract.SizeEntry.TABLE_NAME,
              projection,
              selection,
              selectionArgs,
              null,
              null,
              sortOrder
        );
    }

    private int deleteColorById(Uri uri) {
        long giftId = GiftwiseContract.ColorEntry.getIdFromUri(uri);

        String where = GiftwiseContract.ColorEntry.TABLE_NAME + "." + GiftwiseContract.ColorEntry._ID + " = ? ";
        String[] whereArgs =  new String[]{  Long.toString(giftId) };

        return mDbHelper.getReadableDatabase().delete(
              GiftwiseContract.ColorEntry.TABLE_NAME,
              where,
              whereArgs
        );
    }

    private int deleteSizeById(Uri uri) {
        long giftId = GiftwiseContract.SizeEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.SizeEntry.TABLE_NAME + "." + GiftwiseContract.SizeEntry._ID + " = ? ";
        String[] selectionArgs =  new String[]{  Long.toString(giftId) };

        return mDbHelper.getReadableDatabase().delete(
              GiftwiseContract.SizeEntry.TABLE_NAME,
              selection,
              selectionArgs
        );
    }
}
