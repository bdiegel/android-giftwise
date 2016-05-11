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

/**
 * Fragment for viewing Gift details
 *
 * Uses a Loader to display event dates (birthday, anniversary) for Contact.
 */
public class ViewGiftFragment extends Fragment implements ContactEventDateLoader.ContactEventDateLoaderListener {

    private static final String LOG_TAG = ViewGiftFragment.class.getSimpleName();

    private Gift gift;

    private int mContactId;

    private ContactImageCache mImageCache;

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

        mImageCache = ((GiftwiseApplication)getActivity().getApplicationContext()).getContactImageCache();

        GiftImageCache mImageCache = ((GiftwiseApplication) getActivity().getApplicationContext()).getGiftImageCache();

        // Get the Id of the raw contact
        Bundle args = getArguments();
        gift = args.getParcelable("gift");
        String mContactName = args.getString("contactName");
        mContactId = args.getInt("contactId");

        // gift name
        TextView nameTxt = (TextView)rootView.findViewById(R.id.gift_name);
        nameTxt.setText(gift.getName());

        // recipient name
        TextView recipientTV = (TextView)rootView.findViewById(R.id.contact_display_name);
        recipientTV.setText(mContactName);

        // gift price
        TextView priceTxt = (TextView) rootView.findViewById(R.id.gift_price);
        priceTxt.setText(gift.getFormattedPrice());

        // notes about gift
        TextView notesTxt = (TextView)rootView.findViewById(R.id.gift_notes);
        String notes = gift.getNotes();
        if (TextUtils.isEmpty(notes)) {
            notesTxt.setVisibility(View.GONE);
        } else {
            notesTxt.setVisibility(View.VISIBLE);
            notesTxt.setText(notes);
        }

        // website URL
        ViewGroup urlViewGroup = (ViewGroup) rootView.findViewById(R.id.gift_url_container);
        TextView urlTxt = (TextView)rootView.findViewById(R.id.gift_url);
        String url = gift.getUrl();
        //Linkify.addLinks(urlTxt, Linkify.WEB_URLS);
        if (TextUtils.isEmpty(url)) {
            urlViewGroup.setVisibility(View.GONE);
        } else {
            urlViewGroup.setVisibility(View.VISIBLE);
            urlTxt.setText(url);
        }

        // set image from cache if exists
        ImageView imageView = (ImageView)rootView.findViewById(R.id.gift_image);
        BitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(gift.getGiftId() + "");
        if (bitmap != null ) {
            //imageView.setImageBitmap(bitmap);
            Log.i(LOG_TAG, "Bitmap loaded from cache for giftId: " + gift.getGiftId());
            //imageView.setImageDrawable(bitmap);
            imageView.setImageDrawable(BitmapUtils.getRoundedBitmapDrawable(getResources(), bitmap.getBitmap()));
        } else {
            Log.i(LOG_TAG, "No bitmap found in cache for giftId: " + gift.getGiftId());
        }

        ImageView contactImageView = (ImageView)rootView.findViewById(R.id.ic_contact_bitmap);
        loadBitmap(getActivity().getContentResolver(), mContactId, contactImageView);

        // show options menu
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
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_share:
                shareGift();
                return true;
//            case R.id.action_browse:
//                openUrl();
//                return true;
            case R.id.action_edit:
                editGift();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editGift() {
        Log.i(LOG_TAG, "Open GiftId: " + gift.getGiftId());

        // start activity to add/edit gift idea
        Intent intent = new Intent(getActivity(), EditGiftActivity.class);
        intent.putExtra("gift", gift);

        startActivityForResult(intent, 1);
    }

    private void shareGift() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getTextDescription());
        startActivity(Intent.createChooser(sharingIntent, "Share gift details using"));
    }

    private String getTextDescription() {
        String priceTxt = "";
        if (gift.getPrice() > 0)
            priceTxt = gift.getFormattedPrice();

        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("%s %s\n", gift.getName(), priceTxt));

        if (!TextUtils.isEmpty(gift.getNotes()))
            buffer.append(String.format("Notes: %s\n", gift.getNotes()));
        if (!TextUtils.isEmpty(gift.getUrl()))
            buffer.append(gift.getUrl());

        return buffer.toString();
    }

    /**
     * Load thumbnail photo from cache if found. Otherwise, use place-holder image.
     * Start an async task to load image from the contacts content provider. If an
     * image is found, replace the place-holder and cache the image.
     */
    public void loadBitmap(ContentResolver contentResolver, int resId, ImageView imageView) {
        final String imageKey = String.valueOf(resId);

        final RoundedBitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
            // set temporary placeholder image
            imageView.setImageDrawable(mImageCache.getPlaceholderImage());

            // start background task to load image from contacts provider
            ContactBitmapTask task = new ContactBitmapTask(imageView);
            task.execute(resId);
        }
    }

    @Override
    public void onBirthdayDateLoaded(String date) {
        setBirthday(getView(), formatDateString(date));
    }

    @Override
    public void onAnniversaryDateLoaded(String date) {
        setAnniversary(getView(), formatDateString(date));
    }

    private void setBirthday(View rootView, String birthday) {
        TextView view = (TextView) rootView.findViewById(R.id.contact_birthday_date);
        view.setText(birthday);
    }

    private void setAnniversary(View rootView, String anniversary) {
        ViewGroup layout = (ViewGroup) rootView.findViewById(R.id.contact_anniversary);

        if (TextUtils.isEmpty(anniversary)) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
            TextView view = (TextView) rootView.findViewById(R.id.contact_anniversary_date);
            view.setText(anniversary);
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
