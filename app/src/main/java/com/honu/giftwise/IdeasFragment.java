package com.honu.giftwise;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.honu.giftwise.adapters.GiftItemAdapter;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftwiseContract;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragments that displays Gift items in a ListView.
 */
public class IdeasFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
        mGiftAdapter = new GiftItemAdapter(getActivity(), cur, new GiftItemAdapter.GiftItemClickListener() {
            @Override
            public void onGiftItemClick(View view, Gift selection) {
                openGift(selection.getGiftId());
            }
        });

        mGiftRecyclerView.setAdapter(mGiftAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mGiftRecyclerView.setLayoutManager(llm);

        // register a context menu (long-click)
        registerForContextMenu(mGiftRecyclerView);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GIFT_IDEAS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_gift_item, menu);

        // TODO: the menuInfo is null so we do not the position of the item
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//
//        // Get cursor from the adapter
//        Cursor cursor = mGiftAdapter.getCursor();
//
//        // Extract Name from the selected item for menu title
//        cursor.moveToPosition(info.position);
//        String name = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME));
//        menu.setHeaderTitle(name);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // Get cursor from the adapter
        Cursor cursor = mGiftAdapter.getCursor();

        // Extract data from the selected item
        cursor.moveToPosition(info.position);
        int giftId = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID));

        switch (item.getItemId()) {
            case R.id.gift_view:
                openGift(giftId);
                return true;
            case R.id.gift_edit:
                editGift(giftId);
                return true;
            case R.id.gift_delete:
                deleteGift(giftId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public String getShareText() {
        if (mGiftAdapter != null)
            return mGiftAdapter.getShareText();
        return null;
    }

    private void openGift(long giftId) {
        Log.d(TAG, "Open GiftId: " + giftId);

        // Get cursor from the adapter
        Cursor cursor = mGiftAdapter.getCursor();

        // start activity to add/edit gift idea
        Intent intent = new Intent(getActivity(), ViewGiftActivity.class);
        Gift gift = Gift.createFromCursor(cursor);
        intent.putExtra("gift", gift);
        intent.putExtra("contactName", mContactName);
        intent.putExtra("contactId", mContactId);

        startActivityForResult(intent, 1);
    }

    private void editGift(long giftId) {
        Log.d(TAG, "Edit GiftId: " + giftId);

        // Get cursor from the adapter
        Cursor cursor = mGiftAdapter.getCursor();

        // start activity to add/edit gift idea
        Intent intent = new Intent(getActivity(), EditGiftActivity.class);
        Gift gift = Gift.createFromCursor(cursor);
        intent.putExtra("gift", gift);
        intent.putExtra("contactName", mContactName);

        startActivityForResult(intent, 1);
    }

    private void deleteGift(long giftId) {
        Log.d(TAG, "Delete Gift id: " + giftId);
        Uri uri = GiftwiseContract.GiftEntry.buildGiftsForGiftwiseIdUri(mGiftwiseId);
        String where = GiftwiseContract.GiftEntry.TABLE_NAME + "." + GiftwiseContract.GiftEntry._ID + " = ? ";
        String[] whereArgs =  new String[]{  Long.toString(giftId) };
        mGiftAdapter.removeImageFromCache("" + giftId);
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
