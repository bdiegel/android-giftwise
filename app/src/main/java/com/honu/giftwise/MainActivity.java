package com.honu.giftwise;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.data.NotifyingAsyncQueryHandler;


public class MainActivity extends ActionBarActivity implements NotifyingAsyncQueryHandler.AsyncQueryListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_CONTACT_IMPORT = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            toolbar.setNavigationIcon(null);
            toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                  .add(R.id.container, new ContactsFragment())
                  .commit();
        }

        // make sure the account is created
        ContactsUtils.getOrCreateAccount(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle action bar item clicks here
        int id = item.getItemId();

        switch (id) {
            case R.id.about: {
                // TODO
                break;
            }
            case R.id.action_addcontact: {
                addContact();
                break;
            }
            case R.id.action_createcontact: {
                createContact();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected void addContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_CONTACT_IMPORT);
    }

    /**
     * The Activity returns a lookup URI for the selected Contact. We kick off and
     * async query to get the display name and launch our Activity to complete the import.
     *
     * Example content URI for contact:
     *
     *   content://com.android.contacts/contacts/lookup/1255i3bb07a668d7adab4/31
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "Intent data returned: " + data);

        // Handle result from contact import request
        if (requestCode == REQUEST_CODE_CONTACT_IMPORT) {

            if (resultCode == RESULT_OK) {
                Uri lookupUri = data.getData();
                asyncQueryDisplayName(lookupUri);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Contact import cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createContact() {
        Log.i(LOG_TAG, "Create contact");
        Intent intent = new Intent(this, CreateContactActivity.class);
        startActivity(intent);
    }

    private void asyncQueryDisplayName(final Uri lookupUri)
    {
        final String DISPLAY_NAME_COL = Build.VERSION.SDK_INT
              >= Build.VERSION_CODES.HONEYCOMB ?
              Contacts.DISPLAY_NAME_PRIMARY :
              Contacts.DISPLAY_NAME;

        final String[] projection = new String[] {
              Contacts._ID,
              DISPLAY_NAME_COL,
        };

        NotifyingAsyncQueryHandler asyncQuery = new NotifyingAsyncQueryHandler(this, this);
        asyncQuery.startQuery(1, lookupUri,
            lookupUri,
            projection,
            null,
            null,
            null);
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // move to first (and only) row
        if (!cursor.moveToNext())
            throw new IllegalStateException("contact no longer exists for key");

        // display name is returned by cursor
        String displayName = cursor.getString(1);

        // lookupUri was passed by query as cookie
        String lookupUri = cookie.toString();

        // open activity to add raw contact
        Intent intent = new Intent(this, CreateContactActivity.class);
        intent.putExtra(ContactsUtils.DISPLAY_NAME, displayName);
        intent.putExtra(ContactsUtils.LOOKUP_URI, lookupUri);
        startActivity(intent);
    }

}
