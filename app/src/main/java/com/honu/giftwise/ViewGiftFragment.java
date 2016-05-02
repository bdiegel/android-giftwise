package com.honu.giftwise;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftImageCache;

/**
 * Fragment for viewing Gift details
 */
public class ViewGiftFragment extends Fragment {

    private static final String LOG_TAG = ViewGiftFragment.class.getSimpleName();

    private Gift gift;

    /**
     * Create Fragment and setup the Bundle arguments
     */
    public static ViewGiftFragment getInstance(Gift gift, String contactName) {
        ViewGiftFragment fragment = new ViewGiftFragment();

        // Attach some data needed to populate our fragment layouts
        Bundle args = new Bundle();
        args.putParcelable("gift", gift);
        args.putString("contactName", contactName);

        // Set the arguments on the fragment that will be fetched by the edit activity
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_gift, container, false);

        GiftImageCache mImageCache = ((GiftwiseApplication) getActivity().getApplicationContext()).getGiftImageCache();

        // Get the Id of the raw contact
        Bundle args = getArguments();
        gift = args.getParcelable("gift");
        String mContactName = args.getString("contactName");

        // gift name
        TextView nameTxt = (TextView)rootView.findViewById(R.id.gift_name);
        nameTxt.setText(gift.getName());

        // recipient name
        TextView recipientTV = (TextView)rootView.findViewById(R.id.contact_display_name);
        recipientTV.setText(mContactName);

        // gift price
        TextView priceTxt = (TextView) rootView.findViewById(R.id.gift_price);
        priceTxt.setText(gift.getFormattedPrice());

        TextView urlTxt = (TextView)rootView.findViewById(R.id.gift_url);
        urlTxt.setText(gift.getUrl());
        Linkify.addLinks(urlTxt, Linkify.WEB_URLS);

        TextView notesTxt = (TextView)rootView.findViewById(R.id.gift_notes);
        notesTxt.setText(gift.getNotes());

        // set image from cache if exists
        ImageView imageView = (ImageView)rootView.findViewById(R.id.gift_image);
        BitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(gift.getGiftId() + "");
        if (bitmap != null ) {
            //imageView.setImageBitmap(bitmap);
            Log.i(LOG_TAG, "Bitmap loaded from cache for giftId: " + gift.getGiftId());
            imageView.setImageDrawable(bitmap);
        } else {
            Log.i(LOG_TAG, "No bitmap found in cache for giftId: " + gift.getGiftId());
        }

        // show options menu
        setHasOptionsMenu(true);

        return rootView;
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
}
