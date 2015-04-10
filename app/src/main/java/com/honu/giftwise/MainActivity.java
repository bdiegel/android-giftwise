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


public class MainActivity extends ActionBarActivity {

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
        Log.i(LOG_TAG, "Add contact");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_CONTACT_IMPORT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // content://com.android.contacts/contacts/lookup/1255i3bb07a668d7adab4/31
        Log.i(LOG_TAG, "Intent data returned: " + data);

        // Handle result from contact import request
        if (requestCode == REQUEST_CODE_CONTACT_IMPORT) {

            // TODO: should use a loader
            if(resultCode == RESULT_OK) {
                Uri lookupUri = data.getData();
                String displayName = getDisplayNameForContactLookupUri(lookupUri);
                Log.i(LOG_TAG, "getDisplayName(): " + displayName);

                Intent intent = new Intent(this, CreateContactActivity.class);
                intent.putExtra(ContactsUtils.DISPLAY_NAME, displayName);
                intent.putExtra(ContactsUtils.LOOKUP_URI, lookupUri);
                startActivity(intent);
            }

            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Contact import cancelled", Toast.LENGTH_SHORT);
            }
        }
    }

    private void createContact() {
        Log.i(LOG_TAG, "Create contact");
        Intent intent = new Intent(this, CreateContactActivity.class);
        startActivity(intent);
    }

    private String getDisplayNameForContactLookupUri(Uri lookupUri)
    {
        final String DISPLAY_NAME_COL = Build.VERSION.SDK_INT
              >= Build.VERSION_CODES.HONEYCOMB ?
              Contacts.DISPLAY_NAME_PRIMARY :
              Contacts.DISPLAY_NAME;

        final String[] projection = new String[] {
              Contacts._ID,
              DISPLAY_NAME_COL,
        };

        Cursor cursor = getContentResolver().query (
              lookupUri,
              projection,
              null,
              null,
              null);

        if (!cursor.moveToNext()) // move to first (and only) row.
            throw new IllegalStateException ("contact no longer exists for key");

        String name = cursor.getString(1);
        cursor.close();

        return name;
    }

}
