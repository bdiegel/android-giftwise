package com.honu.giftwise;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bdiegel on 2/24/15.
 */
public class ContactsUtils {

    public static final String DISPLAY_NAME = "display_name";
    public static final String LOOKUP_URI = "lookup_uri";

    private static final String LOG_TAG = ContactsUtils.class.getSimpleName();

    // Query parameters for loading RawContacts
    public static class SimpleRawContactQuery {

        // query projection for contact profile
        static final String[] projection = new String[]{
              ContactsContract.RawContacts._ID,
              ContactsContract.RawContacts.CONTACT_ID,
              ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY
        };

        // sort by display name
        static final String sortOrder = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";

        // column ids for extracting data from cursor returned (matches project order)
        static final int COL_RAW_CONTACT_ID = 0;
        static final int COL_CONTACT_ID = 1;
        static final int COL_CONTACT_NAME = 2;
    }


    /**
     * Load RawContacts for specified account from content provider.
     *
     * @param context
     * @param accountName
     * @param accountType
     * @return
     */
    public static Loader<Cursor> loadRawContacts(Context context, String accountName, String accountType) {

        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
              .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
              .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
              .appendQueryParameter(ContactsContract.RawContacts.DELETED, "0")
              .build();


        return new CursorLoader(
              context,
              rawContactUri,
              SimpleRawContactQuery.projection,
              null,
              null,
              SimpleRawContactQuery.sortOrder
        );
    }


    public static Cursor queryRawContacts(Context context, String accountName, String accountType) {

        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
              .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
              .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
              .appendQueryParameter(ContactsContract.RawContacts.DELETED, "0")
              .build();

        return context.getContentResolver().query(rawContactUri,
            SimpleRawContactQuery.projection,
            null,
            null,
            SimpleRawContactQuery.sortOrder);
    }


    /**
     * Create a new RawContact for our app-specific account type.
     *
     * @param context
     * @param accountName
     * @param displayName
     */
    public static void createRawContact(Context context, String accountName, String displayName) {

        String accountType = context.getString(R.string.account_type);

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        int rawContactInsertIndex = ops.size();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
              .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
              .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
              .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
              .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
              .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
              .build());

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }


    }

    /**
     * List all the RawContacts from the content provider. Convenience function to quickly check contents.
     *
     * @param context
     */
    public static void readRawAccountTypes(Context context) {

        Cursor cursor = context.getContentResolver().query(
              ContactsContract.RawContacts.CONTENT_URI,
              new String[]{ContactsContract.RawContacts._ID, ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY},
              null,
              null,
              null
        );

        while (cursor.moveToNext()) {
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

    /**
     * Load all visible Contacts from the content provider
     *
     * @param context
     * @param projection
     * @return
     */
    public static Loader<Cursor> loadContacts(Context context, String[] projection) {

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

    /**
     * List all RawContacts for specified account. Convenient for checking contents.
     *
     * @param context
     * @param accountName
     * @param accountType
     */
    public static void printRawAccounts(Context context, String accountName, String accountType) {

        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
              .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
              .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
              .build();

        Cursor cursor =  context.getContentResolver().query(
              rawContactUri,
              new String[] {
                    ContactsContract.RawContacts._ID,
                    ContactsContract.RawContacts.ACCOUNT_NAME,
                    ContactsContract.RawContacts.ACCOUNT_TYPE,
                    ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY
              },
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
            Log.i(LOG_TAG, "Found raw account: id=" + id + " name=" + acctName + " type=" + acctType + " display=" + dispName);
        }
        cursor.close();
    }

    public static Bitmap getContactPhoto(Context context, int contactId)
    {
        ContentResolver cr = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);

        if (input == null) {
            return null;
        }

        return BitmapFactory.decodeStream(input);
    }

//    String[] selection = { ContactsContract.Contacts.DISPLAY_NAME,
//          ContactsContract.CommonDataKinds.Event.START_DATE,
//          ContactsContract.CommonDataKinds.Event.CONTACT_ID,
//          ContactsContract.CommonDataKinds.Event.TYPE };
//    String where = ContactsContract.Data.MIMETYPE + "= ? "
// + "AND " +  ContactsContract.CommonDataKinds.Event.TYPE + " IN (" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + ", " + ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY + ") " + "AND substr(" + ContactsContract.CommonDataKinds.Event.START_DATE + ", -5, 5)= ?";
//    String[] args = { ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, dateFormat };
    public static Cursor getContactSpecialDates(Context context, int contactId)
    {
        ContentResolver cr = context.getContentResolver();

        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;

            String[] projection = new String[] {
                  ContactsContract.Data.CONTACT_ID,
                  ContactsContract.CommonDataKinds.Event.START_DATE,
                  ContactsContract.Data.MIMETYPE,
                  ContactsContract.CommonDataKinds.Event.TYPE
            };

            String where = ContactsContract.Data.CONTACT_ID + "=?"
                  + " AND " + ContactsContract.Data.MIMETYPE + "=?"
                  + " AND " +  ContactsContract.CommonDataKinds.Event.TYPE +
                    " IN (" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + ", "
                            + ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY + ") ";
                  //+ " AND " + ContactsContract.CommonDataKinds.Event.TYPE + "=?";

            // Add contactId filter.
            String[] selectionArgs = new String[] {
                  String.valueOf(contactId),
                  ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                  //String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
            };

            String sortOrder = null;

            return cr.query(uri, projection, where, selectionArgs, sortOrder);
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(LOG_TAG, "Error reading dates: " + message);

            return null;
        }
    }

    // or: ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY, TYPE_OTHER, TYPE_CUSTOM
    public static Cursor getContactBirthday(Context context, int contactId)
    {
        ContentResolver cr = context.getContentResolver();

        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;

            String[] projection = new String[] {
                  ContactsContract.Data.CONTACT_ID,
                  ContactsContract.CommonDataKinds.Event.START_DATE,
                  ContactsContract.Data.MIMETYPE,
                  ContactsContract.CommonDataKinds.Event.TYPE
            };

            String where = ContactsContract.Data.CONTACT_ID + "=?"
                  + " AND " + ContactsContract.Data.MIMETYPE + "=?"
                  + " AND " + ContactsContract.CommonDataKinds.Event.TYPE + "=?";

            // Add contactId filter.
            String[] selectionArgs = new String[] {
                  String.valueOf(contactId),
                  ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                  String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
            };

            String sortOrder = null;

            return cr.query(uri, projection, where, selectionArgs, sortOrder);
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.d(LOG_TAG, "Error: " + message);

            return null;
        }
    }

//    String [] PROJECTION = new String [] {  ContactsContract.Contacts.LOOKUP_KEY };
//
//    Cursor cursor = this.managedQuery(ContactsContract.Contacts.CONTENT_URI, PROJECTION, null, null, null);
//
//
//    for(cursor.moveToFirst(); cursor.moveToNext(); cursor.isAfterLast()) {
//        Log.d(LOG_TAG, "lookupKey for contact:  " + cursor.getString(1) + ", is: " + cursor.getString(0));
//    }


    public static String formatPrice(Context context, String currencyCode,
                                     double price) {
//        if (price == 0) {
//            return context.getString(R.string.free);
//        }
        Currency currency = Currency.getInstance(currencyCode);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(currency);
        return format.format(price);
    }

    public static Account getOrCreateAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
              context.getString(R.string.account_name),
              context.getString(R.string.account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount) ) {

            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            //onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

}
