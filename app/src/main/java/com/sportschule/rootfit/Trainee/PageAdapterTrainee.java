package com.sportschule.rootfit.Trainee;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PageAdapterTrainee extends FragmentPagerAdapter {
    private final int numberOfTabs;
    public PageAdapterTrainee(@NonNull FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.numberOfTabs=numberOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new TraineeFragmentHome();
            case 1:
                return new TraineeFragmentTraining();
            case 2:
                return new TraineeFragmentUnbook();
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
