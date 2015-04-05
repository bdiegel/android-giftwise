package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.honu.giftwise.data.GiftwiseContract;


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

        ContentResolver contentResolver = context.getContentResolver();
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int position = cursor.getPosition();
        //view.setTag(1, position);

        String item = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_ITEM_NAME));
        String size = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_NAME));
        String notes = cursor.getString(cursor.getColumnIndex(GiftwiseContract.SizeEntry.COLUMN_SIZE_NOTES));

        viewHolder.tvSize.setText(item + " - " + size);
        viewHolder.tvNotes.setText(notes);
    }

    public static class ViewHolder {

        public final TextView tvSize;
        public final TextView tvNotes;

        public ViewHolder(final View view) {
            tvSize = (TextView) view.findViewById(R.id.list_item_with_size);
            tvNotes = (TextView) view.findViewById(R.id.list_item_with_size_notes);
        }
    }
}
