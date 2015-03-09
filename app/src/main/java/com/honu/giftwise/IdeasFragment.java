package com.honu.giftwise;

import android.content.ContentValues;
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
public class IdeasFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = IdeasFragment.class.getSimpleName();

    private IdeasAdapter mIdeasAdapter;

    private ListView mListView;

    private int mPosition = ListView.INVALID_POSITION;

    private long mRawContactId;

    public static IdeasFragment getInstance(int position, long rawContactId) {
        IdeasFragment fragment = new IdeasFragment();
        // Attach some data to the fragment
        // that we'll use to populate our fragment layouts
        Bundle args = new Bundle();
        args.putInt("page_position", position + 1);
        args.putLong("rawContactId", rawContactId);

        // Set the arguments on the fragment
        // that will be fetched in the
        // DemoFragment@onCreateView
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout resource that'll be returned
        View rootView = inflater.inflate(R.layout.fragment_contact_ideas, container, false);

        Bundle args = getArguments();
        mRawContactId =  args.getLong("rawContactId");


        // initialize adapter (no data)
        Uri giftsForRawContactUri = GiftwiseContract.GiftEntry.buildGiftsForRawContactUri(mRawContactId);
        Cursor cur = getActivity().getContentResolver().query(giftsForRawContactUri, null, null, null, null);
        mIdeasAdapter = new IdeasAdapter(getActivity(), cur, 0);
        //mIdeasAdapter = new IdeasAdapter(getActivity(), matrixCursor, 0);
        //mIdeasAdapter = new IdeasAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.gifts_listview);
        mListView.setAdapter(mIdeasAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                mPosition = position;
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

    private void addGift() {
        Log.i(LOG_TAG, "Add Gift for: " + mRawContactId);
        // TODO: Intent to launch EditGiftIdeaActivity
//        ContentValues weatherValues = new ContentValues();
//        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
//        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
//        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
//        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
        //this.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);

        ContentValues values = new ContentValues();
        values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME, "Gift1");
        values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_PRICE, 49.99);
        values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_URL, "http//bestgifts.com/gift1");
        values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_RAWCONTACT_ID, mRawContactId);
        this.getActivity().getContentResolver().insert(GiftwiseContract.GiftEntry.GIFT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        long startDate = System.currentTimeMillis();
//
//        // Sort order:  Ascending, by date.
//        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
//
//        String locationSetting = Utility.getPreferredLocation(getActivity());

//        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
//              locationSetting, startDate);

        Uri giftsForRawContactUri = GiftwiseContract.GiftEntry.buildGiftsForRawContactUri(mRawContactId);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        // use column indices to specify a projection
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
