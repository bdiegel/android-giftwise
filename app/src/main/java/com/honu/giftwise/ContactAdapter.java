package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.data.ContactImageCache;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ContactAdapter extends CursorAdapter {

    private static final String LOG_TAG = ContactAdapter.class.getSimpleName();

    private ContactImageCache mImageCache;


    public ContactAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mImageCache = ((GiftwiseApplication)context.getApplicationContext()).getContactImageCache();
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
        int contactId = cursor.getInt(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
        viewHolder.nameView.setText(cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME));
        loadBitmap(contentResolver, contactId, viewHolder.iconView);
        //Log.d(LOG_TAG, "bindView at cursor position: " + cursor.getPosition() + " contactId: " + contactId);
    }

    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView nameView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_contact_image);
            nameView = (TextView)view.findViewById(R.id.list_item_contact_name_textview);
        }
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

        final RoundedBitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
            // set temporary placeholder image
            imageView.setImageDrawable(mImageCache.getPlaceholderImage());

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
                mImageCache.addBitmapToMemoryCache(String.valueOf(contactId), roundBitmap);
                return roundBitmap;
            } else {
                mImageCache.addBitmapToMemoryCache(String.valueOf(contactId), mImageCache.getPlaceholderImage());
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
