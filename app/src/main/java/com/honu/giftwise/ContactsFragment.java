package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.InputStream;

/**
 * A placeholder fragment containing a simple view.
 */
public  class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private static final String LOG_TAG = ContactsFragment.class.getSimpleName();

    private static final int CONTACTS_LOADER = 0;

    private ListView mListView;

    private ContactAdapter mContactAdapter;

    static class GiftwiseContactsQuery {

        // query projection for contact profile
        static final String[] projection = new String[]{
              RawContacts._ID,
              RawContacts.CONTACT_ID,
              RawContacts.DISPLAY_NAME_PRIMARY
        };

        //static final String[] fields = new String[] {ContactsContract.Data.DISPLAY_NAME};
        static final int COL_RAW_CONTACT_ID = 0;
        static final int COL_CONTACT_ID = 1;
        static final int COL_CONTACT_NAME = 2;
    }

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //readRawAccountTypes();
        //readRawAccounts();


        // initialize adapter (no data)
        mContactAdapter = new ContactAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.contacts_listview);
        mListView.setAdapter(mContactAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        // initialize loader of GiftWise contacts
        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);

        // listen for contact selections
        mListView.setOnItemClickListener(this);

        super.onActivityCreated(savedInstanceState);
    }


    final public Bitmap getContactPhoto(int contactId)
    {
        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);

        if (input == null) {
            return null;
        }

        return BitmapFactory.decodeStream(input);
    }

    // or: ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY
    public Cursor getContactBirthday(int contactId)
    {
        ContentResolver cr = getActivity().getContentResolver();

        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;

            String[] projection = new String[] {
                  ContactsContract.Data.CONTACT_ID,
                  ContactsContract.CommonDataKinds.Event.START_DATE,
                  ContactsContract.Data.MIMETYPE,
                  ContactsContract.CommonDataKinds.Event.TYPE
            };

            String where = ContactsContract.Data.CONTACT_ID + "=?"
                  + " AND " + ContactsContract.Data.MIMETYPE + "=?"
                  + " AND " + ContactsContract.CommonDataKinds.Event.TYPE + "=?";

            // Add contactId filter.
            String[] selectionArgs = new String[] {
                  String.valueOf(contactId),
                  ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                  String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
            };

            String sortOrder = null;

            return cr.query(uri, projection, where, selectionArgs, sortOrder);
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.d(LOG_TAG, "Error: " + message);

            return null;
        }
    }

    private void readRawAccounts() {
        String accountName = getString(R.string.account_name);
        String accountType = getString(R.string.account_type);

        Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
              .appendQueryParameter(RawContacts.ACCOUNT_NAME, accountName)
              .appendQueryParameter(RawContacts.ACCOUNT_TYPE, accountType)
              .build();

        Cursor cursor =  getActivity().getContentResolver().query(
              rawContactUri,
              new String[] { RawContacts._ID, RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE, RawContacts.DISPLAY_NAME_PRIMARY },
              null,
              null,
              null
        );

        while (cursor.moveToNext())
        {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
            String acctName = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
            String acctType = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
            String dispName = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
            Log.i(LOG_TAG, "Found raw account: id=" + id + " name=" + acctName + " type=" + acctType + " display=" + dispName);
        }
        cursor.close();
    }


    private Loader<Cursor> loadRawContacts() {

        String accountName = getString(R.string.account_name);
        String accountType = getString(R.string.account_type);

        Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
              .appendQueryParameter(RawContacts.ACCOUNT_NAME, accountName)
              .appendQueryParameter(RawContacts.ACCOUNT_TYPE, accountType)
              .appendQueryParameter(RawContacts.DELETED, "0")
              .build();


        return new CursorLoader(
              getActivity(),
              rawContactUri,
              GiftwiseContactsQuery.projection,
              null,
              null,
              null
        );
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return loadRawContacts();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mContactAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(LOG_TAG, "Item clicked: " + position);

        Intent intent = new Intent(getActivity(), ContactActivity.class);
        getActivity().startActivity(intent);


//        // Get the Cursor
//        Cursor cursor = parent.getAdapter().getCursor();
//        // Move to the selected contact
//        cursor.moveToPosition(position);
//        // Get the _ID value
//        String mContactId = getLong(CONTACT_ID_INDEX);
//        // Get the selected LOOKUP KEY
//        String mContactKey = getString(CONTACT_KEY_INDEX);
//        // Create the contact's content Uri
//        Uri mContactUri = Contacts.getLookupUri(mContactId, mContactKey);
//
//        /*
//         * You can use mContactUri as the content URI for retrieving
//         * the details for a contact.
//         */
    }

//        String[] fields = new String[] {ContactsContract.Data.DISPLAY_NAME};
//
//        SimpleCursorAdapter m_slvAdapter = new SimpleCursorAdapter(getActivity(),
//              android.R.layout.simple_list_item_1,
//              m_curContacts,
//              fields,
//              new int[] {android.R.id.text1},
//              0);
//        // Filter by name:
//        m_slvAdapter.setFilterQueryProvider(new FilterQueryProvider() {
//
//            public Cursor runQuery(CharSequence constraint) {
//                Log.d(LOG_TAG, "runQuery constraint:" + constraint);
//                String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'" +
//                      " AND "+ ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%"+constraint+"%'";
//                String[] selectionArgs = null;
//                Cursor cur = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
//                return cur;
//            }
//
//        });
//        m_lvContacts.setAdapter(m_slvAdapter);

    // <uses-permission android:name="android.permission.GET_ACCOUNTS" />


}
