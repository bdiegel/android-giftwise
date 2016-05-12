package com.honu.giftwise;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.data.BitmapUtils;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftImageCache;
import com.honu.giftwise.data.GiftwiseContract;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Adapts Cursor with gift ideas for the ListView
 */
public class IdeasAdapter extends CursorAdapter {

    private static final String LOG_TAG = IdeasAdapter.class.getSimpleName();

    private GiftImageCache mImageCache;

    public IdeasAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mImageCache = ((GiftwiseApplication) context.getApplicationContext()).getGiftImageCache();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_gift, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ContentResolver contentResolver = context.getContentResolver();
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        long giftId = cursor.getLong(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID));

        viewHolder.nameView.setText(cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME)));
        viewHolder.priceView.setText("");

        // format a price if there is one
        double price = cursor.getDouble(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_PRICE));
        if (price != 0.0) {
            Currency currency = Currency.getInstance(Locale.getDefault());
            String currencyCode = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_CURRENCY_CODE));
            if (!TextUtils.isEmpty(currencyCode)) {
                currency = Currency.getInstance(currencyCode);
            }

            NumberFormat format = NumberFormat.getCurrencyInstance();
            format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
            format.setCurrency(currency);

            viewHolder.priceView.setText(format.format(price));
        }
        // tag the menu view with the GiftId for retrieval in the menu selection handler later
        //viewHolder.menuView.setTag(giftId);

        // display gift image
        loadBitmap(contentResolver, giftId, viewHolder.iconView, cursor);
    }

    public String getShareText() {
        StringBuffer buffer = new StringBuffer();

        if (mCursor.getCount() > 0 ) {
            // note current position then reset cursor
            int position = mCursor.getPosition();
            mCursor.moveToPosition(-1);

            while (mCursor.moveToNext()) {
                Gift gift = Gift.createFromCursor(mCursor);

                String priceTxt = "";
                if (gift.getPrice() > 0)
                    priceTxt = gift.getFormattedPrice();

                buffer.append("----------------------------------------\n");
                buffer.append(String.format("%s %s\n", gift.getName(), priceTxt));

                if (!TextUtils.isEmpty(gift.getNotes()))
                    buffer.append(String.format("Notes: %s\n", gift.getNotes()));
                if (!TextUtils.isEmpty(gift.getUrl()))
                    buffer.append(gift.getUrl());
            }
            buffer.append("----------------------------------------\n");

            mCursor.moveToPosition(position);
        }

        return buffer.toString();
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView nameView;
        public final TextView priceView;

        public ViewHolder(final View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_gift_image);
            nameView = (TextView) view.findViewById(R.id.list_item_gift_name_textview);
            priceView = (TextView) view.findViewById(R.id.list_item_gift_price_textview);
        }
    }

    public void removeImageFromCache(String imageKey) {
        Log.d(LOG_TAG, "Remove cached image for imageKey: " + imageKey);
        mImageCache.removeBitmapFromMemCache(imageKey);
    }

    public void loadBitmap(ContentResolver contentResolver, long resId, ImageView imageView, Cursor cursor) {
        final String imageKey = String.valueOf(resId);
        final BitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
            byte[] blob = cursor.getBlob(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_IMAGE));
            if (blob != null && blob.length > 0) {
                BitmapWorkerTask task = new BitmapWorkerTask(imageView, blob);
                task.execute(resId);
            } else {
                // set temporary placeholder image
                imageView.setImageDrawable(mImageCache.getPlaceholderImage());
            }
        }
    }

    class BitmapWorkerTask extends AsyncTask<Long, Void, Bitmap> {
        private final WeakReference<ImageView> mImageView;
        private final byte[] mBlob;
        private final Resources mResources;

        public BitmapWorkerTask(ImageView imageView, byte[] blob) {
            mImageView = new WeakReference<ImageView>(imageView);
            mResources = mImageView.get().getResources();
            mBlob = blob;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Long... params) {
            long resId = params[0];
            final String imageKey = String.valueOf(resId);

            Bitmap bitmap = BitmapUtils.getImage(mBlob);
            mImageCache.updateBitmapToMemoryCache(imageKey, new BitmapDrawable(mResources, bitmap));

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null) {
                mImageView.get().setImageBitmap(bitmap);
            }
        }
    }
}
