package com.cscorner.buddyin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfileViewPagerAdapter extends FragmentStateAdapter {

    public ProfileViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PostProfileFragment();
            case 1:
                return new NotesProfileFragment();
            default:
                return new PostProfileFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
