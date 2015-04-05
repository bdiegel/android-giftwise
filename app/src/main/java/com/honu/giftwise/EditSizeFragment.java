package com.honu.giftwise;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.honu.giftwise.data.Size;


public class EditSizeFragment extends Fragment {

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

    private void initItemView(View rootView) {

        mItemView = (AutoCompleteTextView) rootView.findViewById(R.id.item_spinner);
        mItemView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
             //   mItemView.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
              R.array.clothing_choices,
              android.R.layout.simple_dropdown_item_1line);

        mItemView.setAdapter(adapter);
    }

    private void initSizeView(View rootView) {

        mSizeView = (AutoCompleteTextView) rootView.findViewById(R.id.size_spinner);
        mSizeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //mSizeView.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
              R.array.size_choices,
              android.R.layout.simple_dropdown_item_1line);

        mSizeView.setAdapter(adapter);
    }
}
