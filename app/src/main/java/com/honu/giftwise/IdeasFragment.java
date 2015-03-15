package com.honu.giftwise;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import com.honu.giftwise.data.GiftwiseContract;
import com.honu.giftwise.view.FloatingActionButton;

/**
* Created by bdiegel on 3/4/15.
*/
public class IdeasFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> { //}, PopupMenu.OnMenuItemClickListener {

    private static final String LOG_TAG = IdeasFragment.class.getSimpleName();

    private IdeasAdapter mIdeasAdapter;

    private ListView mListView;

    private int mPosition = ListView.INVALID_POSITION;

    private long mRawContactId;

    // loader id
    private static final int GIFT_IDEAS_LOADER = 1;

    public static IdeasFragment getInstance(int position, long rawContactId) {
        IdeasFragment fragment = new IdeasFragment();

        // Attach some data needed to populate our fragment layouts
        Bundle args = new Bundle();
        args.putInt("page_position", position + 1);
        args.putLong("rawContactId", rawContactId);

        // Set the arguments on the fragment that will be fetched by the edit activity
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout resource that'll be returned
        View rootView = inflater.inflate(R.layout.fragment_contact_ideas, container, false);

        Bundle args = getArguments();
        mRawContactId =  args.getLong("rawContactId");
        Log.i(LOG_TAG, "onCreateView");

        // initialize adapter (no data)
        Uri giftsForRawContactUri = GiftwiseContract.GiftEntry.buildGiftsForRawContactUri(mRawContactId);
        Cursor cur = getActivity().getContentResolver().query(giftsForRawContactUri, null, null, null, null);
        mIdeasAdapter = new IdeasAdapter(getActivity(), cur, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.gifts_listview);
        mListView.setAdapter(mIdeasAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                mPosition = position;

                Log.i(LOG_TAG, "Item selected at position: " + position);
                Log.i(LOG_TAG, "View clicked: " + view.getId());
                Object item = mIdeasAdapter.getItem(position);

                // Get cursor from the adapter
                Cursor cursor = mIdeasAdapter.getCursor();

                // Extract data from the selected item
                cursor.moveToPosition(position);
                int giftId = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID));
                Log.i(LOG_TAG, "GiftId: " + giftId);

                // TODO: open item

                // start activity to add/edit gift idea
                Intent intent = new Intent(getActivity(), EditGiftActivity.class);
                intent.putExtra("rawContactId", mRawContactId);
                intent.putExtra("giftId", giftId);
                //intent.putExtra("giftName", giftId);
                startActivityForResult(intent, 1);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(LOG_TAG, "Item LONG pressed at position: " + position);

                // Get cursor from the adapter
                Cursor cursor = mIdeasAdapter.getCursor();

                // Extract data from the selected item
                cursor.moveToPosition(position);
                int giftId = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID));
                Log.i(LOG_TAG, "GiftId: " + giftId);

                // delete item
                deleteGift(giftId);

                return true;
            }
        });


        FloatingActionButton addButton = (FloatingActionButton) rootView.findViewById(R.id.add_gift_fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGift();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GIFT_IDEAS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void deleteGift(long giftId) {
        Log.i(LOG_TAG, "Delete Gift id: " + giftId);
        Uri uri = GiftwiseContract.GiftEntry.buildGiftUri(giftId);
        getActivity().getContentResolver().delete(uri, null, null);
    }

    private void addGift() {
        Log.i(LOG_TAG, "Add Gift for: " + mRawContactId);

        // start activity to add/edit gift idea
        Intent intent = new Intent(getActivity(), EditGiftActivity.class);
        intent.putExtra("rawContactId", mRawContactId);
        startActivityForResult(intent, 1);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Log.i(LOG_TAG, "onCreateLoader");

        // uri for all gifts for a raw contact
        Uri giftsForRawContactUri = GiftwiseContract.GiftEntry.buildGiftsForRawContactUri(mRawContactId);

        // create CursorLoader that will take care of creating a Cursor for the data being displayed
        return new CursorLoader(
              getActivity(),
              giftsForRawContactUri,
              null,
              null,
              null,
              null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mIdeasAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            ListView listView = (ListView) getView().findViewById(R.id.gifts_listview);
            listView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}
