package com.onecoder.device.adpater;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2016-10-25.
 */

public class FraPagerAdapter<T extends Fragment> extends FragmentPagerAdapter {

    private List<T> fragmentList;

    public FraPagerAdapter(FragmentManager fm, List fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList != null && position < fragmentList.size() ? fragmentList.get(position) : null;
    }

    @Override
    public int getCount() {
        return fragmentList != null ? fragmentList.size() : 0;
    }
}
