package com.honu.giftwise;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.honu.giftwise.data.GiftwiseContract;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SizeAdapter extends CursorAdapter {

    private static final String LOG_TAG = SizeAdapter.class.getSimpleName();

    public SizeAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // inflate view for item
        View view =  LayoutInflater.from(context).inflate(R.layout.list_item_size, parent, false);

        // use ViewHolder to save inflated views:
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        
        String item = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_ITEM_NAME));
        String size = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_NAME));
        String notes = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_NOTES));

        viewHolder.sizeText.setText(item + " - " + size);
        viewHolder.notesText.setText(notes);
    }

    public static class ViewHolder {

        @Bind(R.id.list_item_with_size) TextView sizeText;
        @Bind(R.id.list_item_with_size_notes) TextView notesText;

        public ViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
