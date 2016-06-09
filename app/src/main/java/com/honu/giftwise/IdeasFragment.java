package com.honu.giftwise;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honu.giftwise.adapters.GiftItemAdapter;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftwiseContract;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragments that displays Gift items in a ListView.
 */
public class IdeasFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, GiftItemAdapter.GiftItemActionListener {

    private static final String TAG = IdeasFragment.class.getSimpleName();

    @Bind(R.id.gifts_recycler_view) RecyclerView mGiftRecyclerView;

    private GiftItemAdapter mGiftAdapter;

    //private int mPosition = ListView.INVALID_POSITION;

    private String mGiftwiseId;

    private String mContactName;

    private int mContactId;

    // loader id
    private static final int GIFT_IDEAS_LOADER = 1;

    public static IdeasFragment getInstance(String giftwiseId, String contactName, int contactId) {
        IdeasFragment fragment = new IdeasFragment();

        // Attach some data needed to populate our fragment layouts
        Bundle args = new Bundle();
        args.putString("contactName", contactName);
        args.putString("gwId", giftwiseId);
        args.putInt("contactId", contactId);

        // Set the arguments on the fragment that will be fetched by the edit activity
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact_ideas, container, false);
        ButterKnife.bind(this, rootView);

        Bundle args = getArguments();
        mContactName =  args.getString("contactName");
        mGiftwiseId =  args.getString("gwId");
        mContactId =  args.getInt("contactId");

        // initialize adapter (no data)
        Uri giftsForGwidUri = GiftwiseContract.GiftEntry.buildGiftsForGiftwiseIdUri(mGiftwiseId);
        Cursor cur = getActivity().getContentResolver().query(giftsForGwidUri, null, null, null, null);
        mGiftAdapter = new GiftItemAdapter(getActivity(), cur, this);

        mGiftRecyclerView.setAdapter(mGiftAdapter);
        int numberOfColumns = getResources().getInteger(R.integer.ideas_fragment_grid_columns);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(numberOfColumns, StaggeredGridLayoutManager.VERTICAL);
        mGiftRecyclerView.setLayoutManager(layoutManager);

        // register a context menu (long-click)
        registerForContextMenu(mGiftRecyclerView);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GIFT_IDEAS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public String getShareText() {
        if (mGiftAdapter != null)
            return mGiftAdapter.getShareText();
        return null;
    }

    public void openGift(Gift gift) {
        Intent intent = new Intent(getActivity(), ViewGiftActivity.class);
        intent.putExtra("gift", gift);
        intent.putExtra("contactName", mContactName);
        intent.putExtra("contactId", mContactId);
        startActivityForResult(intent, 1);
    }

    public void editGift(Gift gift) {
        Intent intent = new Intent(getActivity(), EditGiftActivity.class);
        intent.putExtra("gift", gift);
        intent.putExtra("contactName", mContactName);
        startActivityForResult(intent, 1);
    }

    public void deleteGift(Gift gift) {
        Log.d(TAG, "Delete Gift id: " + gift.getGiftId());
        Uri uri = GiftwiseContract.GiftEntry.buildGiftsForGiftwiseIdUri(mGiftwiseId);
        String where = GiftwiseContract.GiftEntry.TABLE_NAME + "." + GiftwiseContract.GiftEntry._ID + " = ? ";
        String[] whereArgs =  new String[]{  Long.toString(gift.getGiftId()) };
        mGiftAdapter.removeImageFromCache("" + gift.getGiftId());
        getActivity().getContentResolver().delete(uri, where, whereArgs);
    }

    @OnClick(R.id.add_gift_fab)
    public void addGiftClicked() {
        Log.d(TAG, "Add Gift for gwid: " + mGiftwiseId);

        // start activity to add/edit gift idea
        Intent intent = new Intent(getActivity(), EditGiftActivity.class);
        Gift gift = new Gift(mGiftwiseId);
        intent.putExtra("gift", gift);

        startActivityForResult(intent, 1);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(TAG, "Query Gifts for: " + mGiftwiseId);

        // uri for all gifts for a raw contact
        Uri giftsForGiftwiseIdUri = GiftwiseContract.GiftEntry.buildGiftsForGiftwiseIdUri(mGiftwiseId);

        // create CursorLoader that will take care of creating a Cursor for the data being displayed
        return new CursorLoader(
              getActivity(),
              giftsForGiftwiseIdUri,
              null,
              null,
              null,
              null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mGiftAdapter.swapCursor(cursor);

        // TODO: move to position
//        if (mPosition != ListView.INVALID_POSITION) {
//            ListView listView = (ListView) getView().findViewById(R.id.gifts_listview);
//            listView.smoothScrollToPosition(mPosition);
//        }

        // update the share intent
        ContactActivity contactActivity = (ContactActivity) getActivity();
        contactActivity.updateShareIntent(mGiftAdapter.getShareText());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
