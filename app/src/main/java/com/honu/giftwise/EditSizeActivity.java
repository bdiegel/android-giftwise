package com.honu.giftwise;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

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

//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_edit_size, menu);
//        return true;
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        // noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

}
