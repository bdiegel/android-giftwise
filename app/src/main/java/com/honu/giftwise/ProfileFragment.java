package com.honu.giftwise;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.honu.giftwise.data.GiftwiseContract;
import com.honu.giftwise.data.Size;
import com.honu.giftwise.loaders.ContactEventDateLoader;
import com.honu.giftwise.loaders.GiftwiseProfileLoader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class ProfileFragment extends Fragment implements ContactEventDateLoader.ContactEventDateLoaderListener, GiftwiseProfileLoader.GiftwiseProfileLoaderListener {

    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    // adapters for color items amd sizes:
    private ColorAdapter mLikedColorsAdapter;
    private ColorAdapter mDislikedColorsAdapter;
    private SizeAdapter mSizeAdapter;

    // id of RawContact
    private long mRawContactId;

    private String mGiftwiseId;

    private long mContactId;


    public static ProfileFragment getInstance(String giftwiseId, long contactId) {
        ProfileFragment fragment = new ProfileFragment();

        // attach data to the fragment used to populate our fragment layouts
        Bundle args = new Bundle();
        args.putLong("contactId", contactId);
        args.putString("gwId", giftwiseId);

        // Set arguments to be fetched in the fragment onCreateView
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact_profile, container, false);
        ButterKnife.bind(this, rootView);

        // create color picker
        initColorPicker(rootView);

        // get args supplied when the fragment was instantiated by the CustomPagerAdapter
        Bundle args = getArguments();
        mContactId = args.getLong("contactId");
        mGiftwiseId = args.getString("gwId");

        // create adapters for liked and disliked colors
        Uri colorsUri = GiftwiseContract.ColorEntry.buildColorsForGiftwiseIdUri(mGiftwiseId);
        String selection = GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED + " = ?";
        Cursor likedColorsCursor = getActivity().getContentResolver().query(colorsUri, null, selection, new String[] { "1" }, null);
        mLikedColorsAdapter = new ColorAdapter(getActivity(), likedColorsCursor, 0);
        Cursor dislikedColorsCursor = getActivity().getContentResolver().query(colorsUri, null, selection, new String[] { "0" }, null);
        mDislikedColorsAdapter = new ColorAdapter(getActivity(), dislikedColorsCursor, 0);

        // create adapter for sizes
        Uri sizesUri = GiftwiseContract.SizeEntry.buildSizesForGiftwiseIdUri(mGiftwiseId);
        Cursor sizesCursor = getActivity().getContentResolver().query(sizesUri, null, null, null, null);
        mSizeAdapter = new SizeAdapter(getActivity(), sizesCursor, 0);

        if (savedInstanceState != null) {
            String birthday = savedInstanceState.getString("birthday");
            String anniversary = savedInstanceState.getString("anniversary");
            Log.d(LOG_TAG, "saved birthday: " + birthday);
            setBirthday(rootView, birthday);
            Log.d(LOG_TAG, "saved anniversary: " + anniversary);
            setAnniversary(rootView, anniversary);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        GiftwiseProfileLoader profileLoader = new GiftwiseProfileLoader(this.getContext(), this, mGiftwiseId);
        getLoaderManager().initLoader(GiftwiseProfileLoader.PROFILE_COLORS_LIKED_LOADER, null, profileLoader);
        getLoaderManager().initLoader(GiftwiseProfileLoader.PROFILE_COLORS_DISLIKED_LOADER, null, profileLoader);
        getLoaderManager().initLoader(GiftwiseProfileLoader.PROFILE_SIZES_LOADER, null, profileLoader);

        ContactEventDateLoader eventLoader = new ContactEventDateLoader(this.getContext(), this,(int) mContactId);
        getActivity().getSupportLoaderManager().initLoader(ContactEventDateLoader.PROFILE_BIRTHDAY_LOADER, null, eventLoader);
        getActivity().getSupportLoaderManager().initLoader(ContactEventDateLoader.PROFILE_ANNIVERSARY_LOADER, null, eventLoader);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView birthdayView = (TextView) getActivity().findViewById(R.id.contact_birthday_date);
        TextView anniversaryView = (TextView) getActivity().findViewById(R.id.contact_anniversary_date);
        outState.putString("birthday", birthdayView.getText().toString());
        outState.putString("anniversary", anniversaryView.getText().toString());
    }

    @OnClick(R.id.size_button)
    public void addSizeClicked() {
        Log.d(LOG_TAG, "Add size for: " + mGiftwiseId);

        // start activity to add/edit size details
        Intent intent = new Intent(getActivity(), EditSizeActivity.class);
        Size size = new Size(mGiftwiseId);
        intent.putExtra("size", size);

        startActivityForResult(intent, 1);
    }

    private void editSize(Size editSize) {
        Log.i(LOG_TAG, "Open size item: " + editSize.getSizeId());

        // start activity to add/edit gift idea
        Intent intent = new Intent(getActivity(), EditSizeActivity.class);
        intent.putExtra("size", editSize);

        startActivityForResult(intent, 1);
    }

    private void deleteSize(long sizeId) {
        Log.d(LOG_TAG, "Delete size id: " + sizeId);
        Uri uri = GiftwiseContract.SizeEntry.buildSizesForGiftwiseIdUri(mGiftwiseId);
        String where = GiftwiseContract.SizeEntry._ID  + " = ?";
        String[] whereArgs = new String[] {"" + sizeId};

        getActivity().getContentResolver().delete(uri, where, whereArgs);
    }

    private void initColorPicker(final View rootView) {
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

                dialog.setDialogSelectedListener(new ColorPickerDialog.DialogSelectionListener() {

                    @Override
                    public void onSelectionCompleted(int[] selectedColors) {
                        updateContentProvider(getColors(mLikedColorsAdapter), selectedColors, 1);
                    }

                    @Override
                    public void onSelectionCancelled() {
                        Log.d(LOG_TAG, "Selection CANCELED");
                    }
                });

                dialog.show(getActivity().getFragmentManager(), "color_picker");
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

                dialog.setDialogSelectedListener(new ColorPickerDialog.DialogSelectionListener() {

                    @Override
                    public void onSelectionCompleted(int[] selectedColors) {
                        updateContentProvider(getColors(mDislikedColorsAdapter), selectedColors, 0);
                    }

                    @Override
                    public void onSelectionCancelled() {
                        Log.i(LOG_TAG, "Selection CANCELED");
                    }
                });


                dialog.show(getActivity().getFragmentManager(), "color_picker_dislike");
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

        Uri colorsUri = GiftwiseContract.ColorEntry.buildColorsForGiftwiseIdUri(mGiftwiseId);

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
            getActivity().getContentResolver().delete(colorsUri, where, whereArgs);
        }

        // add NEW colors for contact to database
        if (addedColors.length > 0 ) {

            // create content values to insert NEW colors
            ContentValues[] allValues = new ContentValues[addedColors.length];
            for (int i = 0; i < addedColors.length; i++) {
                ContentValues values = new ContentValues();
                values.put(GiftwiseContract.ColorEntry.COLUMN_COLOR_GIFTWISE_ID, mGiftwiseId);
                values.put(GiftwiseContract.ColorEntry.COLUMN_COLOR_VALUE, addedColors[i]);
                values.put(GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED, liked);
                Log.i(LOG_TAG, "Values: " + values);
                allValues[i] = values;
            }

            // execute database insert
            if (allValues.length > 0) {
                getActivity().getContentResolver().bulkInsert(colorsUri, allValues);
            }
        }
    }

    private void addSizesToView(LinearLayout layout, SizeAdapter adapter) {
        layout.removeAllViews();

        final int adapterCount = adapter.getCount();
        for (int i = 0; i < adapterCount; i++) {
            View item = adapter.getView(i, null, null);
            layout.addView(item);
            item.setOnClickListener(new SizeClickListener(i));
            item.setOnLongClickListener(new SizeOnLongClickListener(i));
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
    public void onBirthdayDateLoaded(String date) {
        setBirthday(getView(), formatDateString(date));
    }

    @Override
    public void onAnniversaryDateLoaded(String date) {
        setAnniversary(getView(), formatDateString(date));
    }

    @Override
    public void onLikedColorsLoaded(Cursor cursor) {
        mLikedColorsAdapter.swapCursor(cursor);
        LinearLayout editLikedColors = (LinearLayout) getActivity().findViewById(R.id.colors_liked_list);
        addColorsFromAdapter(editLikedColors, mLikedColorsAdapter);
    }

    @Override
    public void onDislikedColorsLoaded(Cursor cursor) {
        mDislikedColorsAdapter.swapCursor(cursor);
        LinearLayout editDislikedColors = (LinearLayout) getActivity().findViewById(R.id.colors_dislike_list);
        addColorsFromAdapter(editDislikedColors, mDislikedColorsAdapter);
    }

    @Override
    public void onSizesLoaded(Cursor cursor) {
        mSizeAdapter.swapCursor(cursor);
        LinearLayout sizeLayout = (LinearLayout) getActivity().findViewById(R.id.sizes_list);
        addSizesToView(sizeLayout, mSizeAdapter);
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

    public class SizeClickListener implements View.OnClickListener {

        public int position;

        public SizeClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            // get cursor from the adapter
            Cursor cursor = mSizeAdapter.getCursor();

            // extract data from the selected item
            cursor.moveToPosition(position);
            Size size = Size.createFromCursor(cursor);

            // open edit activity
            editSize(size);
        }

        public int getPosition() {
            return position;
        }
    }

    public class SizeOnLongClickListener implements View.OnLongClickListener {

        public int position;

        public SizeOnLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View v) {

            // get cursor from the adapter
            Cursor cursor = mSizeAdapter.getCursor();

            // extract data from the selected item
            cursor.moveToPosition(position);
            Size size = Size.createFromCursor(cursor);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(size.getItem() + " - " + size.getSize());
            builder.setItems(R.array.size_context_menu, createListener(size));
            builder.show();

            return true;
        }

        public DialogInterface.OnClickListener createListener(final Size size) {

            return new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    switch(which) {
                        case 0:
                            editSize(size);
                            break;
                        case 1:
                            deleteSize(size.getSizeId());
                            break;
                        default:
                            break;
                    }
                }
            };
        }

    }

}
