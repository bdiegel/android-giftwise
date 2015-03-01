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

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ContactAdapter extends CursorAdapter {

    private static final String LOG_TAG = ContactAdapter.class.getSimpleName();

    private LruCache<String, RoundedBitmapDrawable> mImageCache;

    private RoundedBitmapDrawable mPlaceholderImage;


    public ContactAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        if (mImageCache == null) {
            initBitmapCache();
        }

        if (mPlaceholderImage == null) {
            createPlaceholderImage(context.getResources());
        }
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
        ContentResolver contentResolver = context.getContentResolver();
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.nameView.setText(cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME));
        loadBitmap(contentResolver, cursor.getInt(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID), viewHolder.iconView);
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

    protected void createPlaceholderImage(Resources res) {
        Bitmap src = BitmapFactory.decodeResource(res, R.drawable.ic_face_grey600_48dp);
        RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(res, src);
        roundBitmap.setCornerRadius(Math.min(roundBitmap.getMinimumWidth(),roundBitmap.getMinimumHeight()) / 2.0f);
        mPlaceholderImage = roundBitmap;
    }

    protected void initBitmapCache() {
        Log.i(LOG_TAG, "Initialize bitmap cache");

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

    /**
     * Load thumbnail photo from cache if found. Otherwise, use place-holder image.
     * Start an async task to load image from the contacts content provider. If an
     * image is found, replace the place-holder and cache the image.
     *
     * @param contentResolver
     * @param resId
     * @param imageView
     */
    public void loadBitmap(ContentResolver contentResolver, int resId, ImageView imageView) {
        final String imageKey = String.valueOf(resId);

        final RoundedBitmapDrawable bitmap = getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
            // set temporary placeholder image
            imageView.setImageDrawable(mPlaceholderImage);

            // start background task to load image from contacts provider
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute(resId);
        }
    }

    /**
     * Background task to load image from the contacts provider.
     */
    class BitmapWorkerTask extends AsyncTask<Integer, Void, Drawable> {
        ImageView mImageView;

        public BitmapWorkerTask(ImageView imageView) {
            mImageView = imageView;
        }

        // Decode image in background.
        @Override
        protected Drawable doInBackground(Integer... params) {
            int contactId = params[0];
            final Bitmap bitmap = getContactPhoto(contactId);

            if (bitmap != null) {
                RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(mImageView.getResources(), bitmap);
                roundBitmap.setCornerRadius(Math.min(roundBitmap.getMinimumWidth(), roundBitmap.getMinimumHeight()) / 2.0f);
                addBitmapToMemoryCache(String.valueOf(contactId), roundBitmap);
                Log.i(LOG_TAG, "Caching bitmap for contactId: " + contactId);
                return roundBitmap;
            } else {
                addBitmapToMemoryCache(String.valueOf(contactId), mPlaceholderImage);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            if (drawable != null) {
                mImageView.setImageDrawable(drawable);
            }
        }

        final public Bitmap getContactPhoto(int contactId)
        {
            // ContactsContract.Contacts.PHOTO_ID
            // Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId);

            ContentResolver cr = mImageView.getContext().getContentResolver();
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
            //InputStream input = openPhoto(contactId);

            if (input == null) {
                Log.i(LOG_TAG, "NULL photo input stream: contactId=" + contactId + " uri=" + uri);
                return null;
            } else {
                Log.i(LOG_TAG, "Loading photo: contactId=" + contactId + " uri=" + uri);
            }

            return BitmapFactory.decodeStream(input);
        }

        /**
         * Alternative method of loading the thumbnail bitmap. Convenience method is
         * ContactsContract.Contacts.openContactPhotoInputStream.
         *
         * @param contactId
         * @return
         */
        public InputStream openPhoto(long contactId) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            Cursor cursor = mImageView.getContext().getContentResolver().query(photoUri,
                  new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
            if (cursor == null) {
                return null;
            }
            try {
                if (cursor.moveToFirst()) {
                    byte[] data = cursor.getBlob(0);
                    if (data != null) {
                        return new ByteArrayInputStream(data);
                    }
                }
            } finally {
                cursor.close();
            }
            return null;
        }

    }

}
