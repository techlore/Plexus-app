package tech.techlore.plexus.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import tech.techlore.plexus.fragments.main.InstalledAppsFragment;
import tech.techlore.plexus.fragments.main.PlexusDataFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PlexusDataFragment();
        }
        else {
            return new InstalledAppsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
