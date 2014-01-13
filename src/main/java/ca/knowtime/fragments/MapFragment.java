package ca.knowtime.fragments;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import ca.knowtime.DatabaseHandler;
import ca.knowtime.R;
import ca.knowtime.StopsMarkerLoader;
import ca.knowtime.WebApiService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MapFragment
        extends Fragment
{
    public static final LatLng DEFAULT_HALIFAX_LAT_LNG = new LatLng( 44.67600, -63.60800 );
    public static final int DEFAULT_HALIFAX_LAT_LNG_ZOOM = 15;

    private GoogleMap mMap;
    private Context mContext;
    private HashMap<String, Marker> mBusStopMarkers = new HashMap<String, Marker>();
    private Boolean mShowStops;
    private ProgressBar mMapMarkerProgressBar;
    private TextView mMapMarkerProgressBarText;


    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        DatabaseHandler.getInstance( getActivity() );
        mContext = getActivity();
        mShowStops = true;
    }


    @Override
    public View onCreateView( final LayoutInflater inflater, final ViewGroup container,
                              final Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.activity_main, container, false );

        mMapMarkerProgressBar = (ProgressBar) view.findViewById( R.id.mapMarkerProgressBar );
        mMapMarkerProgressBarText = (TextView) view.findViewById( R.id.mapMarkerProgressBarText );
        WebApiService.fetchAllRoutes();

        if( mMap == null ) {
            mMap = ((com.google.android.gms.maps.MapFragment) getFragmentManager().findFragmentById(
                    R.id.map1 )).getMap();

            if( mMap != null ) {
                mMap.setMyLocationEnabled( true );

                LocationManager locationManager = (LocationManager) getActivity().getSystemService(
                        Context.LOCATION_SERVICE );
                String locationProvider = LocationManager.NETWORK_PROVIDER;
                Location lastKnownLocation = locationManager.getLastKnownLocation( locationProvider );
                mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(
                        new LatLng( lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude() ),
                        DEFAULT_HALIFAX_LAT_LNG_ZOOM ) );

                mMap.setOnCameraChangeListener( new GoogleMap.OnCameraChangeListener()
                {
                    @Override
                    public void onCameraChange( final CameraPosition cameraPosition ) {
                        refreshBusStopMarkers( cameraPosition );
                    }
                } );
                mMap.setOnInfoWindowClickListener( new InfoWindowClickListener() );
            }
        }

        return view;
    }


    private void refreshBusStopMarkers( CameraPosition position ) {
        if( position == null ) {
            position = mMap.getCameraPosition();
        }

        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng bottom = bounds.southwest;
        LatLng top = bounds.northeast;
        float zoom = position.zoom;

        Bundle b = new Bundle();
        b.putDouble( "bottomLat", bottom.latitude );
        b.putDouble( "bottomLog", bottom.longitude );
        b.putDouble( "topLat", top.latitude );
        b.putDouble( "topLog", top.longitude );
        b.putFloat( "zoom", zoom );
        b.putBoolean( "showStops", mShowStops );

        getLoaderManager().restartLoader( 0, b, getMoo() ).forceLoad();
    }


    private LoaderManager.LoaderCallbacks<HashMap<String, MarkerOptions>> getMoo() {
        return new LoaderManager.LoaderCallbacks<HashMap<String, MarkerOptions>>()
        {
            @Override
            public Loader<HashMap<String, MarkerOptions>> onCreateLoader( final int id, final Bundle args ) {
                setStopMarkerProgressBar( true );
                return new StopsMarkerLoader( mContext, args.getDouble( "bottomLat" ), args.getDouble( "bottomLog" ),
                                              args.getDouble( "topLat" ), args.getDouble( "topLog" ),
                                              args.getFloat( "zoom" ), mShowStops );
            }


            @Override
            public void onLoadFinished( final Loader<HashMap<String, MarkerOptions>> loader,
                                        final HashMap<String, MarkerOptions> marker ) {
                setStopMarkerProgressBar( false );
                addItemsToMap( marker );
            }


            private void addItemsToMap( HashMap<String, MarkerOptions> result ) {
                final Object[] currentStops = mBusStopMarkers.keySet().toArray();
                Marker marker;
                for( Object currentStop : currentStops ) {
                    marker = mBusStopMarkers.get( currentStop );

                    if( !result.containsKey( currentStop ) && !marker.isInfoWindowShown() ) {
                        marker.remove();
                        mBusStopMarkers.remove( currentStop );
                    } else {
                        result.remove( currentStop );
                    }
                }
                if( mShowStops ) {
                    for( String newStop : result.keySet() ) {
                        mBusStopMarkers.put( newStop, mMap.addMarker( result.get( newStop ) ) );
                    }
                }
            }


            @Override
            public void onLoaderReset( final Loader<HashMap<String, MarkerOptions>> loader ) {
            }


            private void setStopMarkerProgressBar( boolean b ) {
                mMapMarkerProgressBar.setVisibility( b ? View.VISIBLE : View.INVISIBLE );
                mMapMarkerProgressBarText.setVisibility( b ? View.VISIBLE : View.INVISIBLE );
            }
        };
    }


    private class InfoWindowClickListener
            implements GoogleMap.OnInfoWindowClickListener
    {
        @Override
        public void onInfoWindowClick( Marker marker ) {
            Intent intent = new Intent( getActivity(), StopsFragment.class );
            intent.putExtra( "STOP_NUMBER", marker.getSnippet() );
            intent.putExtra( "STOP_NAME", marker.getTitle() );
            startActivity( intent );
        }
    }
}
