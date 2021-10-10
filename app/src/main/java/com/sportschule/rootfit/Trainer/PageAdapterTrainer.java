package com.sportschule.rootfit.Trainer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PageAdapterTrainer extends FragmentPagerAdapter {
    private final int numberOfTabs;
    public PageAdapterTrainer(@NonNull FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.numberOfTabs=numberOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new TrainerFragmentHome();
            case 1:
                return new TrainerFragmentTraining();
            case 2:
                return new TrainerFragmentPastTraining();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
