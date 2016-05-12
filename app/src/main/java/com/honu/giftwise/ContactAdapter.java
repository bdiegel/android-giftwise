package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.data.ContactImageCache;
import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.tasks.ContactBitmapTask;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ContactAdapter extends CursorAdapter {

    private static final String TAG = ContactAdapter.class.getSimpleName();

    private ContactImageCache mImageCache;


    public ContactAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mImageCache = ((GiftwiseApplication)context.getApplicationContext()).getContactImageCache();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =  LayoutInflater.from(context).inflate(R.layout.list_item_contact, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int contactId = cursor.getInt(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
        viewHolder.nameView.setText(cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME));
        loadBitmap(context.getContentResolver(), contactId, viewHolder.iconView);
    }

    public static class ViewHolder{
        @Bind(R.id.list_item_contact_image) ImageView iconView;
        @Bind(R.id.list_item_contact_name_textview) TextView nameView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Load thumbnail photo from cache if found. Otherwise, use place-holder image.
     * Start an async task to load image from the contacts content provider. If an
     * image is found, replace the place-holder and cache the image.
     */
    public void loadBitmap(ContentResolver contentResolver, int resId, ImageView imageView) {
        final RoundedBitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(resId));

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
            imageView.setImageDrawable(mImageCache.getPlaceholderImage());
            ContactBitmapTask task = new ContactBitmapTask(imageView);
            task.execute(resId);
        }
    }
}
