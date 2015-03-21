package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.CursorAdapter;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.data.BitmapUtils;
import com.honu.giftwise.data.GiftImageCache;
import com.honu.giftwise.data.GiftwiseContract;

/**
 * Adapts Cursor with gift ideas for the ListView
 */
public class IdeasAdapter extends CursorAdapter {

    private static final String LOG_TAG = IdeasAdapter.class.getSimpleName();

    private GiftImageCache mImageCache;

    // handler to attach to the image view with the overflow icon
    private static View.OnClickListener mOverflowMenuClickListener;

    public IdeasAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mImageCache = ((GiftwiseApplication)context.getApplicationContext()).getGiftImageCache();
    }

    public void setOverflowMenuListener(View.OnClickListener listener) {
        mOverflowMenuClickListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // inflate view
        View view =  LayoutInflater.from(context).inflate(R.layout.list_item_gift, parent, false);

        // use ViewHolder to save inflated views:
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ContentResolver contentResolver = context.getContentResolver();
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        long giftId = cursor.getLong(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID));

        //viewHolder.nameView.setText(cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME));
        viewHolder.nameView.setText(cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME)));
        viewHolder.urlView.setText(cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_URL)));
        viewHolder.priceView.setText(cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_PRICE)));

        // tag the menu view with the GiftId for retrieval in the menu selection handler later
        viewHolder.menuView.setTag(giftId);

        // display gift image
        loadBitmap(contentResolver, giftId, viewHolder.iconView, cursor);
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView nameView;
        public final TextView urlView;
        public final TextView priceView;
        public final ImageView menuView;

        public ViewHolder(final View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_gift_image);
            nameView = (TextView)view.findViewById(R.id.list_item_gift_name_textview);
            urlView = (TextView)view.findViewById(R.id.list_item_gift_url_textview);
            priceView = (TextView)view.findViewById(R.id.list_item_gift_price_textview);
            menuView = (ImageView) view.findViewById(R.id.list_item_gift_overflow_icon);
            Linkify.addLinks(urlView, Linkify.WEB_URLS);

            menuView.setOnClickListener(mOverflowMenuClickListener);
        }
    }

    public void loadBitmap(ContentResolver contentResolver, long resId, ImageView imageView, Cursor cursor) {
        final String imageKey = String.valueOf(resId);

        final BitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
//            // start background task to load image from contacts provider
//            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
//            task.execute(resId);
            // TODO: display image
            // TODO: move to background task
            byte[] blob = cursor.getBlob(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_IMAGE));
            if (blob != null && blob.length > 0 ) {
                Bitmap bm = BitmapUtils.getImage(blob);
                imageView.setImageBitmap(bm);
                mImageCache.updateBitmapToMemoryCache(imageKey, new BitmapDrawable(imageView.getResources(), bm));
            } else {
                // set temporary placeholder image
                imageView.setImageDrawable(mImageCache.getPlaceholderImage());
            }
        }
    }

}
