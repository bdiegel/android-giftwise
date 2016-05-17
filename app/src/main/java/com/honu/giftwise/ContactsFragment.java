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
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.honu.giftwise.adapters.ContactAdapter;
import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.data.GiftwiseContract;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment for displaying list of contacts for Main activity
 */
public  class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ContactAdapter.ContactItemActionListener {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private static final String LOG_TAG = ContactsFragment.class.getSimpleName();

    private static final int CONTACTS_LOADER = 0;

    @Bind(R.id.contacts_listview) RecyclerView mContactList;

    private ContactAdapter mContactAdapter;

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        // initialize adapter (no data)
        mContactAdapter = new ContactAdapter(getContext(), null,this);
        mContactList.setAdapter(mContactAdapter);
        mContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // initialize loader of GiftWise contacts (requires permission)
        showContacts();

        super.onActivityCreated(savedInstanceState);
    }

    @OnClick(R.id.add_contact_fab)
    public void onAddContactClicked() {
        ((MainActivity)getActivity()).addContact();
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
    public void viewContact(String contactName, long contactId, long rawId, String gwId) {
        Intent intent = new Intent(getActivity(), ContactActivity.class);
        intent.putExtra("name", contactName );
        intent.putExtra("contactId", String.valueOf(contactId));
        intent.putExtra("rawId", String.valueOf(rawId));
        intent.putExtra("gwId", gwId );
        getActivity().startActivity(intent);
    }

    @Override
    public void deleteContact(long rawContactId, String gwid) {
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

    @Override
    public void editContact(long contactId) {
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
