/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.colorpicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.android.colorpicker.ColorPickerSwatch.OnColorSelectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog which takes in as input an array of colors and creates a palette allowing the user to
 * select a specific color swatch, which invokes a listener.
 */
public class ColorPickerDialog extends DialogFragment implements OnColorSelectedListener {

    public static final int SIZE_LARGE = 1;
    public static final int SIZE_SMALL = 2;

    protected AlertDialog mAlertDialog;

    protected static final String KEY_TITLE_ID = "title_id";
    protected static final String KEY_COLORS = "colors";
    protected static final String KEY_SELECTED_COLORS = "selected_colors";
    protected static final String KEY_COLUMNS = "columns";
    protected static final String KEY_SIZE = "size";

    protected int mTitleResId = R.string.color_picker_default_title;
    protected int[] mColors = null;
    protected int[] mSelectedColors;
    protected int mColumns;
    protected int mSize;

    private ColorPickerPalette mPalette;
    private ProgressBar mProgress;

    protected OnColorSelectedListener mListener;

    protected DialogSelectionListener mDialogListener;

    // activity should register for notifications of ok/cancel events to get selected colors
    public interface DialogSelectionListener {
        /**
         * Called when dialog OK button is pressed
         */
        public void onSelectionCompleted(int[] colors);

        /**
         * Called when dialog CANCEL button is pressed
         */
        public void onSelectionCancelled();
    }

    public ColorPickerDialog() {
        // Empty constructor required for dialog fragments.
    }

    public static ColorPickerDialog newInstance(int titleResId, int[] colors, int[] selectedColors,
            int columns, int size) {
        ColorPickerDialog ret = new ColorPickerDialog();
        ret.initialize(titleResId, colors, selectedColors, columns, size);
        return ret;
    }

    public void initialize(int titleResId, int[] colors, int[] selectedColors, int columns, int size) {
        setArguments(titleResId, columns, size);
        setColors(colors, selectedColors);
    }

    public void setArguments(int titleResId, int columns, int size) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_TITLE_ID, titleResId);
        bundle.putInt(KEY_COLUMNS, columns);
        bundle.putInt(KEY_SIZE, size);
        setArguments(bundle);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }

    public void setDialogSelectedListener(DialogSelectionListener listener) {
        mDialogListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTitleResId = getArguments().getInt(KEY_TITLE_ID);
            mColumns = getArguments().getInt(KEY_COLUMNS);
            mSize = getArguments().getInt(KEY_SIZE);
        }

        if (savedInstanceState != null) {
            mColors = savedInstanceState.getIntArray(KEY_COLORS);
            mSelectedColors = savedInstanceState.getIntArray(KEY_SELECTED_COLORS);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.color_picker_dialog, null);
        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mPalette = (ColorPickerPalette) view.findViewById(R.id.color_picker);
        mPalette.init(mSize, mColumns, this);

        if (mColors != null) {
            showPaletteView();
        }

        mAlertDialog = new AlertDialog.Builder(activity)
            .setTitle(mTitleResId)
            .setView(view)
            .create();

        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new  DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDialogListener.onSelectionCompleted(mSelectedColors);
                dismiss();
            }
        });
        mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new  DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDialogListener.onSelectionCancelled();
                dismiss();
            }
        });


        return mAlertDialog;
    }

    @Override
    public void onColorSelected(int color, boolean selected) {
        if (mListener != null) {
            mListener.onColorSelected(color, selected);
        }

        if (getTargetFragment() instanceof OnColorSelectedListener) {
            final OnColorSelectedListener listener =
                    (OnColorSelectedListener) getTargetFragment();
            listener.onColorSelected(color, selected);
        }

        if (selected)
            addSelectedColor(color);
        else
            removeSelectedColor(color);

        // Redraw palette to show checkmark on newly selected color before dismissing.
        mPalette.drawPalette(mColors, mSelectedColors);
    }

    private void addSelectedColor(int color) {
        List<Integer> selected = new ArrayList<Integer>();
        for (int selectedColor : mSelectedColors) {
            selected.add(selectedColor);
        }

        if (selected != null && selected.contains(color))
            return;

        selected.add(color);

        mSelectedColors = new int[selected.size()];
        for (int i=0; i < selected.size(); i++) {
            mSelectedColors[i] = (int) selected.get(i);
        }
    }

    private void removeSelectedColor(int color) {
        List<Integer> selected = new ArrayList<Integer>();
        for (int selectedColor : mSelectedColors) {
            selected.add(selectedColor);
        }

        if (selected != null && selected.contains(color))
            selected.remove(new Integer(color));

        mSelectedColors = new int[selected.size()];
        for (int i=0; i < selected.size(); i++) {
            mSelectedColors[i] = (int) selected.get(i);
        }
    }

    public void showPaletteView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.GONE);
            refreshPalette();
            mPalette.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressBarView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.VISIBLE);
            mPalette.setVisibility(View.GONE);
        }
    }

    public void setColors(int[] colors, int[] selectedColors) {
        if (mColors != colors || mSelectedColors != selectedColors) {
            mColors = colors;
            mSelectedColors = selectedColors;
            refreshPalette();
        }
    }

    public void setColors(int[] colors) {
        if (mColors != colors) {
            mColors = colors;
            refreshPalette();
        }
    }

    public void setSelectedColors(int[] colors) {
        if (mSelectedColors != colors) {
            mSelectedColors = colors;
            refreshPalette();
        }
    }

    private void refreshPalette() {
        if (mPalette != null && mColors != null) {
            mPalette.drawPalette(mColors, mSelectedColors);
        }
    }

    public int[] getColors() {
        return mColors;
    }

    public int[] getSelectedColors() {
        return mSelectedColors;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(KEY_COLORS, mColors);
        outState.putSerializable(KEY_SELECTED_COLORS, mSelectedColors);
    }
}
