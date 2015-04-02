package com.honu.giftwise;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {

    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    // id of RawContact
    private long mRawContactId;


    public static ProfileFragment getInstance(long rawContactId) {
        ProfileFragment fragment = new ProfileFragment();

        // attach data to the fragment used to populate our fragment layouts
        Bundle args = new Bundle();
        args.putLong("rawContactId", rawContactId);

        // Set arguments to be fetched in the fragment onCreateView
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

        // get args supplied when the fragment was instantiated by the CustomPagerAdapter
        Bundle args = getArguments();
        mRawContactId = args.getLong("rawContactId");

        List<Size> sizes = new ArrayList<Size>();
        sizes.add(new Size("Shirt", "Medium", "Banana Republic"));
        sizes.add(new Size("Jeans", "6L", "Wrap London"));
        sizes.add(new Size("Shirt", "XLarge", "Tommy Bahama; short-sleeve"));

        SizeAdapter adapter = new SizeAdapter(getActivity(), sizes);
//        mContactAdapter = new ContactAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView mListView = (ListView) rootView.findViewById(R.id.sizes_listview);
        mListView.setAdapter(adapter);

        return rootView;
    }

    private void initColorPicker(final View rootView) {
        final int[] noneSelected = new int[0];

        ViewGroup editLikedColors = (ViewGroup) rootView.findViewById(R.id.contact_colors_like);
        editLikedColors.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(
                      R.string.color_picker_liked_title,
                      getDefaultColors(),
                      noneSelected,
                      4,
                      ColorPickerDialog.SIZE_SMALL);
                //Utils.isTablet(this)? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);

                dialog.setDialogSelectedListener(new ColorPickerDialog.DialogSelectionListener() {

                    @Override
                    public void onSelectionCompleted(int[] colors) {
                        Log.i(LOG_TAG, "Selected colors: " + colors);
                        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.colors_liked_list);
                        layout.removeAllViews();
                        addColorsToView(layout, colors);
                    }

                    @Override
                    public void onSelectionCancelled() {
                        Log.i(LOG_TAG, "Selection CANCELED");
                    }
                });


                dialog.show(getActivity().getFragmentManager(), "color_picker");
                //dialog.show(getActivity().getSupportFragmentManager().beginTransaction(), "");
            }
        });

        ViewGroup editDislikedColors = (ViewGroup) rootView.findViewById(R.id.contact_colors_dislike);
        editDislikedColors.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(
                      R.string.color_picker_disliked_title,
                      getDefaultColors(),
                      noneSelected,
                      4,
                      ColorPickerDialog.SIZE_SMALL);
                //Utils.isTablet(this)? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);

                dialog.setDialogSelectedListener(new ColorPickerDialog.DialogSelectionListener() {

                    @Override
                    public void onSelectionCompleted(int[] colors) {
                        Log.i(LOG_TAG, "Selected colors disliked: " + colors);
                        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.colors_dislike_list);
                        layout.removeAllViews();
                        addColorsToView(layout, colors);
                    }

                    @Override
                    public void onSelectionCancelled() {
                        Log.i(LOG_TAG, "Selection CANCELED");
                    }
                });


                dialog.show(getActivity().getFragmentManager(), "color_picker_dislike");
                //dialog.show(getActivity().getSupportFragmentManager().beginTransaction(), "");
            }
        });

        // TODO: remove this test data later
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.colors_liked_list);
        addColorsToView(layout, new int[] { Color.BLUE, Color.RED, Color.GREEN });
    }

    private void addColorsToView(LinearLayout layout, int[] colors) {

        int dp24 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics());
        int dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp24, dp24);
        layoutParams.rightMargin = dp4;
        layoutParams.gravity = Gravity.BOTTOM;

        for (int color : colors) {
            Drawable circle = getResources().getDrawable(R.drawable.shape_circle);
            LayerDrawable coloredCircle = new LayerDrawable(new Drawable[]{circle});
            coloredCircle.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            ImageView imageView = new ImageView(getActivity());
            imageView.setImageDrawable(coloredCircle);
            imageView.setLayoutParams(layoutParams);
            layout.addView(imageView);
        }
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

//    public void showDatePickerDialog(View v) {
//        DialogFragment newFragment = new DatePickerFragment();
//        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
//    }
//
//    public static class DatePickerFragment extends DialogFragment
//          implements DatePickerDialog.OnDateSetListener {
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
////            // Use the current time as the default values for the picker
//            final Calendar c = Calendar.getInstance();
////            int hour = c.get(Calendar.HOUR_OF_DAY);
////            int minute = c.get(Calendar.MINUTE);
//            c.get(Calendar.YEAR);
//
//            // Create a new instance of TimePickerDialog and return it
////            return new DatePickerDialog(getActivity(), this, hour, minute,
////                  DateFormat.is24HourFormat(getActivity()));
//            return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR),c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) );
//        }
//
//        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//            // Do something with the time chosen by the user
//        }
//
//        @Override
//        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            // TODO: handle selection
//        }
//    }

    public class Size{
        private String item;
        private String size;
        private String notes;
        public Size(String item, String size, String notes) {
            this.item = item;
            this.size = size;
            this.notes = notes;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public class SizeAdapter extends ArrayAdapter<Size> {
        public SizeAdapter(Context context, List<Size> sizes) {
            super(context, 0, sizes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Size size = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_size, parent, false);
            }

            // Lookup view for data population
            TextView tvSize = (TextView) convertView.findViewById(R.id.list_item_with_size);
            TextView tvNotes = (TextView) convertView.findViewById(R.id.list_item_with_size_notes);

            // Populate the data into the template view using the data object
            tvSize.setText(size.item + " - " + size.size);
            tvNotes.setText(size.notes);

            // Return the completed view to render on screen
            return convertView;
        }
    }
}
