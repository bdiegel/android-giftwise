package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.data.BitmapUtils;
import com.honu.giftwise.data.ContactImageCache;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftImageCache;
import com.honu.giftwise.loaders.ContactEventDateLoader;
import com.honu.giftwise.tasks.ContactBitmapTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Fragment for viewing Gift details
 *
 * Uses a Loader to display event dates (birthday, anniversary) for Contact.
 */
public class ViewGiftFragment extends Fragment implements ContactEventDateLoader.ContactEventDateLoaderListener {

    private static final String LOG_TAG = ViewGiftFragment.class.getSimpleName();

    private Gift mGift;

    private String mContactName;

    private int mContactId;

    private GiftImageCache mGiftImageCache;

    private ContactImageCache mContactImageCache;

    @Bind(R.id.gift_name) TextView mNameTxt;
    @Bind(R.id.contact_display_name) TextView mRecipientTV;
    @Bind(R.id.gift_price) TextView mPriceTxt;
    @Bind(R.id.gift_notes) TextView mNotesTxt;
    @Bind(R.id.gift_url_container) ViewGroup mUrlViewGroup;
    @Bind(R.id.gift_url) TextView mUrlTxt;
    @Bind(R.id.gift_image) ImageView mGiftImageView;
    @Bind(R.id.ic_contact_bitmap) ImageView mContactImageView;
    @Bind(R.id.contact_birthday_date) TextView mBirthdayTxt;
    @Bind(R.id.contact_anniversary) ViewGroup mAnniversaryViewGroup;
    @Bind(R.id.contact_anniversary_date) TextView mAnniversaryTxt;

    /**
     * Create Fragment and setup the Bundle arguments
     */
    public static ViewGiftFragment getInstance(Gift gift, String contactName, int contactId) {
        ViewGiftFragment fragment = new ViewGiftFragment();

        // Attach some data needed to populate our fragment layouts
        Bundle args = new Bundle();
        args.putParcelable("mGift", gift);
        args.putString("contactName", contactName);
        args.putInt("contactId", contactId);

        // Set the arguments on the fragment that will be fetched by the edit activity
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_gift, container, false);
        ButterKnife.bind(this, rootView);

        mContactImageCache = ((GiftwiseApplication)getActivity().getApplicationContext()).getContactImageCache();

        mGiftImageCache = ((GiftwiseApplication) getActivity().getApplicationContext()).getGiftImageCache();

        // Get the Id of the raw contact
        Bundle args = getArguments();
        mGift = args.getParcelable("mGift");
        mContactName = args.getString("contactName");
        mContactId = args.getInt("contactId");

        initViews();
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "initializing loaders");
        ContactEventDateLoader eventLoader = new ContactEventDateLoader(this.getContext(), this, mContactId);
        getActivity().getSupportLoaderManager().initLoader(ContactEventDateLoader.PROFILE_BIRTHDAY_LOADER, null, eventLoader);
        getActivity().getSupportLoaderManager().initLoader(ContactEventDateLoader.PROFILE_ANNIVERSARY_LOADER, null, eventLoader);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate the fragment menu
        inflater.inflate(R.menu.menu_view_gift_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_share:
                shareGift();
                return true;
            case R.id.action_edit:
                editGift();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        mNameTxt.setText(mGift.getName());
        mRecipientTV.setText(mContactName);
        mPriceTxt.setText(mGift.getFormattedPrice());

        // notes about Gift
        String notes = mGift.getNotes();
        if (TextUtils.isEmpty(notes)) {
            mNotesTxt.setVisibility(View.GONE);
        } else {
            mNotesTxt.setVisibility(View.VISIBLE);
            mNotesTxt.setText(notes);
        }

        // website URL
        String url = mGift.getUrl();
        if (TextUtils.isEmpty(url)) {
            mUrlViewGroup.setVisibility(View.GONE);
        } else {
            mUrlViewGroup.setVisibility(View.VISIBLE);
            mUrlTxt.setText(url);
        }

        // set image from cache if exists
        BitmapDrawable bitmap = mGiftImageCache.getBitmapFromMemCache(mGift.getGiftId() + "");
        if (bitmap != null ) {
            Log.i(LOG_TAG, "Bitmap loaded from cache for giftId: " + mGift.getGiftId());
            mGiftImageView.setImageDrawable(BitmapUtils.getRoundedBitmapDrawable(getResources(), bitmap.getBitmap()));
        } else {
            Log.i(LOG_TAG, "No bitmap found in cache for giftId: " + mGift.getGiftId());
        }

        // load contact avatar image
        loadContactBitmap(getActivity().getContentResolver(), mContactId, mContactImageView);
    }

    private void editGift() {
        Log.i(LOG_TAG, "Open GiftId: " + mGift.getGiftId());

        // start activity to add/edit mGift idea
        Intent intent = new Intent(getActivity(), EditGiftActivity.class);
        intent.putExtra("mGift", mGift);

        startActivityForResult(intent, 1);
    }

    private void shareGift() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getTextDescription());
        startActivity(Intent.createChooser(sharingIntent, "Share mGift details using"));
    }

    private String getTextDescription() {
        String priceTxt = "";
        if (mGift.getPrice() > 0)
            priceTxt = mGift.getFormattedPrice();

        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("%s %s\n", mGift.getName(), priceTxt));

        if (!TextUtils.isEmpty(mGift.getNotes()))
            buffer.append(String.format("Notes: %s\n", mGift.getNotes()));
        if (!TextUtils.isEmpty(mGift.getUrl()))
            buffer.append(mGift.getUrl());

        return buffer.toString();
    }

    /**
     * Load thumbnail photo from cache if found. Otherwise, use place-holder image.
     * Start an async task to load image from the contacts content provider. If an
     * image is found, replace the place-holder and cache the image.
     */
    public void loadContactBitmap(ContentResolver contentResolver, int resId, ImageView imageView) {
        final String imageKey = String.valueOf(resId);

        final RoundedBitmapDrawable bitmap = mContactImageCache.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
            // set temporary placeholder image
            imageView.setImageDrawable(mContactImageCache.getPlaceholderImage());

            // start background task to load image from contacts provider
            ContactBitmapTask task = new ContactBitmapTask(imageView);
            task.execute(resId);
        }
    }

    @Override
    public void onBirthdayDateLoaded(String date) {
        mBirthdayTxt.setText(formatDateString(date));
    }

    @Override
    public void onAnniversaryDateLoaded(String date) {
        String anniversary = formatDateString(date);

        if (TextUtils.isEmpty(anniversary)) {
            mAnniversaryViewGroup.setVisibility(View.GONE);
        } else {
            mAnniversaryViewGroup.setVisibility(View.VISIBLE);
            mAnniversaryTxt.setText(anniversary);
        }
    }

    private String formatDateString(String date) {
        if (TextUtils.isEmpty(date)) return date;

        try {
            DateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date d = parseFormat.parse(date);
            DateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy");
            return displayFormat.format(d);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "date parse failed", e);
            return date;
        }
    }
}
