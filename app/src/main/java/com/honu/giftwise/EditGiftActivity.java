package com.honu.giftwise;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.honu.giftwise.data.GiftwiseContract;

/**
 * Created by bdiegel on 3/9/15.
 */
public class EditGiftActivity extends ActionBarActivity {

    private static final String LOG_TAG = EditGiftActivity.class.getSimpleName();

    private long mRawContactId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gift);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_action_accept);
        }
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);


        // TODO: title change depending on if we are in add or edit mode
        getSupportActionBar().setTitle("Save Gift");

        Intent intent = getIntent();
        mRawContactId = intent.getLongExtra("rawContactId", -1);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                  //.add(R.id.container, new EditGiftFragment())
                  .add(R.id.container, EditGiftFragment.getInstance(mRawContactId))
                  .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //TODO: getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // navigation icon selected (done)
        if (id == android.R.id.home) {
            Log.i(LOG_TAG, "navigation icon clicked");
            //EditText nameEditText = (EditText) findViewById(R.id.gift_name);
            //createOrSaveGift(nameEditText.getText().toString());
            if (createOrSaveGift()) {

//            NavUtils.navigateUpFromSameTask(this);
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


//    android:id="@+id/gift_name"
//    android:id="@+id/gift_url"
//    android:id="@+id/gift_price"
//    android:id="@+id/gift_notes"

    private boolean createOrSaveGift() {
        Log.i(LOG_TAG, "create or save gift idea");


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

        long price = 0;

        if (!TextUtils.isEmpty(priceTxt)) {
            try {
                price = Long.parseLong(priceTxt);
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Invalid price", Toast.LENGTH_LONG).show();
                return false;
            }
        }


        Uri giftsForRawContactUri = GiftwiseContract.GiftEntry.buildGiftsForRawContactUri(mRawContactId);

        // create content values
        ContentValues values = new ContentValues();
        values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_RAWCONTACT_ID, mRawContactId);
        values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_NAME, name);

        if (price != 0)
            values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_PRICE, price);
        if (!TextUtils.isEmpty(url))
            values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_URL, url);
        if (!TextUtils.isEmpty(notes))
            values.put(GiftwiseContract.GiftEntry.COLUMN_GIFT_NOTES, notes);

        // insert new entry into table
        getContentResolver().insert(GiftwiseContract.GiftEntry.GIFT_URI, values);
        //getContentResolver().insert(giftsForRawContactUri, values);

        return true;
    }


}
