package com.honu.giftwise;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setTitle("Title");
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
                String result=data.getStringExtra("result");
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void createContact() {
        Log.i(LOG_TAG, "Create contact");
        /*
 ArrayList<ContentProviderOperation> ops =
          new ArrayList<ContentProviderOperation>();
 ...
 int rawContactInsertIndex = ops.size();
 ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
          .withValue(RawContacts.ACCOUNT_TYPE, accountType)
          .withValue(RawContacts.ACCOUNT_NAME, accountName)
          .build());

 ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
          .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
          .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
          .withValue(StructuredName.DISPLAY_NAME, "Mike Sullivan")
          .build());

 getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

         */
    }

}
