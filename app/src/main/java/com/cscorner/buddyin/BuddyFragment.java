package com.cscorner.buddyin;

import static androidx.fragment.app.FragmentPagerAdapter.*;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BuddyFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_buddy, container, false);

        tabLayout = view.findViewById(R.id.buddytab);
        viewPager = view.findViewById(R.id.buddy_vpager);

        tabLayout.setupWithViewPager(viewPager);

        BuddyPageAdapter vpAdapter = new BuddyPageAdapter(getChildFragmentManager());

        vpAdapter.addfragment(new BuddyChatFragment(), "Buddy Chat");
        vpAdapter.addfragment(new BuddyListFragment(), "Buddy List");
        viewPager.setAdapter(vpAdapter);

        return view;
    }
}
