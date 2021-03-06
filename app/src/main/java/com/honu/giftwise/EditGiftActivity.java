package com.honu.giftwise;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.honu.giftwise.data.BitmapUtils;
import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.data.Gift;
import com.honu.giftwise.data.GiftwiseContract;

/**
 * Edit details of gift item
 */
public class EditGiftActivity extends AppCompatActivity {

    private static final String LOG_TAG = EditGiftActivity.class.getSimpleName();

    private static final String EDIT_GIFT_FRAGMENT_TAG = "EDIT_GIFT_FRAGMENT_TAG";

    private Gift gift;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gift);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_action_accept);
        }

        // Set title (for both add or edit mode):
        getSupportActionBar().setTitle("Save Gift");

        Intent intent = getIntent();
        String receivedAction = intent.getAction();

        // Started from external app sending a URL:
        if (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND)) {
            String receivedType = intent.getType();
            if (receivedType.startsWith("text/")) {
                // get the URL from the text
                String receivedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (receivedText != null) {
                    gift = new Gift();
                    gift.setUrl(receivedText);
                }
            }
            else if (receivedType.startsWith("image/")) {
                gift = new Gift();
            }
        } else {
            gift = intent.getExtras().getParcelable("gift");
        }


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                  .add(R.id.container, EditGiftFragment.getInstance(gift), EDIT_GIFT_FRAGMENT_TAG)
                  .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Log.d(LOG_TAG, "onOptionsItemSelected id: " + id );

        // navigation icon selected (done)
        if (id == android.R.id.home) {
            if (createOrSaveGift()) {
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                  .setMessage(getString(R.string.edit_gift_save_dialog_message))
                  .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          EditGiftActivity.super.onBackPressed();
                      }
                  })
                  .setNegativeButton("No", null)
                  .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        EditGiftFragment editGiftFragment = (EditGiftFragment) getSupportFragmentManager().findFragmentByTag(EDIT_GIFT_FRAGMENT_TAG);
        if (editGiftFragment == null) {
            return false;
        } else {
            return editGiftFragment.hasUnsavedChanges();
        }
    }

    private boolean createOrSaveGift() {
        Log.d(LOG_TAG, "create or save gift idea");

        Uri prevUri = null;

        Spinner contact_spin = (Spinner) findViewById(R.id.contacts_spinner);
        Cursor cursor = (Cursor)(contact_spin.getSelectedItem());
        if (cursor != null) {
            String gwid = cursor.getString(ContactsUtils.SimpleRawContactQuery.COL_CONTACT_GWID);

            // If recipient was modified we will need the old Uri to explicitly send an update notification later
            String prevGwid = gift.getGiftwiseId();
            if (prevGwid != null && !gwid.equals(prevGwid)) {
                prevUri = GiftwiseContract.GiftEntry.buildGiftsForGiftwiseIdUri(gift.getGiftwiseId());
            }

            gift.setGiftwiseId(gwid);
        }

        TextView name_tv = (TextView) findViewById(R.id.gift_name);
        TextView url_tv = (TextView) findViewById(R.id.gift_url);
        TextView price_tv = (TextView) findViewById(R.id.gift_price);
        TextView notes_tv = (TextView) findViewById(R.id.gift_notes);

        // name is required
        String name = name_tv.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_LONG).show();
            return false;
        }
        String priceTxt = price_tv.getText().toString();
        String url = url_tv.getText().toString();
        String notes = notes_tv.getText().toString();

        double price = 0;

        if (!TextUtils.isEmpty(priceTxt)) {
            try {
                price = Double.parseDouble(priceTxt);
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Invalid price", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        Uri uri = GiftwiseContract.GiftEntry.buildGiftsForGiftwiseIdUri(gift.getGiftwiseId());

        // create content values
        ContentValues values = new ContentValues();
        values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_GIFTWISE_ID, gift.getGiftwiseId());
        values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME, name);

        if (price != 0)
            values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_PRICE, price);
        if (!TextUtils.isEmpty(url))
            values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_URL, url);
        if (!TextUtils.isEmpty(notes))
            values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_NOTES, notes);

        byte[] bitmap = gift.getBitmap();

        if (bitmap != null) {
            Log.d(LOG_TAG, "Adding GIFT_IMAGE to ContentValues");
            values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_IMAGE, bitmap);
        } else {
            Log.d(LOG_TAG, "No GIFT_IMAGE for ContentValues");
        }

        // insert new gift or update existing one
        if (gift.getGiftId() == -1) {
            getContentResolver().insert(uri, values);
        } else {
            String selection = GiftwiseContract.GiftEntry._ID + " = ?";
            String[] selectionArgs = new String[] { gift.getGiftId() + "" };
            getContentResolver().update(uri, values, selection, selectionArgs);

            // If recipient has changed, explicitly notify old uri
            if (prevUri != null)
                getContentResolver().notifyChange(prevUri, null);
        }

        return true;
    }

    private byte[] getImageData(long giftId) {
        BitmapDrawable drawableBitmap = ((GiftwiseApplication)getApplicationContext()).getGiftImageCache().getBitmapFromMemCache(""+giftId);
        if (drawableBitmap != null) {
            Log.d(LOG_TAG, "Image data loaded from cache for giftId: " + giftId);
            Bitmap bitmap = drawableBitmap.getBitmap();
            return BitmapUtils.getBytes(bitmap);
        }
        Log.d(LOG_TAG, "No image data found for giftId: " + giftId);
        return null;
    }
}
