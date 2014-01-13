package ca.knowtime;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.knowtime.fragments.AboutFragment;
import ca.knowtime.fragments.FavouritesFragment;
import ca.knowtime.fragments.MapFragment;
import ca.knowtime.fragments.ShareMeFragment;
import ca.knowtime.fragments.StopsFragment;
import ca.knowtime.fragments.TwitterFragment;

public class DrawerActivity
        extends Activity
{
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.drawer_activity_main );

        mPlanetTitles = getResources().getStringArray( R.array.planets_array );

        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        mDrawerList = (ListView) findViewById( R.id.left_drawer );
        mDrawerList.setAdapter( new ArrayAdapter<String>( this, R.layout.drawer_item, mPlanetTitles ) );
        mDrawerList.setOnItemClickListener( new DrawerItemClickListener() );


        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace( R.id.content_frame, new MapFragment() ).commit();
    }


    private class DrawerItemClickListener
            implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick( AdapterView parent, View view, int position, long id ) {
            selectItem( position );
        }
    }


    /** Swaps fragments in the main content view */
    private void selectItem( int position ) {
        // Create a new fragment and specify the planet to show based on position
        //        Fragment fragment = new PlanetFragment();
        //        Bundle args = new Bundle();
        //        args.putInt( PlanetFragment.ARG_PLANET_NUMBER, position );
        //        fragment.setArguments( args );
        //
        //        // Insert the fragment by replacing any existing fragment
        //        FragmentManager fragmentManager = getFragmentManager();
        //        fragmentManager.beginTransaction().replace( R.id.content_frame, fragment ).commit();
        //
        //        // Highlight the selected item, update the title, and close the drawer
        //        mDrawerList.setItemChecked( position, true );
        //        setTitle( mPlanetTitles[position] );

        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace( R.id.content_frame, getFragment( position ) ).commit();
        mDrawerLayout.closeDrawer( mDrawerList );
    }


    private Fragment getFragment( final int position ) {
        switch( position ) {
            case 0:
                return new ShareMeFragment();
            case 1:
                return new AboutFragment();
            case 2:
                return new StopsFragment();
            case 3:
                return new FavouritesFragment();
            case 4:
                return new TwitterFragment();
            default:
                throw new IllegalArgumentException( "Unknown drawer item, pos: " + position );
        }
    }


    @Override
    public void setTitle( CharSequence title ) {
        //        mTitle = title;
        //        getActionBar().setTitle( mTitle );
    }
}
