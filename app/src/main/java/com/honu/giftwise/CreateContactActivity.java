package com.honu.giftwise;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.honu.giftwise.data.ContactsUtils;


public class CreateContactActivity extends AppCompatActivity {

    private static final String LOG_TAG = CreateContactActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_action_accept);
        }

        if (savedInstanceState == null) {
            CreateContactFragment fragment = new CreateContactFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                  .add(R.id.container, fragment)
                  .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // navigation icon selected (done)
        if (id == android.R.id.home) {
            EditText nameEditText = (EditText) findViewById(R.id.contact_display_name);
            createRawContact(nameEditText.getText().toString());
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Create a new RawContact for this account name and type
     */
    private void createRawContact(final String displayName) {
        Log.i(LOG_TAG, "display name: " + displayName);

        if (TextUtils.isEmpty(displayName)) {
            Toast.makeText(this, "Enter a valid contact name",Toast.LENGTH_LONG ).show();
            return;
        }

        final String accountName = getResources().getString(R.string.account_name);

        // Create RawContact
        new Thread(new Runnable() {

            @Override
            public void run() {
                ContactsUtils.createRawContact(CreateContactActivity.this, accountName, displayName);
            }
        }).start();

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class CreateContactFragment extends Fragment {

        public CreateContactFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_create_contact, container, false);

            // If arguments have been passed we are importing a selected contact. Otherwise, a new contact was created.
            Bundle args = getArguments();
            if (args != null) {
                String displayName = args.getString(ContactsUtils.DISPLAY_NAME);

                if (!TextUtils.isEmpty(displayName)) {
                    Log.d(LOG_TAG, "Set displayName: " + displayName);
                    EditText nameEditText = (EditText) rootView.findViewById(R.id.contact_display_name);

                    // hide keyboard and don't let user edit name
                    nameEditText.setKeyListener(null);
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                    nameEditText.setText(displayName);
                }
            }

            if (savedInstanceState != null) {
                EditText nameEditText = (EditText) rootView.findViewById(R.id.contact_display_name);
                nameEditText.setText(savedInstanceState.getString("contact_display_name"));
            }

            return rootView;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            EditText nameEditText = (EditText) getView().findViewById(R.id.contact_display_name);
            outState.putString("contact_display_name", nameEditText.getText().toString());
        }
    }
}
