package com.honu.giftwise.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
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
import android.util.Log;
import android.widget.ImageView;

import com.honu.giftwise.GiftwiseApplication;
import com.honu.giftwise.data.ContactImageCache;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Background task to load image from the contacts provider.
 */
public class ContactBitmapTask extends AsyncTask<Integer, Void, Drawable> {

    private static final String LOG_TAG = ContactBitmapTask.class.getSimpleName();

    private ContactImageCache mImageCache;

    private final WeakReference<ImageView> mImageView;

    private final Resources mResources;

    public ContactBitmapTask(ImageView imageView) {
        mImageView = new WeakReference<ImageView>(imageView);
        mResources = mImageView.get().getResources();
        mImageCache = ((GiftwiseApplication) imageView.getContext().getApplicationContext()).getContactImageCache();
    }

    // Decode image in background.
    @Override
    protected Drawable doInBackground(Integer... params) {
        int contactId = params[0];
        final Bitmap bitmap = getContactPhoto(contactId);

        if (bitmap != null) {
            RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(mResources, bitmap);
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
            mImageView.get().setImageDrawable(drawable);
        }
    }

    final public Bitmap getContactPhoto(int contactId) {
        ContentResolver cr = mImageView.get().getContext().getContentResolver();
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);

        if (input == null) {
            Log.d(LOG_TAG, "NULL photo input stream: contactId=" + contactId + " uri=" + uri);
            return null;
        } else {
            Log.d(LOG_TAG, "Loading photo: contactId=" + contactId + " uri=" + uri);
        }

        return BitmapFactory.decodeStream(input);
    }

    /**
     * Alternative method of loading the thumbnail bitmap. Convenience method is
     * ContactsContract.Contacts.openContactPhotoInputStream.
     */
    public InputStream openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = mImageView.get().getContext().getContentResolver().query(photoUri,
              new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
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
