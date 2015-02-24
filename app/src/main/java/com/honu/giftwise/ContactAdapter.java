package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;


public class ContactAdapter extends CursorAdapter {

    private static final String LOG_TAG = ContactAdapter.class.getSimpleName();

    private LruCache<String, RoundedBitmapDrawable> mImageCache;

    private Drawable mPlaceholderImage;


    public ContactAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        initBitmapCache();
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
        loadBitmap(cursor.getInt(ContactsFragment.COL_CONTACT_ID), viewHolder.iconView);
    }

    public static class ViewHolder{
        public final ImageView iconView;
        //public final TextView dateView;
        public final TextView nameView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_contact_image);
            nameView = (TextView)view.findViewById(R.id.list_item_contact_name_textview);
        }
    }

    protected void initBitmapCache() {

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mImageCache = new LruCache<String, RoundedBitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, RoundedBitmapDrawable drawable) {
                // The cache size will be measured in kilobytes rather than number of items.
                // return bitmap.getByteCount() / 1024;
                Bitmap bitmap = drawable.getBitmap();
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, RoundedBitmapDrawable bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mImageCache.put(key, bitmap);
        }
    }

    public RoundedBitmapDrawable getBitmapFromMemCache(String key) {
        return mImageCache.get(key);
    }

    public void loadBitmap(int resId, ImageView imageView) {
        final String imageKey = String.valueOf(resId);

        final RoundedBitmapDrawable bitmap = getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
            imageView.setImageDrawable(getPlaceholderImage(imageView.getResources()));
            // start task to load image from contacts provider
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute(resId);
        }
    }

    private Drawable getPlaceholderImage(Resources res) {

        if (mPlaceholderImage == null) {
            Bitmap src = BitmapFactory.decodeResource(res, R.drawable.ic_contact_picture_2);
            RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(res, src);
            roundBitmap.setCornerRadius(Math.min(roundBitmap.getMinimumWidth(),roundBitmap.getMinimumHeight()) / 2.0f);
            mPlaceholderImage = roundBitmap;
        }

        return mPlaceholderImage;
    }


    class BitmapWorkerTask extends AsyncTask<Integer, Void, Drawable> {
        ImageView mImageView;

        public BitmapWorkerTask(ImageView imageView) {
            mImageView = imageView;
        }

        // Decode image in background.
        @Override
        protected Drawable doInBackground(Integer... params) {
            final Bitmap bitmap = getContactPhoto(params[0]);

            if (bitmap != null) {
                RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(mImageView.getResources(), bitmap);
                roundBitmap.setCornerRadius(Math.min(roundBitmap.getMinimumWidth(), roundBitmap.getMinimumHeight()) / 2.0f);
                addBitmapToMemoryCache(String.valueOf(params[0]), roundBitmap);
                return roundBitmap;
            }
            return null;
        }

        final public Bitmap getContactPhoto(int contactId)
        {
            ContentResolver cr = mImageView.getContext().getContentResolver();
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Log.i(LOG_TAG, "URI: " + uri.toString());
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);

            if (input == null) {
                Log.i(LOG_TAG, "NULL photo inputstream: id=" + contactId);
                return null;
            }

            return BitmapFactory.decodeStream(input);
        }

    }

}
