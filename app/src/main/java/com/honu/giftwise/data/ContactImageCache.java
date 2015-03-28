package com.honu.giftwise.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.honu.giftwise.R;

/**
 * Created by bdiegel on 3/21/15.
 */
public class ContactImageCache {

    private static final String LOG_TAG = ContactImageCache.class.getSimpleName();

    private LruCache<String, RoundedBitmapDrawable> mImageCache;

    private RoundedBitmapDrawable mPlaceholderImage;

    //public void ContactImageCache(Context context) {
    public ContactImageCache(Context context) {

        if (mImageCache == null) {
            initBitmapCache();
        }

        if (mPlaceholderImage == null) {
            createPlaceholderImage(context.getResources());
        }

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

    protected void createPlaceholderImage(Resources res) {
        Bitmap src = BitmapFactory.decodeResource(res, R.drawable.ic_face_grey600_48dp);
        RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(res, src);
        roundBitmap.setCornerRadius(Math.min(roundBitmap.getMinimumWidth(),roundBitmap.getMinimumHeight()) / 2.0f);
        mPlaceholderImage = roundBitmap;
    }

    public void addBitmapToMemoryCache(String key, RoundedBitmapDrawable bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            if (!bitmap.equals(mPlaceholderImage)) {
                Log.i(LOG_TAG, "Caching bitmap for contactId: " + key);
            }
            mImageCache.put(key, bitmap);
        }
    }

    public RoundedBitmapDrawable getBitmapFromMemCache(String key) {
        return mImageCache.get(key);
    }

    public RoundedBitmapDrawable getPlaceholderImage() {
        return mPlaceholderImage;
    }

}
