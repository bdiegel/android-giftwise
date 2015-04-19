package com.honu.giftwise;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.data.GiftwiseContract;
import com.honu.giftwise.data.Size;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    // data loaders for colors and sizes:
    private static final int PROFILE_COLORS_LIKED_LOADER = 20;
    private static final int PROFILE_COLORS_DISLIKED_LOADER = 21;
    private static final int PROFILE_SIZES_LOADER = 22;

    // data loaders for special dates
    private static final int PROFILE_BIRTHDAY_LOADER = 30;
    private static final int PROFILE_ANNIVERSARY_LOADER = 31;

    // adapters for color items amd sizes:
    private ColorAdapter mLikedColorsAdapter;
    private ColorAdapter mDislikedColorsAdapter;
    private SizeAdapter mSizeAdapter;

    // id of RawContact
    private long mRawContactId;

    private long mContactId;


    public static ProfileFragment getInstance(long rawContactId, long contactId) {
        ProfileFragment fragment = new ProfileFragment();

        // attach data to the fragment used to populate our fragment layouts
        Bundle args = new Bundle();
        args.putLong("rawContactId", rawContactId);
        args.putLong("contactId", contactId);

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
        mContactId = args.getLong("contactId");

        // create adapters for liked and disliked colors
        Uri colorsUri = GiftwiseContract.ColorEntry.buildColorsForRawContactUri(mRawContactId);
        String selection = GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED + " = ?";
        Cursor likedColorsCursor = getActivity().getContentResolver().query(colorsUri, null, selection, new String[] { "1" }, null);
        mLikedColorsAdapter = new ColorAdapter(getActivity(), likedColorsCursor, 0);
        Cursor dislikedColorsCursor = getActivity().getContentResolver().query(colorsUri, null, selection, new String[] { "0" }, null);
        mDislikedColorsAdapter = new ColorAdapter(getActivity(), dislikedColorsCursor, 0);

        // create adapter for sizes
        Uri sizesUri = GiftwiseContract.SizeEntry.buildSizesForRawContactUri(mRawContactId);
        Cursor sizesCursor = getActivity().getContentResolver().query(sizesUri, null, null, null, null);
        mSizeAdapter = new SizeAdapter(getActivity(), sizesCursor, 0);

        // add listener to add size button
        Button addSizeButton = (Button) rootView.findViewById(R.id.size_button);
        addSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSize();
            }
        });

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
        getLoaderManager().initLoader(PROFILE_COLORS_LIKED_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_COLORS_DISLIKED_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_SIZES_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_BIRTHDAY_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_ANNIVERSARY_LOADER, null, this);
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

    private void addSize() {
        Log.i(LOG_TAG, "Add size for: " + mRawContactId);

        // start activity to add/edit size details
        Intent intent = new Intent(getActivity(), EditSizeActivity.class);
        Size size = new Size(mRawContactId);
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
        Log.i(LOG_TAG, "Delete size id: " + sizeId);
        //Uri uri = GiftwiseContract.SizeEntry.buildSizeUri(sizeId);
        Uri uri = GiftwiseContract.SizeEntry.buildSizesForRawContactUri(mRawContactId);
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
                //Utils.isTablet(this)? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);

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

        Uri colorsUri = GiftwiseContract.ColorEntry.buildColorsForRawContactUri(mRawContactId);

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
                values.put(GiftwiseContract.ColorEntry.COLUMN_COLOR_RAWCONTACT_ID, mRawContactId);
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
            case PROFILE_BIRTHDAY_LOADER: {
                return ContactsUtils.getContactEventDateCurosrLoader(getActivity(), mContactId, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);
            }
            case PROFILE_ANNIVERSARY_LOADER: {
                return ContactsUtils.getContactEventDateCurosrLoader(getActivity(), mContactId, ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY);
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
                mSizeAdapter.swapCursor(data);
                LinearLayout sizeLayout = (LinearLayout) getActivity().findViewById(R.id.sizes_list);
                addSizesToView(sizeLayout, mSizeAdapter);
                break;
            case PROFILE_BIRTHDAY_LOADER:
                setEventDate(data);
                break;
            case PROFILE_ANNIVERSARY_LOADER:
                if (data == null || data.getCount() == 0) {
                    hideAnniversaryView();
                } else {
                    setEventDate(data);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setBirthday(View rootView, String birthday) {
        TextView view = (TextView) rootView.findViewById(R.id.contact_birthday_date);
        view.setText(birthday);
    }

    private void setAnniversary(View rootView, String anniversary) {
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.contact_anniversary);

        if (TextUtils.isEmpty(anniversary)) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
            TextView view = (TextView) rootView.findViewById(R.id.contact_anniversary_date);
            view.setText(anniversary);
        }
    }

    private void setEventDate(Cursor c) {

        if ( c != null && c.moveToNext()) {
            TextView view;

            int type = c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE));

            if (type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) {
                view = (TextView) getActivity().findViewById(R.id.contact_birthday_date);
            } else if (type == ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY) {
                LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.contact_anniversary);
                layout.setVisibility(View.VISIBLE);
                view = (TextView) getActivity().findViewById(R.id.contact_anniversary_date);
            } else {
                // not displaying custom ones yet
                return;
            }

            String date = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
            Log.i(LOG_TAG, "Found contact event: type=" + type + " date=" + date);

            // format date
            try {
                DateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date d = parseFormat.parse(date);
                DateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy");
                view.setText(displayFormat.format(d));
            } catch (ParseException e) {
                Log.e(LOG_TAG, "date parse failed", e);
                view.setText(date);
            }
        }
    }

    private void hideAnniversaryView() {
        Log.i(LOG_TAG, "no anniversary; hide layout");
        LinearLayout view = (LinearLayout) getActivity().findViewById(R.id.contact_anniversary);
        view.setVisibility(View.GONE);
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
