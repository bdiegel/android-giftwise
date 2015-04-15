package com.honu.giftwise;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.honu.giftwise.view.SlidingTabLayout;


public class ContactActivity extends ActionBarActivity {

    private static final String LOG_TAG = ContactActivity.class.getSimpleName();

    private static final int EDIT_CONTACT_RESULT = 100;

    // id of RawContact
    private long mRawContactId;

    // display name of RawContact
    private String mContactName = "";

    // Contact Id
    private long mContactId;


    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        Intent intent = getIntent();
        if (intent != null) {
            mContactName = intent.getStringExtra("name");
            mRawContactId = Long.parseLong(intent.getStringExtra("rawId"));
            mContactId = Long.parseLong(intent.getStringExtra("contactId"));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(mContactName);


        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);

        tabs.setSelectedIndicatorColors(R.color.selector);

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        ContactDetailPagerAdapter customPagerAdapter = new ContactDetailPagerAdapter(getSupportFragmentManager(), this, mContactName, mRawContactId, mContactId);
        mViewPager.setAdapter(customPagerAdapter);
        tabs.setViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact, menu);

        // Retrieve the share menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);

        // get the ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        // create a default intent for the share action
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(""));
        } else {
            Log.d(LOG_TAG, "Problem finding ShareActionProvider");
            //shareActionProvider = new ShareActionProvider(getActivity());
            //MenuItemCompat.setActionProvider(shareItem, shareActionProvider);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle action bar item clicks here
        int id = item.getItemId();

        if (id == R.id.action_edit_contact) {
            editContact();
        }

        // navigation icon selected (done)
        if (id == android.R.id.home) {
            Log.i(LOG_TAG, "navigation icon clicked");
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EDIT_CONTACT_RESULT && resultCode == Activity.RESULT_OK && null != data) {
            // not sure what we do with it ... nothing for now
            Log.d(LOG_TAG, "activity result data: " + data);
        }
    }

    private void editContact() {
        // use content uri to edit contacts:
        Uri mUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, mContactId);

        // Creates a new Intent to edit a contact
        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        editIntent.setDataAndType(mUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        editIntent.putExtra("finishActivityOnSaveCompleted", true);
        editIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(editIntent, EDIT_CONTACT_RESULT);
    }

    private Intent createShareIntent(String shareText) {
        Log.d(LOG_TAG, "Share gift item: " );

        Intent intent = new Intent(Intent.ACTION_SEND);
        // prevents Activity selected for sharing from being placed on app stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        return intent;
    }

    // call to update the share intent when content changes
    public void updateShareIntent(String shareText) {

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(shareText));
        }
    }
}
