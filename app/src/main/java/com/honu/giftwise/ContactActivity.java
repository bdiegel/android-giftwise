package com.honu.giftwise;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.honu.giftwise.view.SlidingTabLayout;

import java.util.Calendar;


public class ContactActivity extends ActionBarActivity {

    private static final String LOG_TAG = ContactActivity.class.getSimpleName();

    private CustomPagerAdapter mCustomPagerAdapter;
    private ViewPager mViewPager;
    private SlidingTabLayout mTabs;

    private String mContactName = "Contact";
    private long mRawContactId;

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

        // navigation icon selected (done)
        if (id == android.R.id.home) {
            Log.i(LOG_TAG, "navigation icon clicked");
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(this.getSupportFragmentManager(), "datePicker");
    }

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
                    return IdeasFragment.getInstance(position, mRawContactId, mContactName);
                case 1:
                    return ProfileFragment.getInstance(position);
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

    public static class ProfileFragment extends Fragment {

        public static ProfileFragment getInstance(int position) {
            ProfileFragment fragment = new ProfileFragment();
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
            View rootView = inflater.inflate(R.layout.fragment_contact_profile, container, false);

            initColorPicker(rootView);
//            initDateInput(rootView);
//            initClothingSpinner(rootView);
//            initSizeSpinner(rootView);

            // Get the arguments that was supplied when
            // the fragment was instantiated in the
            // CustomPagerAdapter
//            Bundle args = getArguments();
//            ((TextView) rootView.findViewById(R.id.textView)).setText("Page " + args.getInt("page_position"));

            return rootView;
        }

        private void initColorPicker(View rootView) {
            ImageView editColorsIV = (ImageView) rootView.findViewById(R.id.ic_edit_colors_like);
            editColorsIV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ColorPickerDialog dialog = ColorPickerDialog.newInstance(
                          R.string.color_picker_default_title,
                          getDefaultColors(),
                          0,
                          5,
                          ColorPickerDialog.SIZE_SMALL);
                          //Utils.isTablet(this)? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);

                    // get selected color value
                    dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                        @Override
                        public void onColorSelected(int color) {
                            int selectedColor = color;
                            Log.i(LOG_TAG, "Selected color: " + selectedColor);
                        }

                    });

                    dialog.show(getActivity().getFragmentManager(), "cal");
                }
            });
        }

        private int[] getDefaultColors() {
            int[] colors = null;

            String[] color_array = getActivity().getResources().
                  getStringArray(R.array.default_color_choice_values);

            if (color_array != null && color_array.length > 0) {
                colors = new int[color_array.length];
                for (int i = 0; i < color_array.length; i++) {
                    colors[i] = Color.parseColor(color_array[i]);
                }
            }

            return colors;
        }

//        private void initSizeSpinner(View rootView) {
//            Spinner spinner = (Spinner) rootView.findViewById(R.id.size_spinner);
//
//            // Create an ArrayAdapter using the string array and a default spinner layout
//            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
//                  R.array.size_choices, android.R.layout.simple_spinner_item);
//
//            // Specify the layout to use when the list of choices appears
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            spinner.setAdapter(adapter);
//        }

//        private void initClothingSpinner(View rootView) {
//            Spinner spinner = (Spinner) rootView.findViewById(R.id.item_spinner);
//
//            // Create an ArrayAdapter using the string array and a default spinner layout
//            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
//                  R.array.clothing_choices, android.R.layout.simple_spinner_item);
//
//            // Specify the layout to use when the list of choices appears
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            spinner.setAdapter(adapter);
//        }
//
//        private void initDateInput(View rootView) {
//            Spinner spinner = (Spinner) rootView.findViewById(R.id.special_date_spinner);
//
//            // Create an ArrayAdapter using the string array and a default spinner layout
//            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
//                  R.array.special_dates, android.R.layout.simple_spinner_item);
//
//            // Specify the layout to use when the list of choices appears
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            spinner.setAdapter(adapter);
//        }


    }

    public static class DatePickerFragment extends DialogFragment
          implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
//            int hour = c.get(Calendar.HOUR_OF_DAY);
//            int minute = c.get(Calendar.MINUTE);
            c.get(Calendar.YEAR);

            // Create a new instance of TimePickerDialog and return it
//            return new DatePickerDialog(getActivity(), this, hour, minute,
//                  DateFormat.is24HourFormat(getActivity()));
            return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR),c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) );
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // TODO: handle selection
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
