package com.honu.giftwise.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.honu.giftwise.R;

/**
 * Created by bdiegel on 3/21/15.
 */
public class GiftImageCache {

    private static final String LOG_TAG = GiftImageCache.class.getSimpleName();

    private LruCache<String, BitmapDrawable> mImageCache;

    private BitmapDrawable mPlaceholderImage;

    public GiftImageCache(Context context) {

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

        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable drawable) {
                // The cache size will be measured in kilobytes rather than number of items.
                // return bitmap.getByteCount() / 1024;
                Bitmap bitmap = drawable.getBitmap();
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };
    }

    protected void createPlaceholderImage(Resources res) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.gift_gray);
        //RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(res, src);
        //roundBitmap.setCornerRadius(Math.min(roundBitmap.getMinimumWidth(),roundBitmap.getMinimumHeight()) / 2.0f);
        mPlaceholderImage = new BitmapDrawable(res, bitmap);
    }

    public void addBitmapToMemoryCache(String key, BitmapDrawable drawable) {
        if (getBitmapFromMemCache(key) == null) {
            if (!drawable.equals(mPlaceholderImage)) {
                Log.i(LOG_TAG, "Caching bitmap for giftId: " + key);
            }
            mImageCache.put(key, drawable);
        }
    }

    public BitmapDrawable getBitmapFromMemCache(String key) {
        return mImageCache.get(key);
    }

    public BitmapDrawable getPlaceholderImage() {
        return mPlaceholderImage;
    }

}
