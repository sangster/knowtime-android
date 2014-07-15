package ca.knowtime;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import ca.knowtime.adapters.WelcomePagerAdapter;
import ca.knowtime.views.BlockableViewPager;
import ca.knowtime.views.listeners.OnBlockableTouchEventListener;

public class WelcomeActivity
        extends FragmentActivity
{
    private BlockableViewPager mPager;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.welcome_activity );

        mPager = (BlockableViewPager) findViewById( R.id.pager );
        mPager.setAdapter( new WelcomePagerAdapter( getSupportFragmentManager() ) );
        mPager.setOnTouchEventBlockedListener( new MyOnBlockableTouchEventListener() );
    }


    @Override
    public void onBackPressed() {
        if( mPager.getCurrentItem() != 0 ) {
            mPager.setCurrentItem( mPager.getCurrentItem() - 1 );
        } else {
            super.onBackPressed();
        }
    }


    private class MyOnBlockableTouchEventListener
            implements OnBlockableTouchEventListener
    {
        @Override
        public boolean onBlockableTouchEvent() {
            if( mPager.getCurrentItem() == WelcomePagerAdapter.PAGE_CHOOSE_DATA_SET ) {

            }
            return false;
        }
    }
}
