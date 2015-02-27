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


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Log.i(LOG_TAG, "toolbar: " + toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //toolbar.setTitle("Title");
            //toolbar.setSubtitle("subtitle");
            toolbar.setNavigationIcon(null);
            //toolbar.setNavigationContentDescription("Test");
            toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        }


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                  .add(R.id.container, new ContactsFragment())
                  .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings: {
                //startActivity(new Intent(this, SettingsActivity.class));
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

    private void addContact() {
        Log.i(LOG_TAG, "Add contact");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 0);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Intent data returned: Intent { dat=content://com.android.contacts/contacts/lookup/1255i3bb07a668d7adab4/31 flg=0x1 }
        Log.i(LOG_TAG, "Intent data returned: " + data);
        if (requestCode == 0) {
            if(resultCode == RESULT_OK){
                Log.i(LOG_TAG, "Intent data.getData(): " + data.getData());
                // TODO: should use a loader for all queries
                String displayName = getDisplayNameForContactLookupUri(data.getData());
                Log.i(LOG_TAG, "getDisplayName(): " + displayName);
                //ContactsUtils.createRawContact(this, "bdiegel@gmail.com", displayName);
                Intent intent = new Intent(this, CreateContactActivity.class);
                intent.putExtra(ContactsUtils.DISPLAY_NAME, displayName);
                startActivity(intent);
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
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

    private void findOrCreateRawContact(Uri lookupUri) {

        final String DISPLAY_NAME_COL = Build.VERSION.SDK_INT
              >= Build.VERSION_CODES.HONEYCOMB ?
              Contacts.DISPLAY_NAME_PRIMARY :
              Contacts.DISPLAY_NAME;

        final String[] projection = { DISPLAY_NAME_COL };

             // getContentResolver().query(lookupUri,

    }

//    private void createRawContact(String accountName, String displayName) {
//
//        String accountType = getString(R.string.account_type);
//
//        ArrayList<ContentProviderOperation> ops =
//              new ArrayList<ContentProviderOperation>();
//
//        int rawContactInsertIndex = ops.size();
//        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//              .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
//              .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
//              .build());
//
//        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//              .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
//              .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
//              .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
//              .build());
//
//        try {
//            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } catch (OperationApplicationException e) {
//            e.printStackTrace();
//        }
//
//
//    }

}
