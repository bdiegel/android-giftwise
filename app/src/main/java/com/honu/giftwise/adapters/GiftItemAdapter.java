package com.honu.giftwise.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.GiftwiseApplication;
import com.honu.giftwise.R;
import com.honu.giftwise.data.BitmapUtils;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftImageCache;
import com.honu.giftwise.data.GiftwiseContract;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class GiftItemAdapter extends CursorRecyclerViewAdapter<GiftItemAdapter.ViewHolder> {

    private static final String TAG = GiftItemAdapter.class.getSimpleName();

    private GiftImageCache mImageCache;

    private GiftItemActionListener mListener;

    public interface GiftItemActionListener {
        void openGift(Gift gift);
        void editGift(Gift gift);
        void deleteGift(Gift gift);
    }

    public GiftItemAdapter(Context context, Cursor cursor, GiftItemActionListener listener) {
        super(context, cursor);
        mListener = listener;
        mImageCache = ((GiftwiseApplication) context.getApplicationContext()).getGiftImageCache();
    }

    @Override
    public GiftItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_gift, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {
        final long giftId = cursor.getLong(cursor.getColumnIndex(GiftwiseContract.GiftEntry._ID));
        final int position = cursor.getPosition();

        // enable a long-click context menu
        viewHolder.itemView.setLongClickable(true);

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

        String notes = cursor.getString(cursor.getColumnIndex(GiftwiseContract.GiftEntry.COLUMN_GIFT_NOTES));
        if (TextUtils.isEmpty(notes)) {
            viewHolder.notesView.setVisibility(View.GONE);
        } else {
            viewHolder.notesView.setVisibility(View.VISIBLE);
            viewHolder.notesView.setText(notes);
        }

        // tag the menu view with the GiftId for retrieval in the menu selection handler later
        //viewHolder.menuView.setTag(mGiftId);

        viewHolder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mGiftId
                Log.d(TAG, "onOverflowClick CLICK");
                PopupMenu popup = new PopupMenu(getContext(), viewHolder.menuButton);
                popup.setOnMenuItemClickListener(new OverflowMenuListener(position, giftId));
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_gift_item, popup.getMenu());
                popup.show();
            }
        });

        // display gift image
        loadBitmap(getContext().getContentResolver(), giftId, viewHolder.iconView, cursor);
    }

    class ViewHolder extends RecyclerView.ViewHolder { //implements View.OnCreateContextMenuListener {
        @Bind(R.id.list_item_gift_image) ImageView iconView;
        @Bind(R.id.list_item_gift_name_textview) TextView nameView;
        @Bind(R.id.list_item_gift_price_textview) TextView priceView;
        @Bind(R.id.list_item_gift_notes) TextView notesView;
        @Bind(R.id.card_overflow) ImageButton menuButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.list_item_card)
        public void onClick() {
            GiftItemAdapter.this.getItemId(getAdapterPosition());
            Gift gift = Gift.createFromCursor(getCursor());
            mListener.openGift(gift);
        }
    }

    class OverflowMenuListener implements PopupMenu.OnMenuItemClickListener {

        int mPosition;
        long mGiftId;

        public OverflowMenuListener(int position, long giftId) {
            mPosition = position;
            mGiftId = giftId;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Cursor cursor = getCursor();
            cursor.moveToPosition(mPosition);
            Gift gift = Gift.createFromCursor(cursor);

            switch (item.getItemId()) {
                case R.id.gift_view:
                    mListener.openGift(gift);
                    return true;
                case R.id.gift_edit:
                    mListener.editGift(gift);
                    return true;
                case R.id.gift_delete:
                    mListener.deleteGift(gift);
                    return true;
                default:
                    return false;
            }
        }
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

    public void removeImageFromCache(String imageKey) {
        Log.d(TAG, "Remove cached image for imageKey: " + imageKey);
        mImageCache.removeBitmapFromMemCache(imageKey);
    }

    public String getShareText() {
        StringBuffer buffer = new StringBuffer();

        if (getItemCount() > 0 ) {
            // note current mPosition then reset cursor
            int position = getCursor().getPosition();
            getCursor().moveToPosition(-1);

            while (getCursor().moveToNext()) {
                Gift gift = Gift.createFromCursor(getCursor());

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

            getCursor().moveToPosition(position);
        }

        return buffer.toString();
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
