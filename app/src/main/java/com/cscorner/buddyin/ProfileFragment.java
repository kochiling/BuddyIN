package com.cscorner.buddyin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

public class ProfileFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tabLayout = view.findViewById(R.id.profiletab);
        viewPager = view.findViewById(R.id.profile_vpager);

        tabLayout.setupWithViewPager(viewPager);

        BuddyPageAdapter vpAdapter = new BuddyPageAdapter(getChildFragmentManager());

        vpAdapter.addfragment(new PostProfileFragment(), "Post");
        vpAdapter.addfragment(new NotesProfileFragment(), "Notes");
        viewPager.setAdapter(vpAdapter);

        return view;
    }
}