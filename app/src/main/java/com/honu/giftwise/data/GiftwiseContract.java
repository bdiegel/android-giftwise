package com.honu.giftwise.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by bdiegel on 2/27/15.
 */
public class GiftwiseContract {

    // CONTENT_AUTHORITY is the base of all URI's
    public static final String CONTENT_AUTHORITY = "com.honu.giftwise";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // URI paths:
    public static final String PATH_CONTACT = "contact";
    public static final String PATH_GIFT = "gift";
    public static final String PATH_COLOR = "color";
    public static final String PATH_SIZE = "size";

    //public static final Uri GIFT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GIFT).build();

    // content://com.honu.giftwise/contacts/<contactId>
//    public static Uri buildContactUri(long contactId) {
//        return ContentUris.withAppendedId(GIFT_URI, contactId);
//    }


    /* Define contents of 'Gift' table */
    public static final class GiftEntry implements BaseColumns {

        public static final Uri GIFT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GIFT).build();
        public static final Uri GIFTS_BYCONTACT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GIFT).appendPath(PATH_CONTACT).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GIFT;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GIFT;

        // Table name
        public static final String TABLE_NAME = "gift";

        // Columns:
        public static final String COLUMN_GIFT_NAME = "name";
        public static final String COLUMN_GIFT_URL = "url";
        public static final String COLUMN_GIFT_PRICE = "price";
        public static final String COLUMN_GIFT_CURRENCY_CODE = "currency_code";
        public static final String COLUMN_GIFT_IMAGE = "image";
        public static final String COLUMN_GIFT_NOTES = "notes";
        public static final String COLUMN_GIFT_RAWCONTACT_ID = "rawcontact_id";


        public static Uri buildGiftUri(long id) {
            return ContentUris.withAppendedId(GIFT_URI, id);
        }

        public static Uri buildGiftsForRawContactUri(long rawContactId) {
            return ContentUris.withAppendedId(GIFTS_BYCONTACT_URI, rawContactId);
        }

        // return id for uri in either format
        public static long getIdFromUri(Uri uri) {
            //return  Long.parseLong(uri.getPathSegments().get(1));
            return  Long.parseLong(uri.getPathSegments().get(uri.getPathSegments().size()-1));
        }

//        public static long getIdFromUriWithContactId(Uri uri) {
//            return  Long.parseLong(uri.getPathSegments().get(uri.getPathSegments().size()-1));
//        }
    }

    // Color Table Entry:
    public static final class ColorEntry implements BaseColumns {

        // Uris
        public static final Uri COLOR_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COLOR).build();
        public static final Uri COLOR_BYCONTACT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COLOR).appendPath(PATH_CONTACT).build();

        // Content types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COLOR;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COLOR;

        // Table name
        public static final String TABLE_NAME = "color";

        // Column names:
        public static final String COLUMN_COLOR_VALUE = "color";
        public static final String COLUMN_COLOR_RAWCONTACT_ID = "rawcontact_id";
        public static final String COLUMN_COLOR_LIKED = "liked";

        public static Uri buildColorUri(long id) {
            return ContentUris.withAppendedId(COLOR_URI, id);
        }

        public static Uri buildColorsForRawContactUri(long rawContactId) {
            return ContentUris.withAppendedId(COLOR_BYCONTACT_URI, rawContactId);
        }

        public static long getIdFromUri(Uri uri) {
            return  Long.parseLong(uri.getPathSegments().get(uri.getPathSegments().size()-1));
        }
    }

    // Size Table Entry:
    public static final class SizeEntry implements BaseColumns {
        // Uris
        public static final Uri SIZE_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SIZE).build();
        public static final Uri SIZE_BYCONTACT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SIZE).appendPath(PATH_CONTACT).build();

        // Content types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SIZE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SIZE;

        // Table name
        public static final String TABLE_NAME = "size";

        // Column names:
        public static final String COLUMN_SIZE_RAWCONTACT_ID = "rawcontact_id";
        public static final String COLUMN_SIZE_ITEM_NAME = "item";
        public static final String COLUMN_SIZE_NAME = "size";
        public static final String COLUMN_SIZE_NOTES = "notes";

        public static Uri buildSizeUri(long id) {
            return ContentUris.withAppendedId(SIZE_URI, id);
        }

        public static Uri buildSizesForRawContactUri(long rawContactId) {
            return ContentUris.withAppendedId(SIZE_BYCONTACT_URI, rawContactId);
        }

        public static long getIdFromUri(Uri uri) {
            return  Long.parseLong(uri.getPathSegments().get(uri.getPathSegments().size()-1));
        }
    }
}
