package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import butterknife.OnClick;

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
        args.putParcelable("gift", gift);
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

        Bundle args = getArguments();
        mGift = args.getParcelable("gift");
        mContactName = args.getString("contactName");
        mContactId = args.getInt("contactId");

        initViews(rootView);
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

    private void initViews(View rootView) {
        TextView nameTxt = ButterKnife.findById(rootView, R.id.gift_name);
        TextView recipientTV = ButterKnife.findById(rootView, R.id.contact_display_name);
        TextView priceTxt = ButterKnife.findById(rootView, R.id.gift_price);
        TextView notesTxt = ButterKnife.findById(rootView, R.id.gift_notes);
        ViewGroup urlViewGroup = ButterKnife.findById(rootView, R.id.gift_url_container);
        TextView urlTxt = ButterKnife.findById(rootView, R.id.gift_url);
        ImageView giftImageView = ButterKnife.findById(rootView, R.id.gift_image);
        ImageView contactImageView = ButterKnife.findById(rootView, R.id.ic_contact_bitmap);

        nameTxt.setText(mGift.getName());
        recipientTV.setText(mContactName);
        priceTxt.setText(mGift.getFormattedPrice());

        // notes about Gift
        String notes = mGift.getNotes();
        if (TextUtils.isEmpty(notes)) {
            notesTxt.setVisibility(View.GONE);
        } else {
            notesTxt.setVisibility(View.VISIBLE);
            notesTxt.setText(notes);
        }

        // website URL
        String url = mGift.getUrl();
        if (TextUtils.isEmpty(url)) {
            urlViewGroup.setVisibility(View.GONE);
        } else {
            urlViewGroup.setVisibility(View.VISIBLE);
            urlTxt.setText(trimUrl(url));
        }

        // set image from cache if exists
        BitmapDrawable bitmap = mGiftImageCache.getBitmapFromMemCache(mGift.getGiftId() + "");
        if (bitmap != null ) {
            Log.d(LOG_TAG, "Bitmap loaded from cache for giftId: " + mGift.getGiftId());
            giftImageView.setImageDrawable(BitmapUtils.getRoundedBitmapDrawable(getResources(), bitmap.getBitmap()));
        } else {
            Log.d(LOG_TAG, "No bitmap found in cache for giftId: " + mGift.getGiftId());
        }

        // load contact avatar image
        loadContactBitmap(getActivity().getContentResolver(), mContactId, contactImageView);
    }

    private void editGift() {
        Intent intent = new Intent(getActivity(), EditGiftActivity.class);
        intent.putExtra("gift", mGift);
        startActivityForResult(intent, 1);
    }

    private void shareGift() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getTextDescription());
        startActivity(Intent.createChooser(sharingIntent, "Share gift details using"));
    }

    @OnClick(R.id.open_url_button)
    public void viewUrl() {
        String url = mGift.getUrl();

        if (TextUtils.isEmpty(url)) {
            return;
        }

        // add prefix if necessary
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        Uri webpage = Uri.parse(url);

        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private String trimUrl(String url) {
        String trimmedUrl = url;

        if (!TextUtils.isEmpty(url)) {
            trimmedUrl = Uri.parse(url).getHost();
        }

        if (TextUtils.isEmpty(trimmedUrl)) {
            trimmedUrl = url;
        }

        return trimmedUrl;
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
    public void loadContactBitmap(ContentResolver contentResolver, long resId, ImageView imageView) {
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
