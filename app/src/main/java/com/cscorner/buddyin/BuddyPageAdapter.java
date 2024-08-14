package com.cscorner.buddyin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class BuddyPageAdapter extends FragmentStateAdapter {


    public BuddyPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new BuddyRecommendFragment();
        } else if (position == 1) {
            return new BuddyListFragment();
        }
        return new BuddyRecommendFragment(); // Return a default fragment if position is invalid
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs
    }
}


//    private final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
//    private final ArrayList<String> fragmentTitle = new ArrayList<>();
//
//    public BuddyPageAdapter(@NonNull FragmentManager fm) {
//        super(fm);
//    }
//
//    @NonNull
//    @Override
//    public Fragment getItem(int position) {
//        return fragmentArrayList.get(position);
//    }
//
//    @Override
//    public int getCount() {
//        return fragmentArrayList.size();
//    }
//
//    public void addfragment(Fragment fragment, String title) {
//        fragmentArrayList.add(fragment);
//        fragmentTitle.add(title);
//    }
//
//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return fragmentTitle.get(position);
//    }
