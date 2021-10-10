package com.sportschule.rootfit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sportschule.rootfit.Trainee.TraineeFragmentRegister;

public class PageAdapterRegister extends FragmentPagerAdapter {
    private final int numberOfTabs;
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new TraineeFragmentRegister();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
    public PageAdapterRegister(FragmentManager fm, int numberoftabs){
        super(fm);
        this.numberOfTabs=numberoftabs;
    }
}
