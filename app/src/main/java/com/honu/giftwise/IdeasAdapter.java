package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by bdiegel on 3/4/15.
 */
public class IdeasAdapter extends CursorAdapter {

    public IdeasAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // inflate view
        View view =  LayoutInflater.from(context).inflate(R.layout.list_item_gift, parent, false);

        // wse ViewHolder to save inflated views:
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ContentResolver contentResolver = context.getContentResolver();
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //viewHolder.nameView.setText(cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME));
        viewHolder.nameView.setText(cursor.getString(cursor.getColumnIndex("name")));
        viewHolder.urlView.setText(cursor.getString(cursor.getColumnIndex("url")));
        viewHolder.priceView.setText(cursor.getString(cursor.getColumnIndex("price")));
    }

    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView nameView;
        public final TextView urlView;
        public final TextView priceView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_gift_image);
            nameView = (TextView)view.findViewById(R.id.list_item_gift_name_textview);
            urlView = (TextView)view.findViewById(R.id.list_item_gift_url_textview);
            priceView = (TextView)view.findViewById(R.id.list_item_gift_price_textview);
        }
    }
}
