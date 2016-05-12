package com.honu.giftwise;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.honu.giftwise.data.Size;


public class EditSizeActivity extends AppCompatActivity {

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
}
