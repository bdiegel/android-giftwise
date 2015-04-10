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
 * Cache for Gift images
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

        Log.d(LOG_TAG, "CacheSize: " + cacheSize);

        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable drawable) {
                // The cache size will be measured in kilobytes rather than number of items.
                Bitmap bitmap = drawable.getBitmap();
                int size = (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
                Log.d(LOG_TAG, "sizeOf: " + size);
                Log.d(LOG_TAG, "CacheSize: " + size() + " createCount: " + createCount() + " putCount: " + putCount());
                Log.d(LOG_TAG, "Cache: " + this.toString());

                return size;
            }
        };
    }

    protected void createPlaceholderImage(Resources res) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.gift_silhouette_48dp);
        mPlaceholderImage = new BitmapDrawable(res, bitmap);
    }

    public void updateBitmapToMemoryCache(String key, BitmapDrawable drawable) {

        // ignore invalid key
        if (key.equals("-1"))
            return;

        if (drawable != null) {
            if (!drawable.equals(mPlaceholderImage)) {
                Log.i(LOG_TAG, "Caching bitmap for giftId: " + key);
                BitmapDrawable r = mImageCache.put(key, drawable);
                if (r == null) {
                    Log.i(LOG_TAG, "Put failed for image cache");
                }
            }
        }
    }

    public BitmapDrawable getBitmapFromMemCache(String key) {
        return mImageCache.get(key);
    }

    public BitmapDrawable removeBitmapFromMemCache(String key) {
        return mImageCache.remove(key);
    }

    public BitmapDrawable getPlaceholderImage() {
        return mPlaceholderImage;
    }

}
