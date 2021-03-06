package com.honu.giftwise.data;

import android.app.backup.BackupManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
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

    // URIs: queries and notifications are per contact:
    private static final int GIFTS_BY_CONTACT = 102;               // gifts by raw contact gwid
    private static final int COLORS_BY_CONTACT = 202;              // colors by raw contact gwid
    private static final int SIZES_BY_CONTACT = 302;               // sizes by raw contact gwid
    private static final int CONTACT_BY_GWID = 402;               // contact by contact gwid
    private static final int CONTACTS = 403;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_GIFT + "/" + GiftwiseContract.PATH_GWID + "/*", GIFTS_BY_CONTACT);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_COLOR + "/" + GiftwiseContract.PATH_GWID + "/*", COLORS_BY_CONTACT);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_SIZE + "/" + GiftwiseContract.PATH_GWID + "/*", SIZES_BY_CONTACT);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_CONTACT + "/" + GiftwiseContract.PATH_GWID + "/*", CONTACT_BY_GWID);
        matcher.addURI(GiftwiseContract.CONTENT_AUTHORITY, GiftwiseContract.PATH_CONTACT, CONTACTS);
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
            case CONTACT_BY_GWID: {
                retCursor = getContactForGwid(uri, projection, sortOrder);
                break;
            }
            case CONTACTS: {
                retCursor = getContacts(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        synchronized (DbHelper.dbLock) {

            final SQLiteDatabase db = mDbHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);

            switch (match) {
                case GIFTS_BY_CONTACT: {
                    db.insert(GiftwiseContract.GiftEntry.TABLE_NAME, null, values);
                    break;
                }
                case COLORS_BY_CONTACT: {
                    db.insert(GiftwiseContract.ColorEntry.TABLE_NAME, null, values);
                    break;
                }
                case SIZES_BY_CONTACT: {
                    db.insert(GiftwiseContract.SizeEntry.TABLE_NAME, null, values);
                    break;
                }
                case CONTACT_BY_GWID: {
                    db.insert(GiftwiseContract.ContactEntry.TABLE_NAME, null, values);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        getContext().getContentResolver().notifyChange(uri, null);
        notifyBackupManager(getContext());

        return uri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        int rowsDeleted;

        synchronized (DbHelper.dbLock) {

            final SQLiteDatabase db = mDbHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);

            switch (match) {
                case GIFTS_BY_CONTACT:
                    rowsDeleted = db.delete(GiftwiseContract.GiftEntry.TABLE_NAME, where, whereArgs);
                    break;
                case COLORS_BY_CONTACT:
                    rowsDeleted = db.delete(GiftwiseContract.ColorEntry.TABLE_NAME, where, whereArgs);
                    break;
                case SIZES_BY_CONTACT:
                    rowsDeleted = db.delete(GiftwiseContract.SizeEntry.TABLE_NAME, where, whereArgs);
                    break;

                case CONTACT_BY_GWID:
                    rowsDeleted = db.delete(GiftwiseContract.ContactEntry.TABLE_NAME, where, whereArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            notifyBackupManager(getContext());
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int rowsUpdated;

        synchronized (DbHelper.dbLock) {

            final SQLiteDatabase db = mDbHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);

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
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            notifyBackupManager(getContext());
        }

        return rowsUpdated;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        // Returns the MIME type for this URI
        switch (match) {
            case GIFTS_BY_CONTACT:
                return GiftwiseContract.GiftEntry.CONTENT_TYPE;
            case COLORS_BY_CONTACT:
                return GiftwiseContract.ColorEntry.CONTENT_TYPE;
            case SIZES_BY_CONTACT:
                return GiftwiseContract.SizeEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Cursor getGiftsForRawContactId(Uri uri, String[] projection, String sortOrder) {
        String gwid = GiftwiseContract.GiftEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.GiftEntry.TABLE_NAME + "." + GiftwiseContract.GiftEntry.COLUMN_GIFT_GIFTWISE_ID + " = ? ";
        String[] selectionArgs =  new String[]{ gwid };

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

    private Cursor getColorsForRawContactId(Uri uri, String[] projection, String selection, String[] args, String sortOrder) {
        String gwid = GiftwiseContract.ColorEntry.getIdFromUri(uri);

        String selectionClause = GiftwiseContract.ColorEntry.TABLE_NAME + "." + GiftwiseContract.ColorEntry.COLUMN_COLOR_GIFTWISE_ID + " = ?  ";
        if (!TextUtils.isEmpty(selection)) {
            selectionClause = selectionClause + " AND " + selection;
        }
        String[] selectionArgs =  new String[args.length + 1];
        selectionArgs[0] = gwid;
        System.arraycopy(args, 0, selectionArgs, 1, args.length);

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
        String gwid = GiftwiseContract.SizeEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.SizeEntry.TABLE_NAME + "." + GiftwiseContract.SizeEntry.COLUMN_SIZE_GIFTWISE_ID + " = ? ";
        String[] selectionArgs =  new String[]{ gwid };

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

    private Cursor getContacts(Uri uri, String[] projection, String sortOrder) {

        return mDbHelper.getReadableDatabase().query(
              GiftwiseContract.ContactEntry.TABLE_NAME,
              projection,
              null,
              null,
              null,
              null,
              sortOrder
        );
    }

    private Cursor getContactForGwid(Uri uri, String[] projection, String sortOrder) {
        String gwid = GiftwiseContract.ContactEntry.getIdFromUri(uri);

        String selection = GiftwiseContract.ContactEntry.TABLE_NAME + "." + GiftwiseContract.ContactEntry.COLUMN_CONTACT_GIFTWISE_ID + " = ? ";
        String[] selectionArgs =  new String[]{ gwid };

        return mDbHelper.getReadableDatabase().query(
              GiftwiseContract.ContactEntry.TABLE_NAME,
              projection,
              selection,
              selectionArgs,
              null,
              null,
              sortOrder
        );
    }

    private void notifyBackupManager(Context ctx) {
        Log.d("GiftwiseContentProvider", "Requesting BACKUP");
        // notify the BackupManager there is changed data
        BackupManager backupManager = new BackupManager(ctx);
        backupManager.dataChanged();
    }

}
