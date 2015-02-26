package com.honu.giftwise;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honu.giftwise.view.*;


public class ContactActivity extends ActionBarActivity {

    private CustomPagerAdapter mCustomPagerAdapter;
    private ViewPager mViewPager;
    private SlidingTabLayout mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_contact);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                  .add(R.id.container, new PlaceholderFragment())
//                  .commit();
//        }
        setContentView(R.layout.activity_contact);


        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);

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

        return super.onOptionsItemSelected(item);
    }

    class CustomPagerAdapter extends FragmentPagerAdapter {

        Context mContext;

        String[] mTabTitles;

        public CustomPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mTabTitles = getResources().getStringArray(R.array.contatct_tabs);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return DemoFragment.getInstance(position);
                case 1:
                    return DemoFragment.getInstance(position);
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

    public static class DemoFragment extends Fragment {

        public static DemoFragment getInstance(int position) {
            DemoFragment fragment = new DemoFragment();
            // Attach some data to the fragment
            // that we'll use to populate our fragment layouts
            Bundle args = new Bundle();
            args.putInt("page_position", position + 1);

            // Set the arguments on the fragment
            // that will be fetched in the
            // DemoFragment@onCreateView
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout resource that'll be returned
            View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

            // Get the arguments that was supplied when
            // the fragment was instantiated in the
            // CustomPagerAdapter
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(R.id.textView)).setText("Page " + args.getInt("page_position"));

            return rootView;
        }
    }

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
//            return rootView;
//        }
//    }
}
