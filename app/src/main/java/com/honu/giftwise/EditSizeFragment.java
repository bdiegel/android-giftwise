package com.honu.giftwise;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.honu.giftwise.data.GiftwiseContract;
import com.honu.giftwise.data.Size;


public class EditSizeFragment extends Fragment {

    private static final String LOG_TAG = EditSizeFragment.class.getSimpleName();

    private Size size;

    private AutoCompleteTextView mItemView;
    private AutoCompleteTextView mSizeView;

    public static EditSizeFragment getInstance(Size size) {
        EditSizeFragment fragment = new EditSizeFragment();

        // Attach some data needed to populate our fragment layouts
        Bundle args = new Bundle();
        args.putParcelable("size", size);

        // Set the arguments on the fragment that will be fetched by the edit activity
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_size, container, false);

        // Get the Id of the raw contact
        Bundle args = getArguments();
        size = args.getParcelable("size");

        //initClothingSpinner(rootView);
        initItemView(rootView);
        initSizeView(rootView);

        // repopulate form fields from state:
        if (savedInstanceState != null) {
            EditText itemEdit = (EditText) rootView.findViewById(R.id.item_spinner);
            EditText sizeEdit = (EditText) rootView.findViewById(R.id.size_spinner);
            EditText notesEdit = (EditText) rootView.findViewById(R.id.size_notes);

            itemEdit.setText(savedInstanceState.getString("size_item"));
            sizeEdit.setText(savedInstanceState.getString("size_size"));
            notesEdit.setText(savedInstanceState.getString("size_notes"));
        }

        // show options menu
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // get values from all fields:
        View rootView = getView();
        EditText itemEdit = (EditText) rootView.findViewById(R.id.item_spinner);
        EditText sizeEdit = (EditText) rootView.findViewById(R.id.size_spinner);
        EditText notesEdit = (EditText) rootView.findViewById(R.id.size_notes);

        // save values to bundle
        outState.putString("size_item", itemEdit.getText().toString());
        outState.putString("size_size", sizeEdit.getText().toString());
        outState.putString("size_notes", notesEdit.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            Log.i(LOG_TAG, "navigation icon clicked");

            if (createOrSaveSize()) {
                Intent intent = NavUtils.getParentActivityIntent(getActivity());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(getActivity(), intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initItemView(View rootView) {

        mItemView = (AutoCompleteTextView) rootView.findViewById(R.id.item_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
              R.array.clothing_choices,
              android.R.layout.simple_dropdown_item_1line);

        mItemView.setAdapter(adapter);
    }

    private void initSizeView(View rootView) {

        mSizeView = (AutoCompleteTextView) rootView.findViewById(R.id.size_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
              R.array.size_choices,
              android.R.layout.simple_dropdown_item_1line);

        mSizeView.setAdapter(adapter);
    }

    private boolean createOrSaveSize() {

        EditText itemEdit = (EditText) getActivity().findViewById(R.id.item_spinner);
        EditText sizeEdit = (EditText) getActivity().findViewById(R.id.size_spinner);
        EditText notesEdit = (EditText) getActivity().findViewById(R.id.size_notes);

        ContentValues values = new ContentValues();

        String itemName = itemEdit.getText().toString();
        String sizeName = sizeEdit.getText().toString();

        if (TextUtils.isEmpty(itemName)) {
            Toast.makeText(getActivity(), "Item name is required", Toast.LENGTH_LONG).show();
            itemEdit.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(sizeName)) {
            Toast.makeText(getActivity(), "Size is required", Toast.LENGTH_LONG).show();
            sizeEdit.requestFocus();
            return false;
        }

        values.put(GiftwiseContract.SizeEntry.COLUMN_SIZE_RAWCONTACT_ID, size.getRawContactId());
        values.put(GiftwiseContract.SizeEntry.COLUMN_SIZE_ITEM_NAME, itemName);
        values.put(GiftwiseContract.SizeEntry.COLUMN_SIZE_NAME, sizeName);
        values.put(GiftwiseContract.SizeEntry.COLUMN_SIZE_NOTES, notesEdit.getText().toString());

        if (size.getSizeId() == -1) {
            // insert new entry into table
            getActivity().getContentResolver().insert(GiftwiseContract.SizeEntry.SIZE_URI, values);
        } else {
            String selection = GiftwiseContract.GiftEntry._ID + " = ?";
            String[] selectionArgs = new String[]{size.getSizeId() + ""};
            getActivity().getContentResolver().update(GiftwiseContract.SizeEntry.SIZE_URI, values, selection, selectionArgs);
        }

        return true;
    }
}
