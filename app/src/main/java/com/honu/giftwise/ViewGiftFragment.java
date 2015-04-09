package com.honu.giftwise;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
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
import com.honu.giftwise.view.FloatingActionButton;

/**
 * Fragment for viewing Gift details
 */
public class ViewGiftFragment extends Fragment {

    private static final String LOG_TAG = ViewGiftFragment.class.getSimpleName();

    private Gift gift;

    private String mContactName;

    private GiftImageCache mImageCache;

    private ShareActionProvider mShareActionProvider;

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

        mImageCache = ((GiftwiseApplication)getActivity().getApplicationContext()).getGiftImageCache();

        // Get the Id of the raw contact
        Bundle args = getArguments();
        gift = args.getParcelable("gift");
        mContactName = args.getString("contactName");

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

        // handler for the FAB edit button
        FloatingActionButton editButton = (FloatingActionButton) rootView.findViewById(R.id.edit_gift_fab);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((MainActivity)getActivity()).addContact();
                editGift();
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate the fragment menu
        inflater.inflate(R.menu.menu_view_gift_fragment, menu);

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
//            case R.id.action_browse:
//                openUrl();
//                return true;
//            case R.id.action_edit:
//                editGift();
//                return true;
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
        TextView nameEdit = (TextView)rootView.findViewById(R.id.gift_name);
        TextView priceEdit = (TextView) rootView.findViewById(R.id.gift_price);
        TextView urlEdit = (TextView)rootView.findViewById(R.id.gift_url);
        TextView notesEdit = (TextView)rootView.findViewById(R.id.gift_notes);

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
}
