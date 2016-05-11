package com.honu.giftwise;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.data.GiftwiseContract;

import java.util.ArrayList;

/**
 * Fragment for displaying list of contacts for Main activity
 */
public  class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
      AdapterView.OnItemClickListener {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private static final String LOG_TAG = ContactsFragment.class.getSimpleName();

    private static final int CONTACTS_LOADER = 0;

    private ListView mListView;

    private ContactAdapter mContactAdapter;

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

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

        registerForContextMenu(mListView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        // initialize loader of GiftWise contacts (requires permission)
        showContacts();

        // listen for contact selections
        mListView.setOnItemClickListener(this);
        //mListView.setOnItemLongClickListener(this);

        super.onActivityCreated(savedInstanceState);
    }

    private void showContacts() {
        int hasReadContactPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);

        if (hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            return;
        }

        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(getActivity(), "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_contact_item, menu);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // Get cursor from the adapter
        Cursor cursor = mContactAdapter.getCursor();

        // Extract Name from the selected item for menu title
        cursor.moveToPosition(info.position);
        String contactName = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME);

        menu.setHeaderTitle(contactName);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Log.d(LOG_TAG, "Selected info.position: " + info.position);

        // Get cursor from the adapter
        Cursor cursor = mContactAdapter.getCursor();

        // Extract data from the selected item
        cursor.moveToPosition(info.position);
        long contactId = cursor.getLong(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
        long rawId = cursor.getLong(ContactsUtils.SimpleRawContactQuery.COL_RAW_CONTACT_ID);
        String gwid = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_GWID);

        switch (item.getItemId()) {
            case R.id.contact_view:
                viewContact(mContactAdapter, info.position);
                Log.d(LOG_TAG, "View pressed");
                return true;
            case R.id.contact_edit:
                editContact(contactId);
                Log.d(LOG_TAG, "Edit pressed");
                return true;
            case R.id.contact_delete:
                Log.d(LOG_TAG, "Delete pressed");
                deleteContact(rawId, gwid);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
        viewContact((ContactAdapter)parent.getAdapter(), position);
    }

    private void viewContact(CursorAdapter adapter, int position) {

        // Get the Cursor
        Cursor cursor = adapter.getCursor();

        // Extract data from the item in the Cursor:
        cursor.moveToPosition(position);
        String contactId = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
        String contactName = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME);
        String rawId = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_RAW_CONTACT_ID);
        String gwId = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_GWID);

        //String mContactKey = getString(CONTACT_KEY_INDEX);
        // Create the contact's content Uri
        //Uri mContactUri = Contacts.getLookupUri(mContactId, mContactKey);

        Intent intent = new Intent(getActivity(), ContactActivity.class);
        intent.putExtra("name", contactName );
        intent.putExtra("contactId", contactId );
        intent.putExtra("rawId", rawId );
        intent.putExtra("gwId", gwId );
        getActivity().startActivity(intent);
    }

    private void deleteContact(long rawContactId, String gwid) {
        Log.d(LOG_TAG, "rawContactId: " + rawContactId + " gwid: " + gwid);

        // uris for deleting data
        Uri uriGifts = GiftwiseContract.GiftEntry.buildGiftsForGiftwiseIdUri(gwid);
        Uri uriColors = GiftwiseContract.ColorEntry.buildColorsForGiftwiseIdUri(gwid);
        Uri uriSizes = GiftwiseContract.SizeEntry.buildSizesForGiftwiseIdUri(gwid);

        // selection criteria for delete
        String[] selectionArgs =  new String[]{ gwid };
        String selectGifts = GiftwiseContract.GiftEntry.TABLE_NAME + "." + GiftwiseContract.GiftEntry.COLUMN_GIFT_GIFTWISE_ID + " = ? ";
        String selectColors = GiftwiseContract.ColorEntry.TABLE_NAME + "." + GiftwiseContract.ColorEntry.COLUMN_COLOR_GIFTWISE_ID + " = ? ";
        String selectSizes = GiftwiseContract.SizeEntry.TABLE_NAME + "." + GiftwiseContract.SizeEntry.COLUMN_SIZE_GIFTWISE_ID + " = ? ";

        // create batch of delete operations
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(uriGifts).withSelection(selectGifts, selectionArgs).build());
        ops.add(ContentProviderOperation.newDelete(uriColors).withSelection(selectColors, selectionArgs).build());
        ops.add(ContentProviderOperation.newDelete(uriSizes).withSelection(selectSizes, selectionArgs).build());

        // delete all data for raw contact
        try {
            getActivity().getContentResolver().applyBatch(GiftwiseContract.CONTENT_AUTHORITY, ops);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Batch delete failed", e);
            return;
        }

        // raw account name and type
        String accountName = getString(R.string.account_name);
        String accountType = getString(R.string.account_type);

        // delete raw contact from contact provider
        ContactsUtils.deleteRawContact(getActivity(), rawContactId, gwid, accountName, accountType);
    }

    private void editContact(long contactId) {
        // use content uri to edit contacts:
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

        // Creates a new Intent to edit a contact
        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        editIntent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        editIntent.putExtra("finishActivityOnSaveCompleted", true);
        editIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(editIntent);
    }
}
