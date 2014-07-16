package ca.knowtime.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.widget.Toast;
import ca.knowtime.R;
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
        public boolean onBlockableTouchEvent( final MotionEvent ev ) {
            if( mPager.getCurrentItem() == WelcomePagerAdapter.PAGE_CHOOSE_DATA_SET && !isDataSetNameSet() ) {
                Toast.makeText( WelcomeActivity.this,
                                R.string.please_select_data_set,
                                Toast.LENGTH_LONG );
                return true;
            }
            return false;
        }


        private boolean isDataSetNameSet() {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                    WelcomeActivity.this );
            return pref.contains( "data_set" );
        }
    }
}
