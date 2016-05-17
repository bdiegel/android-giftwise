package com.honu.giftwise.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.honu.giftwise.data.ContactImageCache;
import com.honu.giftwise.data.ContactsUtils;

/**
 *
 */
public class ContactEventDateLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    // data loaders for special dates
    public static final int PROFILE_BIRTHDAY_LOADER = 30;
    public static final int PROFILE_ANNIVERSARY_LOADER = 31;

    private static final String TAG = ContactEventDateLoader.class.getSimpleName();

    private int mContactId;

    private Context mContext;

    private ContactEventDateLoaderListener mListener;

    public interface ContactEventDateLoaderListener {
        void onBirthdayDateLoaded(String date);
        void onAnniversaryDateLoaded(String date);
    }

    public ContactEventDateLoader(Context context, ContactEventDateLoaderListener listener, int contactId) {
        mContext = context.getApplicationContext();
        mContactId = contactId;
        mListener = listener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        switch (id) {
            case PROFILE_BIRTHDAY_LOADER: {
                return ContactsUtils.getContactEventDateCurosrLoader(mContext, mContactId, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);
            }
            case PROFILE_ANNIVERSARY_LOADER: {
                return ContactsUtils.getContactEventDateCurosrLoader(mContext, mContactId, ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY);
            }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: " + loader.getId());

        switch (loader.getId()) {
            case PROFILE_BIRTHDAY_LOADER:
                String birthday = findEventDateByType(cursor, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);
                mListener.onBirthdayDateLoaded(birthday);
                break;
            case PROFILE_ANNIVERSARY_LOADER:
                String anniversary = findEventDateByType(cursor, ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY);
                mListener.onAnniversaryDateLoaded(anniversary);
                break;
            default:
                break;
        }

        // reset position (on orientation change the same cursor is reused)
        cursor.moveToPosition(-1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
    }

    private String findEventDateByType(Cursor cursor, int expectedType) {
        if ( cursor != null && cursor.moveToNext()) {
            int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE));
            String date = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
            Log.i(TAG, "Found event for contact: type=" + type + " date=" + date);

            if (type == expectedType) {
                return date;
            }
        }

        return null;
    }
}
