package ca.knowtime.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import ca.knowtime.fragments.welcome.WelcomeFinishFragment;
import ca.knowtime.fragments.welcome.WelcomeChooseDataSetFragment;
import ca.knowtime.fragments.welcome.WelcomeStartFragment;

public class WelcomePagerAdapter
        extends FragmentStatePagerAdapter
{
    public static final int NUM_PAGES = 3;

    public static final int PAGE_START = 0;
    public static final int PAGE_CHOOSE_DATA_SET = 1;
    public static final int PAGE_FINISH = 2;


    public WelcomePagerAdapter( FragmentManager fm ) {
        super( fm );
    }


    @Override
    public Fragment getItem( int position ) {
        switch( position ) {
            case PAGE_START:
                return new WelcomeStartFragment();
            case PAGE_CHOOSE_DATA_SET:
                return new WelcomeChooseDataSetFragment();
            case PAGE_FINISH:
                return new WelcomeFinishFragment();
            default:
                throw new IllegalStateException( "unknown pager position" );
        }
    }


    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
