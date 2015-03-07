package com.honu.giftwise.data;

import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by bdiegel on 2/27/15.
 */
public class GiftwiseContract {

    // CONTENT_AUTHORITY is the base of all URI's
    public static final String CONTENT_AUTHORITY = "com.honu.giftwise";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CONTACTS = "contacts";

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACTS).build();

    // content://com.honu.giftwise/contacts/<contactId>
    public static Uri buildContactUri(long contactId) {
        return ContentUris.withAppendedId(CONTENT_URI, contactId);
    }
}
