package ca.knowtime.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import ca.knowtime.DatabaseHandler;
import ca.knowtime.R;
import ca.knowtime.Route;
import ca.knowtime.RouteMapActivity;
import ca.knowtime.Stop;
import ca.knowtime.StopsActivity;

import java.util.List;

public class FavouritesFragment
        extends Fragment
{
    TableLayout stopTable;
    TableLayout routeTable;


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        loadFavouriteStops();
        loadFavouriteRoutes();
    }


    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        final View view = inflater.inflate( R.layout.activity_favourite, container, false );

        stopTable = (TableLayout) view.findViewById( R.id.stopsTable );
        routeTable = (TableLayout) view.findViewById( R.id.routesTable );
        return view;
    }


    private void loadFavouriteStops() {
        List<Stop> stops = DatabaseHandler.getInstance( getActivity() ).getAllFavouriteStops();
        for( final Stop stop : stops ) {
            // Get favourite stops some how
            LayoutInflater li = (LayoutInflater) getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View stopRow = li.inflate( R.layout.favouritecellview, null );
            final String stopNumber = stop.getCode();
            final String stopName = stop.getName();
            stopRow.setOnTouchListener( new View.OnTouchListener()
            {
                @Override
                public boolean onTouch( View v, MotionEvent event ) {
                    if( event.getAction() == MotionEvent.ACTION_DOWN ) {
                        Intent intent = new Intent( getActivity(), StopsActivity.class );
                        intent.putExtra( "STOP_NUMBER", stopNumber );
                        intent.putExtra( "STOP_NAME", stopName );
                        startActivity( intent );
                    }
                    return false;
                }
            } );
            stopTable.addView( stopRow, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT ) );
            TextView stopNumberTextView = (TextView) stopRow.findViewById( R.id.favouriteId );
            stopNumberTextView.setText( stopNumber );
            TextView stopNameTextView = (TextView) stopRow.findViewById( R.id.favouriteDescription );
            stopNameTextView.setText( stopName );
            final ImageButton favouriteButton = (ImageButton) stopRow.findViewById( R.id.favouriteButton );
            favouriteButton.setSelected( true );
            favouriteButton.setOnTouchListener( new View.OnTouchListener()
            {
                @Override
                public boolean onTouch( View v, MotionEvent event ) {
                    stop.setFavourite( !favouriteButton.isSelected() );
                    DatabaseHandler.getInstance( getActivity() ).updateStop( stop );
                    favouriteButton.setSelected( !favouriteButton.isSelected() );
                    return false;
                }
            } );
        }
    }


    private void loadFavouriteRoutes() {
        List<Route> routes = DatabaseHandler.getInstance( getActivity() ).getAllFavouriteRoutes();
        for( final Route route : routes ) {
            // Get favourite routes some how
            LayoutInflater li = getActivity().getLayoutInflater();
            View stopRow = li.inflate( R.layout.favouritecellview, null );
            final String routeNumber = route.getShortName();
            String routeName = route.getLongName();
            final String routeId = route.getId();
            stopRow.setOnTouchListener( new View.OnTouchListener()
            {
                @Override
                public boolean onTouch( View v, MotionEvent event ) {
                    if( event.getAction() == MotionEvent.ACTION_DOWN ) {
                        Intent intent = new Intent( getActivity(), RouteMapActivity.class );
                        intent.putExtra( "ROUTE_ID", routeId );
                        intent.putExtra( "ROUTE_NUMBER", routeNumber );
                        startActivity( intent );
                    }
                    return false;
                }
            } );
            routeTable.addView( stopRow, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
                                                                     ViewGroup.LayoutParams.WRAP_CONTENT ) );
            TextView routeNumberTextView = (TextView) stopRow.findViewById( R.id.favouriteId );
            routeNumberTextView.setText( routeNumber );
            TextView routeNameTextView = (TextView) stopRow.findViewById( R.id.favouriteDescription );
            routeNameTextView.setText( routeName );
            final ImageButton favouriteButton = (ImageButton) stopRow.findViewById( R.id.favouriteButton );
            favouriteButton.setSelected( true );
            favouriteButton.setOnTouchListener( new View.OnTouchListener()
            {
                @Override
                public boolean onTouch( View v, MotionEvent event ) {
                    route.setFavourite( !favouriteButton.isSelected() );
                    DatabaseHandler.getInstance( getActivity() ).updateRoute( route );
                    favouriteButton.setSelected( !favouriteButton.isSelected() );
                    return false;
                }
            } );
        }
    }


    public void touchBackButton( View view ) {
        getActivity().finish();
    }
}
