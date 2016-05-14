package com.honu.giftwise.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honu.giftwise.GiftwiseApplication;
import com.honu.giftwise.R;
import com.honu.giftwise.data.ContactImageCache;
import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.tasks.ContactBitmapTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class ContactAdapter extends CursorRecyclerViewAdapter<ContactAdapter.ViewHolder> {

    private static final String TAG = ContactAdapter.class.getSimpleName();

    private ContactItemActionListener mListener;

    private ContactImageCache mImageCache;

    public interface ContactItemActionListener {
        void viewContact(String contactName, long contactId, long rawId, String gwId);
        void deleteContact(long rawContactId, String gwid);
        void editContact(long contactId);
    }

    public ContactAdapter(Context context, Cursor cursor, ContactItemActionListener listener) {
        super(context, cursor);
        mListener = listener;
        mImageCache = ((GiftwiseApplication)context.getApplicationContext()).getContactImageCache();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_contact, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {
        final int position = cursor.getPosition();

        final String contactName = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME);
        final long contactId = cursor.getLong(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
        final long rawId = cursor.getLong(ContactsUtils.SimpleRawContactQuery.COL_RAW_CONTACT_ID);
        final String gwid = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_GWID);

        viewHolder.nameView.setText(contactName);

        viewHolder.viewGroup.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "onLongClick");
                PopupMenu popup = new PopupMenu(getContext(), viewHolder.viewGroup);
                popup.setOnMenuItemClickListener(new LongPressListener(position, contactName, contactId, rawId, gwid));
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_contact_item, popup.getMenu());
                popup.show();
                return  true;
            }
        });

        loadBitmap(getContext().getContentResolver(), contactId, viewHolder.iconView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.list_item_contact_image) ImageView iconView;
        @Bind(R.id.list_item_contact_name_textview) TextView nameView;
        @Bind(R.id.list_item_contact) ViewGroup viewGroup;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.list_item_contact)
        public void onContactClicked() {
            Log.i(TAG, "Item clicked: " );

            // move cursor to position
            ContactAdapter.this.getItemId(getAdapterPosition());

            // Get the Cursor
            Cursor cursor = getCursor();

            // Extract data from the item in the Cursor:
            String contactName = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_NAME);
            long contactId = cursor.getLong(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_ID);
            long rawId = cursor.getLong(ContactsUtils.SimpleRawContactQuery.COL_RAW_CONTACT_ID);
            String gwId = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_GWID);

            mListener.viewContact(contactName, contactId, rawId, gwId);
        }
    }

    class LongPressListener implements PopupMenu.OnMenuItemClickListener {

        int mPosition;

        String mContactName;
        long mContactId;
        long mRawId;
        String mGwId;

        public LongPressListener(int position, String contactName, long contactId, long rawId, String gwId) {
            mPosition = position;
            mContactName = contactName;
            mContactId = contactId;
            mRawId = rawId;
            mGwId = gwId;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.contact_view:
                    Log.d(TAG, "View pressed");
                    mListener.viewContact(mContactName, mContactId, mRawId, mGwId);
                    return true;
                case R.id.contact_edit:
                    Log.d(TAG, "Edit pressed");
                    mListener.editContact(mContactId);
                    return true;
                case R.id.contact_delete:
                    Log.d(TAG, "Delete pressed");
                    mListener.deleteContact(mRawId, mGwId);
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * Load thumbnail photo from cache if found. Otherwise, use place-holder image.
     * Start an async task to load image from the contacts content provider. If an
     * image is found, replace the place-holder and cache the image.
     */
    public void loadBitmap(ContentResolver contentResolver, long resId, ImageView imageView) {
        final RoundedBitmapDrawable bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(resId));

        if (bitmap != null) {
            imageView.setImageDrawable(bitmap);
        } else {
            imageView.setImageDrawable(mImageCache.getPlaceholderImage());
            ContactBitmapTask task = new ContactBitmapTask(imageView);
            task.execute(resId);
        }
    }
}
