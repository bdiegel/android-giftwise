package com.honu.giftwise;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by bdiegel on 2/24/15.
 */
public class ContactsUtils {

    private static final String LOG_TAG = ContactsUtils.class.getSimpleName();

    public static void readRawAccountTypes(Context context) {
        String mAcccountName = "*";
        String mAccountType = "*";

        Cursor cursor =  context.getContentResolver().query(
              ContactsContract.RawContacts.CONTENT_URI,
              new String[] { ContactsContract.RawContacts._ID, ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY },
              null,
              null,
              null
        );

        while (cursor.moveToNext())
        {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
            String acctName = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
            String acctType = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
            String dispName = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
            Log.i(LOG_TAG, "Found account: id=" + id + " name=" + acctName + " type=" + acctType + " display=" + dispName);
        }
        cursor.close();
    }

    public static String getUsername(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            // TODO: Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type
            // values.

            String possibleEmail = account.name;
            String type = account.type;

            if (type.equals("com.google")) {
                Log.e("", "Emails: " + possibleEmail);
                break;
            }

            possibleEmails.add(possibleEmail);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");
            if (parts.length > 0 && parts[0] != null)
                return parts[0];
            else
                return null;
        } else
            return null;
    }

    private Loader<Cursor> loadContacts(Context context, String[] projection) {
        // defined query arguments:
        final Uri uri = ContactsContract.Contacts.CONTENT_URI;
        final String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
        final String[] selectionArgs = null;
        final String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return new CursorLoader(
              context,
              uri,
              projection,
              selection,
              selectionArgs,
              sortOrder
        );
    }
}
