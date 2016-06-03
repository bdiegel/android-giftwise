package com.honu.giftwise;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                  .setMessage(getString(R.string.edit_gift_save_dialog_message))
                  .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          EditSizeActivity.super.onBackPressed();
                      }
                  })
                  .setNegativeButton("No", null)
                  .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        EditSizeFragment fragment = (EditSizeFragment) getSupportFragmentManager().findFragmentByTag(EDIT_SIZE_FRAGMENT_TAG);
        if (fragment == null) {
            return false;
        } else {
            return fragment.hasUnsavedChanges();
        }
    }
}
