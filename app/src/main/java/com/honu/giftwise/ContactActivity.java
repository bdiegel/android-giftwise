package com.honu.giftwise;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.honu.giftwise.view.SlidingTabLayout;


public class ContactActivity extends ActionBarActivity {

    private static final String LOG_TAG = ContactActivity.class.getSimpleName();

    private static final int EDIT_CONTACT_RESULT = 100;

    private CustomPagerAdapter mCustomPagerAdapter;
    private ViewPager mViewPager;
    private SlidingTabLayout mTabs;

    // id of RawContact
    private long mRawContactId;

    // display name of RawContact
    private String mContactName = "Contact";

    private long mContactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        if (savedInstanceState != null) {

        }

        Intent intent = getIntent();
        if (intent != null) {
            Log.i(LOG_TAG, "Activity with Intent: " + intent.toString());
            mContactName = intent.getStringExtra("name");
            //mRawContactId = intent.getLongExtra("rawId", -1);
            mRawContactId = Long.parseLong(intent.getStringExtra("rawId"));
            mContactId = Long.parseLong(intent.getStringExtra("contactId"));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            //getSupportActionBar().setDisplayShowTitleEnabled(true);
            //toolbar.setNavigationIcon(R.drawable.ic_action_accept);
            //toolbar.setTitle(mContactName);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(mContactName);


        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);

        mTabs.setSelectedIndicatorColors(R.color.selector);

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        mCustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mCustomPagerAdapter);
        mTabs.setViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

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

    /**
     * Displays tabs:
     *
     *   1. GIFT IDEAS - list of gift ideas
     *   2. PROFILE - personal info and preferences of contact
     */
    class CustomPagerAdapter extends FragmentPagerAdapter {

        Context mContext;

        String[] mTabTitles;

        public CustomPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mTabTitles = getResources().getStringArray(R.array.contact_tabs);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return IdeasFragment.getInstance(mRawContactId, mContactName);
                case 1:
                    return ProfileFragment.getInstance(mRawContactId, mContactId);
            }

            return  null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }

}
