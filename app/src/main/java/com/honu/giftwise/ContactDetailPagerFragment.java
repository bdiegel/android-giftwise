package com.honu.giftwise;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honu.giftwise.view.SlidingTabLayout;

public class ContactDetailPagerFragment extends Fragment {

    private String mContactName;
    private long mRawContactId;
    private long mContactId;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View result=inflater.inflate(R.layout.contact_pager, container, false);
        ViewPager pager=(ViewPager)result.findViewById(R.id.pager);
        SlidingTabLayout tabs = (SlidingTabLayout) result.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);

        tabs.setSelectedIndicatorColors(R.color.selector);

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        Bundle args = getArguments();
        mContactName = args.getString("name");
        mRawContactId = args.getLong("rawId");
        mContactId = args.getLong("contactId");

        pager.setAdapter(buildAdapter());
        tabs.setViewPager(pager);

        return(result);
    }

    private PagerAdapter buildAdapter() {
        return(new ContactDetailPagerAdapter(getChildFragmentManager(), getActivity(), mContactName, mRawContactId, mContactId));
    }
}
