package com.honu.giftwise;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

/**
* Created by bdiegel on 3/15/15.
*/
public class EditGiftFragment extends Fragment {

    private static final String LOG_TAG = EditGiftFragment.class.getSimpleName();

    private Spinner mContactSpinner;
    private SimpleCursorAdapter mContactAdapter;

    private long mRawContactId;

    // Url received from Intent
    private String mUrl;

    /**
     * Create Fragment and setup the Bundle arguments
     */
    public static EditGiftFragment getInstance(long rawContactId, String url) {
        EditGiftFragment fragment = new EditGiftFragment();

        // Attach some data needed to populate our fragment layouts
        Bundle args = new Bundle();
        args.putLong("rawContactId", rawContactId);
        args.putString("url", url);

        // Set the arguments on the fragment that will be fetched by the edit activity
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_edit_gift, container, false);

        // Get the Id of the raw contact
        Bundle args = getArguments();
        mRawContactId =  args.getLong("rawContactId", -1);
        mUrl =  args.getString("url");

        TextView urlTV = (TextView)rootView.findViewById(R.id.gift_url);
        urlTV.setText(mUrl);

        populateContactsSpinner(rootView);

        return rootView;
    }

    private void populateContactsSpinner(View root) {
        mContactSpinner = (Spinner) root.findViewById(R.id.contacts_spinner);

        // Query for list of contacts
        String accountName = getString(R.string.account_name);
        String accountType = getString(R.string.account_type);
        Cursor cursor = ContactsUtils.queryRawContacts(getActivity(), accountName, accountType);

        // Create adapter to display contacts in spinner
        String[] adapterCols = new String[] { ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY };
        int[] adapterRowViews = new int[] { android.R.id.text1 };
        mContactAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, cursor, adapterCols, adapterRowViews, 0);

        // Specify the layout to use when the list of choices appears
        mContactAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mContactSpinner.setAdapter(mContactAdapter);

        //selectSpinnerItemByValue(name);
        if (mRawContactId == -1) {
            mContactSpinner.setSelection(0);
            mRawContactId = mContactSpinner.getSelectedItemId();
        } else {
            selectSpinnerItemByContactId(mRawContactId);
        }
    }

    public void selectSpinnerItemByContactName(String value)
    {
        int position = 0;
        Cursor cursor = mContactAdapter.getCursor();

        for (int i=0; i<mContactAdapter.getCount(); i++) {
            cursor.moveToPosition(i);
            String temp = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME);
            long rawContactId = cursor.getLong(ContactsUtils.SimpleRawContactQuery.COL_RAW_CONTACT_ID);

            if ( temp.contentEquals(value) ) {
                Log.d("TAG", "Found match at index: " + i);
                mRawContactId = rawContactId;
                position = i;
                break;
            }
        }
        mContactSpinner.setSelection(position);
    }

    public void selectSpinnerItemByContactId(long value)
    {
        int position = 0;
        Cursor cursor = mContactAdapter.getCursor();

        for (int i=0; i<mContactAdapter.getCount(); i++) {
            cursor.moveToPosition(i);
            long temp = cursor.getLong(ContactsUtils.SimpleRawContactQuery.COL_RAW_CONTACT_ID);

            if ( temp == value ) {
                Log.d("TAG", "Found match at index: " + i);
                position = i;
                break;
            }
        }
        mContactSpinner.setSelection(position);
    }

}
