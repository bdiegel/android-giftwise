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
import android.support.v4.widget.SimpleCursorAdapter;
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
import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftImageCache;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment for the gift editor
 */
public class EditGiftFragment extends Fragment {

    private static final String LOG_TAG = EditGiftFragment.class.getSimpleName();

    public static final int SELECT_IMAGE = 1;

    private SimpleCursorAdapter mContactAdapter;

    private Gift gift;

    private GiftImageCache mImageCache;

    private boolean mGiftImageUpdated = false;

    @Bind(R.id.gift_name) EditText mNameEdit;
    @Bind(R.id.gift_price) EditText mPriceEdit;
    @Bind(R.id.gift_url) EditText mUrlEdit;
    @Bind(R.id.gift_notes) EditText mNotesEdit;
    @Bind(R.id.gift_image) ImageView mImageView;
    @Bind(R.id.contacts_spinner) Spinner mContactSpinner;

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
        ButterKnife.bind(this, rootView);

        mImageCache = ((GiftwiseApplication)getActivity().getApplicationContext()).getGiftImageCache();

        // Get the Id of the raw contact
        Bundle args = getArguments();
        gift = args.getParcelable("gift");

        initViews(rootView, savedInstanceState);
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        double price = 0;
        String priceTxt = mPriceEdit.getText().toString();
        if (!TextUtils.isEmpty(priceTxt)) {
            try {
                price = Double.parseDouble(priceTxt);
            } catch (NumberFormatException nfe) {
                price = 0;
            }
        }

        // save values to bundle
        outState.putString("gift_name", mNameEdit.getText().toString());
        outState.putString("gift_url", mUrlEdit.getText().toString());
        outState.putString("gift_notes", mNotesEdit.getText().toString());
        outState.putDouble("gift_price", price);
        outState.putLong("contact_index", mContactSpinner.getSelectedItemId());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                Bitmap resizedBitmap = BitmapUtils.resizeBitmap(bitmap, 480);
                mImageView.setImageBitmap(resizedBitmap);

                // save to gift
                gift.setBitmap(BitmapUtils.getBytes(resizedBitmap));
                mGiftImageUpdated = true;

                if (gift.getGiftId() != -1) {
                    mImageCache.updateBitmapToMemoryCache(gift.getGiftId() + "", new BitmapDrawable(mImageView.getResources(), resizedBitmap));
                }
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error importing image: ", e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_gift_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_browse:
                openUrl();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews(View rootView, Bundle savedInstanceState) {
        mNameEdit.setText(gift.getName());
        if (gift.getPrice() > 0) {
            mPriceEdit.setText("" + gift.getFormattedPriceNoCurrency());
        }
        mUrlEdit.setText(gift.getUrl());
        mNotesEdit.setText(gift.getNotes());

        // set image from cache if exists
        BitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(gift.getGiftId() + "");
        if (bitmap != null ) {
            Log.d(LOG_TAG, "Bitmap loaded from cache for giftId: " + gift.getGiftId());
            mImageView.setImageDrawable(bitmap);
        } else {
            Log.d(LOG_TAG, "No bitmap found in cache for giftId: " + gift.getGiftId());
            bitmap = mImageCache.getPlaceholderImage();
            mImageView.setImageDrawable(bitmap);
        }

        // populate values for the recipient spin control:
        populateContactsSpinner(rootView);

        // repopulate form fields from state:
        if (savedInstanceState != null) {
            mNameEdit.setText(savedInstanceState.getString("gift_name"));
            mUrlEdit.setText(savedInstanceState.getString("gift_url"));
            mNotesEdit.setText(savedInstanceState.getString("gift_notes"));
            double price = savedInstanceState.getDouble("gift_price");
            if (price > 0) {
                mPriceEdit.setText(savedInstanceState.getDouble("gift_price") + "");
            }
            mContactSpinner.setSelection((int)savedInstanceState.getLong("contact_index"));
        }
    }

    @OnClick(R.id.gift_image)
    public void onSelectImageClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_IMAGE);
    }

    public boolean hasUnsavedChanges() {
        String gwidContact = getSelectedContactGWID();
        String nameTxt = mNameEdit.getText().toString();
        double price = getPrice();
        String url = mUrlEdit.getText().toString();
        String notes = mNotesEdit.getText().toString();

        return ( mGiftImageUpdated ||
              !(gift.getGiftwiseId().equals(gwidContact)) ||
              !(gift.getName().equals(nameTxt)) ||
              !(gift.getUrl().equals(url)) ||
              !(gift.getNotes().equals(notes)) ||
              gift.getPrice() != price
        );
    }

    private double getPrice() {
        String priceTxt = mPriceEdit.getText().toString();

        double price = 0;

        if (!TextUtils.isEmpty(priceTxt)) {
            try {
                price = Double.parseDouble(priceTxt);
            } catch (NumberFormatException nfe) {
                price = 0;
            }
        }
        return price;
    }

    private void openUrl() {
        String url = mUrlEdit.getText().toString();

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
        String gwid = gift.getGiftwiseId();
        if (gwid == null) {
            mContactSpinner.setSelection(0);
        } else {
            selectSpinnerItemByContactId(gwid);
        }
    }

    public void selectSpinnerItemByContactId(String value)
    {
        int position = 0;
        Cursor cursor = mContactAdapter.getCursor();

        for (int i=0; i < mContactAdapter.getCount(); i++) {
            cursor.moveToPosition(i);
            String temp = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_GWID);

            if (temp.equals(value)) {
                Log.d(LOG_TAG, "Found match at index: " + i);
                position = i;
                break;
            }
        }
        mContactSpinner.setSelection(position);
    }

    private String getSelectedContactGWID() {
        Cursor cursor = (Cursor)(mContactSpinner.getSelectedItem());
        if (cursor != null) {
            return cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_GWID);
        }
        return null;
    }
}
