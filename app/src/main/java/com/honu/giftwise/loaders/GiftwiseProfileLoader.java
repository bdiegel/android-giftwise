package com.honu.giftwise.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.honu.giftwise.data.ContactImageCache;
import com.honu.giftwise.data.GiftwiseContract;

/**
 * Loads profile data for Contact from the database
 */
public class GiftwiseProfileLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    // data loaders for colors and sizes:
    public static final int PROFILE_COLORS_LIKED_LOADER = 20;
    public static final int PROFILE_COLORS_DISLIKED_LOADER = 21;
    public static final int PROFILE_SIZES_LOADER = 22;

    private static final String TAG = GiftwiseProfileLoader.class.getSimpleName();

    private String mGiftwiseId;

    private Context mContext;

    private GiftwiseProfileLoaderListener mListener;

    public interface GiftwiseProfileLoaderListener {
        void onLikedColorsLoaded(Cursor cursor);
        void onDislikedColorsLoaded(Cursor cursor);
        void onSizesLoaded(Cursor cursor);
    }

    public GiftwiseProfileLoader(Context context, GiftwiseProfileLoaderListener listener, String giftwiseId) {
        mContext = context.getApplicationContext();
        mGiftwiseId = giftwiseId;
        mListener = listener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        switch (id) {
            case PROFILE_COLORS_LIKED_LOADER: {
                String selection = GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED + " = ? ";
                String[] selectionArgs = new String[] { "1" };
                Uri uri = GiftwiseContract.ColorEntry.buildColorsForGiftwiseIdUri(mGiftwiseId);
                return new CursorLoader(mContext, uri, null, selection, selectionArgs, null);
            }
            case PROFILE_COLORS_DISLIKED_LOADER: {
                String selection = GiftwiseContract.ColorEntry.COLUMN_COLOR_LIKED + " = ? ";
                String[] selectionArgs = new String[] { "0" };
                Uri uri = GiftwiseContract.ColorEntry.buildColorsForGiftwiseIdUri(mGiftwiseId);
                return new CursorLoader(mContext, uri, null, selection, selectionArgs, null);
            }
            case PROFILE_SIZES_LOADER: {
                Uri uri = GiftwiseContract.SizeEntry.buildSizesForGiftwiseIdUri(mGiftwiseId);
                return new CursorLoader(mContext, uri, null, null, null, null);
            }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: " + loader.getId());

        switch (loader.getId()) {
            case PROFILE_COLORS_DISLIKED_LOADER:
                mListener.onDislikedColorsLoaded(cursor);
                break;
            case PROFILE_COLORS_LIKED_LOADER:
                mListener.onLikedColorsLoaded(cursor);
                break;
            case PROFILE_SIZES_LOADER:
                mListener.onSizesLoaded(cursor);
                break;
            default:
                break;
        }

        // reset position (on orientation change the same cursor is reused)
        cursor.moveToPosition(-1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
    }
}
