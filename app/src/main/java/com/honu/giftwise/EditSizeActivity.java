package com.honu.giftwise;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.honu.giftwise.data.GiftwiseContract;
import com.honu.giftwise.data.Size;


public class EditSizeActivity extends ActionBarActivity {

    private static final String LOG_TAG = EditGiftActivity.class.getSimpleName();

    private static final String EDIT_SIZE_FRAGMENT_TAG = "EDIT_SIZE_FRAGMENT_TAG";

    private Size size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_size);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_action_accept);
        }


        // Set title (for both add or edit mode):
        getSupportActionBar().setTitle("Save Size");

        Intent intent = getIntent();
        if (intent != null) {
            size = intent.getExtras().getParcelable("size");
        }


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                  .add(R.id.container, EditSizeFragment.getInstance(size), EDIT_SIZE_FRAGMENT_TAG)
                  .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_size, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.i(LOG_TAG, "onOptionsItemSelected id: " + id);

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // navigation icon selected (done)
        if (id == android.R.id.home) {
            Log.i(LOG_TAG, "navigation icon clicked");

            if (createOrSaveSize()) {
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean createOrSaveSize() {
        // TODO: insert or update database
        //Uri sizesForRawContactUri = GiftwiseContract.SizeEntry.buildSizesForRawContactUri(size.getRawContactId());

        EditText itemEdit = (EditText) findViewById(R.id.item_spinner);
        EditText sizeEdit = (EditText) findViewById(R.id.size_spinner);
        EditText notesEdit = (EditText) findViewById(R.id.size_notes);

        ContentValues values = new ContentValues();

        values.put(GiftwiseContract.SizeEntry.COLUMN_SIZE_RAWCONTACT_ID, size.getRawContactId());
        values.put(GiftwiseContract.SizeEntry.COLUMN_SIZE_ITEM_NAME, itemEdit.getText().toString());
        values.put(GiftwiseContract.SizeEntry.COLUMN_SIZE_NAME, sizeEdit.getText().toString());
        values.put(GiftwiseContract.SizeEntry.COLUMN_SIZE_NOTES, notesEdit.getText().toString());

        if (size.getSizeId() == -1) {
            // insert new entry into table
            getContentResolver().insert(GiftwiseContract.SizeEntry.SIZE_URI, values);
        } else {
            String selection = GiftwiseContract.GiftEntry._ID + " = ?";
            String[] selectionArgs = new String[]{size.getSizeId() + ""};
            getContentResolver().update(GiftwiseContract.SizeEntry.SIZE_URI, values, selection, selectionArgs);
        }

        return true;
    }

}
