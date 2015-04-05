package com.honu.giftwise;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.honu.giftwise.data.GiftwiseContract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    // data loaders for colors and sizes:
    private static final int PROFILE_COLORS_LIKED_LOADER = 20;
    private static final int PROFILE_COLORS_DISLIKED_LOADER = 21;
    private static final int PROFILE_SIZES_LOADER = 22;

    // adapters for color items
    private ColorAdapter mLikedColorsAdapter;
    private ColorAdapter mDislikedColorsAdapter;

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
        // inflate the layout
        View rootView = inflater.inflate(R.layout.fragment_contact_profile, container, false);

        // create color picker
        initColorPicker(rootView);

        // get args supplied when the fragment was instantiated by the CustomPagerAdapter
        Bundle args = getArguments();
        mRawContactId = args.getLong("rawContactId");

        // create adapters for liked and disliked colors
        Uri colorsUri = GiftwiseContract.ColorEntry.buildColorsForRawContactUri(mRawContactId);
        String selection = GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED + " = ?";
        Cursor likedColorsCursor = getActivity().getContentResolver().query(colorsUri, null, selection, new String[] { "1" }, null);
        mLikedColorsAdapter = new ColorAdapter(getActivity(), likedColorsCursor, 0);
        Cursor dislikedColorsCursor = getActivity().getContentResolver().query(colorsUri, null, selection, new String[] { "0" }, null);
        mDislikedColorsAdapter = new ColorAdapter(getActivity(), dislikedColorsCursor, 0);

        // TODO: remove fake data for sizes
        List<Size> sizes = new ArrayList<Size>();
        sizes.add(new Size("Shirt", "Medium", "Banana Republic"));
        sizes.add(new Size("Jeans", "6L", "Wrap London"));
        sizes.add(new Size("Shirt", "XLarge", "Tommy Bahama; short-sleeve"));

        SizeAdapter adapter = new SizeAdapter(getActivity(), sizes);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.sizes_list);
        addSizesToView(linearLayout, adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PROFILE_COLORS_LIKED_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_COLORS_DISLIKED_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_SIZES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void initColorPicker(final View rootView) {
        final int[] noneSelected = new int[0];

        // add click listener for liked colors:
        ViewGroup editLikedColors = (ViewGroup) rootView.findViewById(R.id.contact_colors_like);
        editLikedColors.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(
                      R.string.color_picker_liked_title,
                      getDefaultColors(),
                      getColors(mLikedColorsAdapter),
                      4,
                      ColorPickerDialog.SIZE_SMALL);
                //Utils.isTablet(this)? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);

                dialog.setDialogSelectedListener(new ColorPickerDialog.DialogSelectionListener() {

                    @Override
                    public void onSelectionCompleted(int[] selectedColors) {
                        Log.i(LOG_TAG, "Selected colors: " + selectedColors);
                        updateContentProvider(getColors(mLikedColorsAdapter), selectedColors, 1);
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

        // add click listener for disliked colors
        ViewGroup editDislikedColors = (ViewGroup) rootView.findViewById(R.id.contact_colors_dislike);
        editDislikedColors.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(
                      R.string.color_picker_disliked_title,
                      getDefaultColors(),
                      getColors(mDislikedColorsAdapter),
                      4,
                      ColorPickerDialog.SIZE_SMALL);
                //Utils.isTablet(this)? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);

                dialog.setDialogSelectedListener(new ColorPickerDialog.DialogSelectionListener() {

                    @Override
                    public void onSelectionCompleted(int[] selectedColors) {
                        Log.i(LOG_TAG, "Selected colors disliked: " + selectedColors);
                        updateContentProvider(getColors(mDislikedColorsAdapter), selectedColors, 0);
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

    }

    private int[] getColors(ColorAdapter adapter) {
        Cursor cursor = adapter.getCursor();
        int countColors = adapter.getCount();
        int[] colors = new int[countColors];

        for (int i = 0; i < countColors; i++) {
            cursor.moveToPosition(i);
            int color = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.ColorEntry.COLUMN_COLOR_VALUE));
            colors[i] = color;
        }

        return colors;
    }

    private int getColorIdFromAdapter(int color, int liked) {

        // get cursor from selected adapter
        ColorAdapter adapter = (liked == 1) ? mLikedColorsAdapter : mDislikedColorsAdapter;
        Cursor cursor = adapter.getCursor();
        int countColors = adapter.getCount();

        // find color
        for (int i = 0; i < countColors; i++) {
            cursor.moveToPosition(i);
            int c = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.ColorEntry.COLUMN_COLOR_VALUE));
            if (c == color) {
                return cursor.getInt(cursor.getColumnIndex(GiftwiseContract.ColorEntry._ID));
            }
        }
        return -1;
    }


    private void updateContentProvider(int[] oldColors, int[] newColors, int liked){

        // do some set algebra to determine new and deleted colors
        Set<Integer> oldSet = new HashSet<Integer> (Ints.asList(oldColors));
        Set<Integer> newSet = new HashSet<Integer> (Ints.asList(newColors));
        Set<Integer> intersection = Sets.intersection(oldSet, newSet);
        Set<Integer> deleted = Sets.difference(oldSet, intersection);
        Set<Integer> added = Sets.difference(newSet, intersection);

        // colors to add and delete
        int[] deletedColors = Ints.toArray(deleted);
        int[] addedColors = Ints.toArray(added);

        // remove DELETE colors from database for contact
        if (deletedColors.length > 0 ) {

            // create where clause for delete
            String where = GiftwiseContract.ColorEntry._ID  + " IN (" + new String(new char[deletedColors.length]).replace("\0", "?,") + "?)";
            String[] whereArgs = new String[deletedColors.length];

            // build whereArgs from color ids
            for (int i = 0; i < deletedColors.length; i++) {
                whereArgs[i] = "" + getColorIdFromAdapter(deletedColors[i], liked);
            }

            // execute delete query
            Uri colorsUri = GiftwiseContract.ColorEntry.buildColorsForRawContactUri(mRawContactId);
            getActivity().getContentResolver().delete(colorsUri, where, whereArgs);
        }

        // add NEW colors for contact to database
        if (deletedColors.length > 0 ) {

            // create content values to insert NEW colors
            ContentValues[] allValues = new ContentValues[addedColors.length];
            for (int i = 0; i < addedColors.length; i++) {
                ContentValues values = new ContentValues();
                values.put(GiftwiseContract.ColorEntry.COLUMN_COLOR_RAWCONTACT_ID, mRawContactId);
                values.put(GiftwiseContract.ColorEntry.COLUMN_COLOR_VALUE, addedColors[i]);
                values.put(GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED, liked);
                Log.i(LOG_TAG, "Values: " + values);
                allValues[i] = values;
            }

            // execute database insert
            if (allValues.length > 0) {
                getActivity().getContentResolver().bulkInsert(GiftwiseContract.ColorEntry.COLOR_URI, allValues);
            }
        }
    }

    private void addSizesToView(LinearLayout layout, SizeAdapter adapter) {

        final int adapterCount = adapter.getCount();

        for (int i = 0; i < adapterCount; i++) {
            View item = adapter.getView(i, null, null);
            layout.addView(item);
        }
    }

    private void addColorsFromAdapter(LinearLayout layout, ColorAdapter adapter) {
        layout.removeAllViews();

        final int adapterCount = adapter.getCount();
        for (int i = 0; i < adapterCount; i++) {
            View item = adapter.getView(i, null, null);
            layout.addView(item);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");

        switch (id) {
            case PROFILE_COLORS_LIKED_LOADER: {
                String selection = GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED + " = ? ";
                String[] selectionArgs = new String[] { "1" };
                Uri uri = GiftwiseContract.ColorEntry.buildColorsForRawContactUri(mRawContactId);
                return new CursorLoader(getActivity(), uri, null, selection, selectionArgs, null);
            }
            case PROFILE_COLORS_DISLIKED_LOADER: {
                String selection = GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED + " = ? ";
                String[] selectionArgs = new String[] { "0" };
                Uri uri = GiftwiseContract.ColorEntry.buildColorsForRawContactUri(mRawContactId);
                return new CursorLoader(getActivity(), uri, null, selection, selectionArgs, null);
            }
            case PROFILE_SIZES_LOADER: {
                Uri uri = GiftwiseContract.SizeEntry.buildSizesForRawContactUri(mRawContactId);
                return new CursorLoader(getActivity(), uri, null, null, null, null);
            }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished: " + loader.getId());

        switch (loader.getId()) {
            case PROFILE_COLORS_DISLIKED_LOADER:
                mDislikedColorsAdapter.swapCursor(data);
                LinearLayout editDislikedColors = (LinearLayout) getActivity().findViewById(R.id.colors_dislike_list);
                addColorsFromAdapter(editDislikedColors, mDislikedColorsAdapter);
                break;
            case PROFILE_COLORS_LIKED_LOADER:
                mLikedColorsAdapter.swapCursor(data);
                LinearLayout editLikedColors = (LinearLayout) getActivity().findViewById(R.id.colors_liked_list);
                addColorsFromAdapter(editLikedColors, mLikedColorsAdapter);
                break;
            case PROFILE_SIZES_LOADER:
                break;
            default:
                return;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
