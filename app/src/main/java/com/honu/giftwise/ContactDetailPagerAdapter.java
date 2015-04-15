package com.honu.giftwise;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ContactDetailPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    private String[] mTabTitles;

    private String mContactName;
    private long mRawContactId;
    private long mContactId;

    public ContactDetailPagerAdapter(FragmentManager fm, Context context, String contactName, long rawContactId, long contactId) {
        super(fm);

        mContext = context;
        mContactName = contactName;
        mRawContactId = rawContactId;
        mContactId = contactId;

        mTabTitles = mContext.getResources().getStringArray(R.array.contact_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return IdeasFragment.getInstance(mRawContactId, mContactName);
            case 1:
                return ProfileFragment.getInstance(mRawContactId, mContactId);
        }

        return  null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }
}


