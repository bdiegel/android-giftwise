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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.honu.giftwise.data.BitmapUtils;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftImageCache;

import java.io.IOException;

/**
* Created by bdiegel on 3/15/15.
*/
public class EditGiftFragment extends Fragment {

    private static final String LOG_TAG = EditGiftFragment.class.getSimpleName();

    public static final int SELECT_IMAGE = 1;

    private Spinner mContactSpinner;

    private SimpleCursorAdapter mContactAdapter;

    private Gift gift;

    private GiftImageCache mImageCache;

    /**
     * Create Fragment and setup the Bundle arguments
     */
    //public static EditGiftFragment getInstance(long rawContactId, String url) {
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
        EditText priceTxt = (EditText)rootView.findViewById(R.id.gift_price);
        priceTxt.setText("" + gift.getPrice());
        EditText urlTxt = (EditText)rootView.findViewById(R.id.gift_url);
        urlTxt.setText(gift.getUrl());
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
            imageView.setImageDrawable(bitmap);
        }

        // populate values for the recipient spin control:
        populateContactsSpinner(rootView);

        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

//            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
//                  filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

                Bitmap resizedBitmap = BitmapUtils.createScaledBitmap(bitmap);

                ImageView imageView = (ImageView) getView().findViewById(R.id.gift_image);
                //imageView.setImageBitmap(bitmap);
                imageView.setImageBitmap(resizedBitmap);
                Log.i(LOG_TAG, "Saving image to cahce for giftId: " + gift.getGiftId());
                //mImageCache.updateBitmapToMemoryCache(gift.getGiftId() + "", new BitmapDrawable(imageView.getResources(), bitmap));
                mImageCache.updateBitmapToMemoryCache(gift.getGiftId() + "", new BitmapDrawable(imageView.getResources(), resizedBitmap));
            } catch (IOException e) {
                Log.d(LOG_TAG, "Exception: ", e);
                e.printStackTrace();
            }

//            // String picturePath contains the path of selected Image
//            ImageView imageView = (ImageView) getView().findViewById(R.id.gift_image);
//            imageView.setImageURI(selectedImage);
//            //imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
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
