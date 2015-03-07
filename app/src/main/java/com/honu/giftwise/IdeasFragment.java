package com.honu.giftwise;

import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.honu.giftwise.view.FloatingActionButton;

/**
* Created by bdiegel on 3/4/15.
*/
public class IdeasFragment extends Fragment {

    private static final String LOG_TAG = IdeasFragment.class.getSimpleName();

    private IdeasAdapter mIdeasAdapter;

    private ListView mListView;

    public static IdeasFragment getInstance(int position) {
        IdeasFragment fragment = new IdeasFragment();
        // Attach some data to the fragment
        // that we'll use to populate our fragment layouts
        Bundle args = new Bundle();
        args.putInt("page_position", position + 1);

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

        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"_id", "image", "name", "url", "price"}, 20);
        matrixCursor.addRow(new Object[]{0, null, "Book", "http://amaxon.com/books/some-book-url", "$7.99"});
        matrixCursor.addRow(new Object[]{1, null, "Shirt", "http://bananrepublic.com/some-shirt", "$35.00"});
        matrixCursor.addRow(new Object[]{2, null, "Blender", "http://kitchenstuff/blender", "$85.00"});

        // initialize adapter (no data)
        mIdeasAdapter = new IdeasAdapter(getActivity(), matrixCursor, 0);
        //mIdeasAdapter = new IdeasAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.gifts_listview);
        mListView.setAdapter(mIdeasAdapter);

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
        // TODO: Intent to launch EditGiftIdeaActivity
    }
}
