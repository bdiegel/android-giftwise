package com.honu.giftwise;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ContactAdapter extends CursorAdapter {


    public ContactAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // inflate view
        View view =  LayoutInflater.from(context).inflate(R.layout.list_item_contact, parent, false);

        // wse ViewHolder to save inflated views:
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Use ViewHolder
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.nameView.setText(cursor.getString(ContactsFragment.COL_CONTACT_NAME));
    }

    public static class ViewHolder{
        //public final ImageView iconView;
        //public final TextView dateView;
        public final TextView nameView;

        public ViewHolder(View view) {
//            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
//            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            nameView = (TextView)view.findViewById(R.id.list_item_contact_name_textview);
        }
    }
}
