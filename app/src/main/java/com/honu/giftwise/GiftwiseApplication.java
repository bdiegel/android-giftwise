package com.honu.giftwise;

import android.app.Application;
import android.content.Context;

import com.honu.giftwise.data.ContactImageCache;

/**
 * Created by bdiegel on 3/21/15.
 */
public class GiftwiseApplication extends Application {

    private ContactImageCache mContactImageCache;

    public static ContactImageCache getContactImageCache(Context context) {
        return ((GiftwiseApplication) context.getApplicationContext()).getContactImageCache();
    }

    public ContactImageCache getContactImageCache() {

        if (mContactImageCache == null) {
            mContactImageCache = new ContactImageCache(this);
        }

        return mContactImageCache;
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
