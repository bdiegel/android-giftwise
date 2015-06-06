package com.honu.giftwise.service;


import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.honu.giftwise.data.ContactsUtils;
import com.honu.giftwise.data.DbHelper;

import java.io.IOException;

public class BackupAgent extends BackupAgentHelper {

    private static final String LOG_TAG = BackupAgent.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate called");

        FileBackupHelper giftwise_database = new FileBackupHelper(this, "../databases/" + DbHelper.DATABASE_NAME);
        addHelper(DbHelper.DATABASE_NAME, giftwise_database);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        Log.d(LOG_TAG, "onBackup called");

        synchronized (DbHelper.dbLock) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        Log.d(LOG_TAG, "onRestore called");

        synchronized (DbHelper.dbLock) {
            Log.d(LOG_TAG, "onRestore in-lock");

            super.onRestore(data, appVersionCode, newState);

            ContactsUtils.restoreRawContacts(this.getApplicationContext());
        }
    }

}
