package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by bdiegel on 3/4/15.
 */
public class IdeasAdapter extends CursorAdapter {

    private static final String LOG_TAG = IdeasAdapter.class.getSimpleName();

    public IdeasAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
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
        viewHolder.nameView.setText(cursor.getString(cursor.getColumnIndex("name")));
        viewHolder.urlView.setText(cursor.getString(cursor.getColumnIndex("url")));
        viewHolder.priceView.setText(cursor.getString(cursor.getColumnIndex("price")));
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

            menuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(LOG_TAG, "MENU PRESSED: " + view.getParent().toString());
                    Log.i(LOG_TAG, "activity: " + view.getContext().toString());
                    showPopup(v);
                }
            });

            //((Activity)view.getContext()).registerForContextMenu(menuView);
        }

        public void showPopup(View v) {
            Log.i(LOG_TAG, "Show popup");
            PopupMenu popup = new PopupMenu(v.getContext(), v);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.gift_edit:
                            //archive(item);
                            Log.i(LOG_TAG, "Edit pressed");
                            return true;
                        case R.id.gift_delete:
                            Log.i(LOG_TAG, "Delete pressed");
                            //delete(item);
                            return true;
                        case R.id.gift_open_url:
                            Log.i(LOG_TAG, "Open url pressed");
                            //delete(item);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_gift_item, popup.getMenu());
            popup.show();
        }
    }

}
