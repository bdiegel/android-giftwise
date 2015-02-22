package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.InputStream;

/**
 * A placeholder fragment containing a simple view.
 */
public  class ContactsFragment extends Fragment {

    private static final String LOG_TAG = ContactsFragment.class.getSimpleName();

    private ContactAdapter mContactAdapater;

    final String[] projection = new String[] {
          ContactsContract.Contacts._ID,
          ContactsContract.Contacts.DISPLAY_NAME
    };

    //static final String[] fields = new String[] {ContactsContract.Data.DISPLAY_NAME};
    static final int COL_CONTACT_ID = 0;
    static final int COL_CONTACT_NAME = 1;

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //final String[] fields = new String[] {ContactsContract.Data.DISPLAY_NAME};

        Cursor curContacts = readContacts();
        mContactAdapater = new ContactAdapter(getActivity(), curContacts, 0);


        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.contacts_listview);
        listView.setAdapter(mContactAdapater);

        return rootView;
    }

    private Cursor readContacts() {

        final Uri uri = ContactsContract.Contacts.CONTENT_URI;



        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";

        String[] selectionArgs = null;

        final String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor contacts = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

        return contacts;

//        String[] fields = new String[] {ContactsContract.Data.DISPLAY_NAME};
//
//        SimpleCursorAdapter m_slvAdapter = new SimpleCursorAdapter(getActivity(),
//              android.R.layout.simple_list_item_1,
//              m_curContacts,
//              fields,
//              new int[] {android.R.id.text1},
//              0);

        // Filter by name:
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


}
