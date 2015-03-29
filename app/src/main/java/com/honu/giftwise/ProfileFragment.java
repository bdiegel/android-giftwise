package com.honu.giftwise;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;


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

        return rootView;
    }

    private void initColorPicker(View rootView) {
        final int[] noneSelected = new int[0];
        ImageView editColorsIV = (ImageView) rootView.findViewById(R.id.ic_edit_colors_like);
        editColorsIV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(
                //CustomColorPickerDialog dialog = CustomColorPickerDialog.newInstance(
                      R.string.color_picker_default_title,
                      getDefaultColors(),
                      noneSelected,
                      4,
                      ColorPickerDialog.SIZE_SMALL);
                      //Utils.isTablet(this)? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);

                // get selected color value
                dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                    @Override
                    public void onColorSelected(int color, boolean isSelected) {
                        int selectedColor = color;
                        Log.i(LOG_TAG, "Color: " + selectedColor + " isSelected: " + isSelected);
                    }

                });

                dialog.setDialogSelectedListener(new ColorPickerDialog.DialogSelectionListener() {
                    @Override
                    public void onSelectionCompleted(int[] colors) {
                        Log.i(LOG_TAG, "Selected colors: " + colors);
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
}
