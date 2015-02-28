package com.honu.giftwise;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.honu.giftwise.view.FloatingActionButton;

/**
 * A placeholder fragment containing a simple view.
 */
public  class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
      AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String LOG_TAG = ContactsFragment.class.getSimpleName();

    private static final int CONTACTS_LOADER = 0;

    private ListView mListView;

    private ContactAdapter mContactAdapter;

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

        FloatingActionButton addButton = (FloatingActionButton) rootView.findViewById(R.id.add_contact_fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).addContact();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        // initialize loader of GiftWise contacts
        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);

        // listen for contact selections
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String accountName = getString(R.string.account_name);
        String accountType = getString(R.string.account_type);
        return ContactsUtils.loadRawContacts(getActivity(), accountName, accountType);
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

        // Get the Cursor
        Cursor cursor = ((ContactAdapter)parent.getAdapter()).getCursor();

        // Extract data from the item in the Cursor:
        cursor.moveToPosition(position);
        String mContactId = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
        //String mContactKey = getString(CONTACT_KEY_INDEX);
        // Create the contact's content Uri
        //Uri mContactUri = Contacts.getLookupUri(mContactId, mContactKey);

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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        //        // TODO: delete RawContact and associated Data (with confirmation)
        Log.i(LOG_TAG, "Item long clicked: " + position);

        // Get the Cursor
        Cursor cursor = ((ContactAdapter)parent.getAdapter()).getCursor();

        // Extract data from the item in the Cursor:
        cursor.moveToPosition(position);
        //String mContactId = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
        int contactId = cursor.getInt(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
        Cursor c = ContactsUtils.getContactSpecialDates(getActivity(), contactId);
        while (c.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
            String type = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE));
            Log.i(LOG_TAG, "Found contact event: type=" + type + " date=" + date);
        }
        c.close();

        return true;
    }

}
