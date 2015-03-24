package com.honu.giftwise;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.honu.giftwise.data.BitmapUtils;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftImageCache;

import java.io.IOException;

/**
 * Fragment for the gift editor
 */
public class EditGiftFragment extends Fragment {

    private static final String LOG_TAG = EditGiftFragment.class.getSimpleName();

    public static final int SELECT_IMAGE = 1;

    private Spinner mContactSpinner;

    private SimpleCursorAdapter mContactAdapter;

    private Gift gift;

    private GiftImageCache mImageCache;

    private ShareActionProvider mShareActionProvider;

    /**
     * Create Fragment and setup the Bundle arguments
     */
    public static EditGiftFragment getInstance(Gift gift) {
        EditGiftFragment fragment = new EditGiftFragment();

        // Attach some data needed to populate our fragment layouts
        Bundle args = new Bundle();
        args.putParcelable("gift", gift);

        // Set the arguments on the fragment that will be fetched by the edit activity
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_gift, container, false);

        mImageCache = ((GiftwiseApplication)getActivity().getApplicationContext()).getGiftImageCache();

        // Get the Id of the raw contact
        Bundle args = getArguments();
        gift = args.getParcelable("gift");

        // populate form with value:
        EditText nameTxt = (EditText)rootView.findViewById(R.id.gift_name);
        nameTxt.setText(gift.getName());
        if (gift.getPrice() > 0) {
            EditText priceTxt = (EditText) rootView.findViewById(R.id.gift_price);
            priceTxt.setText("" + gift.getPrice());
        }
        EditText urlTxt = (EditText)rootView.findViewById(R.id.gift_url);
        urlTxt.setText(gift.getUrl());
        //Linkify.addLinks(urlTxt, Linkify.WEB_URLS);
        EditText notesTxt = (EditText)rootView.findViewById(R.id.gift_notes);
        notesTxt.setText(gift.getNotes());

        // select image from gallery on click:
        ImageView imageView = (ImageView)rootView.findViewById(R.id.gift_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_IMAGE);
            }
        });

        // set image from cache if exists
        BitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(gift.getGiftId() + "");
        if (bitmap != null ) {
            //imageView.setImageBitmap(bitmap);
            Log.i(LOG_TAG, "Bitmap loaded from cache for giftId: " + gift.getGiftId());
            imageView.setImageDrawable(bitmap);
        } else {
            Log.i(LOG_TAG, "No bitmap found in cache for giftId: " + gift.getGiftId());
        }

        // populate values for the recipient spin control:
        populateContactsSpinner(rootView);

        // repopulate form fields from state:
        if (savedInstanceState != null) {
            nameTxt.setText(savedInstanceState.getString("gift_name"));
            urlTxt.setText(savedInstanceState.getString("gift_url"));
            notesTxt.setText(savedInstanceState.getString("gift_notes"));
            double price = savedInstanceState.getDouble("gift_price");
            if (price > 0) {
                EditText priceTxt = (EditText) rootView.findViewById(R.id.gift_price);
                priceTxt.setText(savedInstanceState.getDouble("gift_price") + "");
            }
            mContactSpinner.setSelection((int)savedInstanceState.getLong("contact_index"));
        }

        //nameTxt.clearFocus();
        //nameTxt.setBackgroundColor(0);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // get values from all fields:
        View rootView = getView();
        EditText nameEdit = (EditText)rootView.findViewById(R.id.gift_name);
        EditText priceEdit = (EditText) rootView.findViewById(R.id.gift_price);
        EditText urlEdit = (EditText)rootView.findViewById(R.id.gift_url);
        EditText notesEdit = (EditText)rootView.findViewById(R.id.gift_notes);

        double price = 0;
        String priceTxt = priceEdit.getText().toString();
        if (!TextUtils.isEmpty(priceTxt)) {
            try {
                price = Double.parseDouble(priceTxt);
            } catch (NumberFormatException nfe) {
                price = 0;
            }
        }

        // save values to bundle
        outState.putString("gift_name", nameEdit.getText().toString());
        outState.putString("gift_url", urlEdit.getText().toString());
        outState.putString("gift_notes", notesEdit.getText().toString());
        outState.putDouble("gift_price", price);
        outState.putLong("contact_index", mContactSpinner.getSelectedItemId());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

                //Bitmap resizedBitmap = BitmapUtils.resizeBitmap(bitmap, 640);
                Bitmap resizedBitmap = BitmapUtils.resizeBitmap(bitmap, 480);

                ImageView imageView = (ImageView) getView().findViewById(R.id.gift_image);
                //imageView.setImageBitmap(bitmap);
                imageView.setImageBitmap(resizedBitmap);
                Log.i(LOG_TAG, "Saving image to cache for giftId: " + gift.getGiftId());
                mImageCache.updateBitmapToMemoryCache(gift.getGiftId() + "", new BitmapDrawable(imageView.getResources(), resizedBitmap));
            } catch (IOException e) {
                Log.d(LOG_TAG, "Exception: ", e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate the fragment menu
        inflater.inflate(R.menu.menu_edit_gift_fragment, menu);

        // Retrieve the share menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);

        // Now get the ShareActionProvider from the item
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createtShareIntent());
            //mShareActionProvider.setShareHistoryFileName(null);
        } else {
            Log.d(LOG_TAG, "Problem finding ShareActionProvider");
            //shareActionProvider = new ShareActionProvider(getActivity());
            //MenuItemCompat.setActionProvider(shareItem, shareActionProvider);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//            case R.id.action_share:
//                newGame();
//                return true;
            case R.id.action_browse:
                openUrl();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Intent createtShareIntent() {
        Log.d(LOG_TAG, "Share gift item: " );

        Intent intent = new Intent(Intent.ACTION_SEND);
        // prevents Activity selected for sharing from being placed on app stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getTextDescription());
        return intent;
    }

    private String getTextDescription() {
        View rootView = getView();
        EditText nameEdit = (EditText)rootView.findViewById(R.id.gift_name);
        EditText priceEdit = (EditText) rootView.findViewById(R.id.gift_price);
        EditText urlEdit = (EditText)rootView.findViewById(R.id.gift_url);
        EditText notesEdit = (EditText)rootView.findViewById(R.id.gift_notes);

        double price = 0;
        String priceTxt = priceEdit.getText().toString();
        if (!TextUtils.isEmpty(priceTxt)) {
            try {
                price = Double.parseDouble(priceTxt);
            } catch (NumberFormatException nfe) {
                price = 0;
            }
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("Gift: %s\n", nameEdit.getText().toString()));
        buffer.append(String.format("Price: %s\n", ContactsUtils.formatPrice(getActivity(), "USD", price)));
        buffer.append(String.format("Notes: %s\n", notesEdit.getText().toString()));
        buffer.append(String.format(urlEdit.getText().toString()));

        return buffer.toString();
    }

    private void openUrl() {
        // get the url from the EditText field
        View rootView = getView();
        EditText urlEdit = (EditText)rootView.findViewById(R.id.gift_url);
        String url = urlEdit.getText().toString();

        // if empty, do nothing
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getActivity(), "No URL found", Toast.LENGTH_LONG).show();
            return;
        }

        // add prefix if necessary
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        // start activity to launch browser
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
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

        // If a contact id is available set the spinner selection:
        long rawContactId = gift.getRawContactId();
        if (rawContactId == -1) {
            mContactSpinner.setSelection(0);
        } else {
            selectSpinnerItemByContactId(rawContactId);
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
                gift.setRawContactId(rawContactId);
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
