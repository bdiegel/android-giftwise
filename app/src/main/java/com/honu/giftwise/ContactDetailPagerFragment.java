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

    private static final String LOG_TAG = ContactDetailPagerFragment.class.getSimpleName();

    private String mContactName;
    private long mRawContactId;
    private long mContactId;
    private String mGiftwiseId;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.contact_pager, container, false);
        final ViewPager pager = (ViewPager) result.findViewById(R.id.pager);
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
        mGiftwiseId = args.getString("gwId");

        pager.setAdapter(buildAdapter());

        // restore page selection
        if (savedInstanceState != null)  {
            int position = savedInstanceState.getInt("position");
            pager.setCurrentItem(position);
        }

        tabs.setViewPager(pager);

        return(result);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save selection tab position
        View rootView = getView();
        ViewPager pager= (ViewPager) rootView.findViewById(R.id.pager);

        // save values to bundle
        outState.putInt("position", pager.getCurrentItem());
    }

    private PagerAdapter buildAdapter() {
        return(new ContactDetailPagerAdapter(getChildFragmentManager(), getActivity(), mContactName, mRawContactId, mContactId, mGiftwiseId));
    }
}
