package ca.knowtime;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.knowtime.fragments.AboutFragment;
import ca.knowtime.fragments.FavouritesFragment;
import ca.knowtime.fragments.MapFragment;
import ca.knowtime.fragments.PrivacyPolicyFragment;
import ca.knowtime.fragments.ShareMeFragment;
import ca.knowtime.fragments.TwitterFragment;

public class DrawerActivity
        extends Activity
{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.drawer_activity_main );

        final String[] drawerActions = getResources().getStringArray( R.array.drawer_actions );

        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        mDrawerList = (ListView) findViewById( R.id.left_drawer );
        mDrawerList.setAdapter( new ArrayAdapter<String>( this, R.layout.drawer_item, drawerActions ) );
        mDrawerList.setOnItemClickListener( new DrawerItemClickListener() );

        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace( R.id.content_frame, new MapFragment() ).commit();
    }


    private class DrawerItemClickListener
            implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick( AdapterView parent, View view, int position, long id ) {
            if( position == 2 ) {
                final MapFragment map = (MapFragment) getFragmentManager().findFragmentById( R.id.content_frame );
                map.toggleStopsVisibility();
            } else {
                final FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.addToBackStack( null );
                transaction.replace( R.id.content_frame, createFragment( position ) );
                transaction.commit();

                mDrawerLayout.closeDrawer( mDrawerList );
            }
        }
    }


    private Fragment createFragment( final int position ) {
        switch( position ) {
            case 0:
                return new ShareMeFragment();
            case 1:
                return new AboutFragment();

            case 3:
                return new FavouritesFragment();
            case 4:
                return new TwitterFragment();
            case 5:
                return new PrivacyPolicyFragment();
            default:
                throw new IllegalArgumentException( "Unknown drawer item, pos: " + position );
        }
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.main_activity_actions, menu );
        return true;
    }
}
