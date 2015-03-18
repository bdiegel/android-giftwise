package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.data.GiftwiseContract;

/**
 * Adapts Cursor with gift ideas for the ListView
 */
public class IdeasAdapter extends CursorAdapter {

    private static final String LOG_TAG = IdeasAdapter.class.getSimpleName();

    // handler to attach to the image view with the overflow icon
    private static View.OnClickListener mOverflowMenuClickListener;

    public IdeasAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setOverflowMenuListener(View.OnClickListener listener) {
        mOverflowMenuClickListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // inflate view
        View view =  LayoutInflater.from(context).inflate(R.layout.list_item_gift, parent, false);

        // use ViewHolder to save inflated views:
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ContentResolver contentResolver = context.getContentResolver();
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //viewHolder.nameView.setText(cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME));
        viewHolder.nameView.setText(cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME)));
        viewHolder.urlView.setText(cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_URL)));
        viewHolder.priceView.setText(cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_PRICE)));

        // tag the menu view with the GiftId for retrieval in the menu selection handler later
        viewHolder.menuView.setTag(cursor.getLong(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID)));
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView nameView;
        public final TextView urlView;
        public final TextView priceView;
        public final ImageView menuView;

        public ViewHolder(final View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_gift_image);
            nameView = (TextView)view.findViewById(R.id.list_item_gift_name_textview);
            urlView = (TextView)view.findViewById(R.id.list_item_gift_url_textview);
            priceView = (TextView)view.findViewById(R.id.list_item_gift_price_textview);
            menuView = (ImageView) view.findViewById(R.id.list_item_gift_overflow_icon);
            Linkify.addLinks(urlView, Linkify.WEB_URLS);

            menuView.setOnClickListener(mOverflowMenuClickListener);
        }
    }

}
