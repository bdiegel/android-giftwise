package com.honu.giftwise;

import android.app.Application;
import android.content.Context;

import com.honu.giftwise.data.ContactImageCache;
import com.honu.giftwise.data.GiftImageCache;

/**
 * Created by bdiegel on 3/21/15.
 */
public class GiftwiseApplication extends Application {

    private ContactImageCache mContactImageCache;
    private GiftImageCache mGiftImageCache;

    public static ContactImageCache getContactImageCache(Context context) {
        return ((GiftwiseApplication) context.getApplicationContext()).getContactImageCache();
    }

    public ContactImageCache getContactImageCache() {

        if (mContactImageCache == null) {
            mContactImageCache = new ContactImageCache(this);
        }

        return mContactImageCache;
    }

    public static GiftImageCache getGiftImageCache(Context context) {
        return ((GiftwiseApplication) context.getApplicationContext()).getGiftImageCache();
    }

    public GiftImageCache getGiftImageCache() {

        if (mGiftImageCache == null) {
            mGiftImageCache = new GiftImageCache(this);
        }

        return mGiftImageCache;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
