package ca.knowtime.activities;

import android.app.Dialog;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import ca.knowtime.Development;
import ca.knowtime.R;
import ca.knowtime.adapters.StopRoutesViewAdapter;
import ca.knowtime.comm.KnowTime;
import ca.knowtime.comm.KnowTimeAccess;
import ca.knowtime.comm.Response;
import ca.knowtime.comm.models.gtfs.Route;
import ca.knowtime.comm.models.gtfs.Stop;
import ca.knowtime.fragments.ErrorDialogFragment;
import ca.knowtime.listeners.MyLocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Optional;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ca.knowtime.LocationUtils.APPTAG;
import static ca.knowtime.LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static ca.knowtime.LocationUtils.FAST_INTERVAL_CEILING_IN_MS;
import static ca.knowtime.LocationUtils.UPDATE_INTERVAL_IN_MS;
import static ca.knowtime.ViewHolder.cacheView;
import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

public class NearbyStopsActivity
        extends FragmentActivity
{
    public static final float DEFAULT_MAP_ZOOM = 17f;
    private static final float MIN_ZOOM_SHOW_STOPS = 14f;
    public final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd" );

    private MapFragment mMapFragment;
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    private KnowTimeAccess mAccess;
    private Map<String, Marker> mVisibleMarkers = Collections.emptyMap();
    private Map<String, Stop> mMarkerStops = Collections.emptyMap();
    private Optional<Stop> mLastStop = Optional.absent();
    private List<Route> mLastStopRoutes = Collections.emptyList();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawer;


    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.nearby_stops_activity );
        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        mDrawerLayout.setScrimColor( R.color.drawer_scrim_color );

        mDrawer = (ListView) findViewById( R.id.left_drawer );
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById( R.id.nearby_stops_map );
        setUpMapView();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval( UPDATE_INTERVAL_IN_MS );
        mLocationRequest.setPriority( PRIORITY_HIGH_ACCURACY );
        mLocationRequest.setFastestInterval( FAST_INTERVAL_CEILING_IN_MS );

        mLocationListener = new MyLocationListener();

        mLocationClient = new LocationClient( this,
                                              new MyConnectionCallbacks(),
                                              new MyOnConnectionFailedListener() );
    }


    private void setUpMapView() {
        MapsInitializer.initialize( this );
        final GoogleMap map = mMapFragment.getMap();
        map.setMyLocationEnabled( true );
        map.setOnCameraChangeListener( new MyOnCameraChangeListener() );
        map.setInfoWindowAdapter( new StopMarkerInfoWindowAdapter() );
        map.setOnMarkerClickListener( new MyOnMarkerClickListener() );
        map.setOnInfoWindowClickListener( new MyOnInfoWindowClickListener() );
        map.setMapType( GoogleMap.MAP_TYPE_NORMAL );
    }


    private void updateRoutesList( final Marker marker,
                                   final ListView routesView,
                                   final Stop stop ) {
        if( mLastStop.isPresent() && mLastStop.get().equals( stop ) ) {
            final StopRoutesViewAdapter adapter = new StopRoutesViewAdapter(
                    getLayoutInflater(),
                    mLastStopRoutes );
            routesView.setAdapter( adapter );
            adapter.notifyDataSetChanged();
            routesView.invalidateViews();
        } else {
            mAccess.routesForStop( NearbyStopsActivity.this,
                                   getDataSetId(),
                                   dateToday(),
                                   stop.getId(),
                                   new RoutesForStopResponse( stop, marker ) );
        }
    }


    private String dateToday() {
        return DATE_FORMAT.format( new Date() );
    }


    @Override
    public void onStart() {
        super.onStart();
        mAccess = KnowTime.connect( this, Development.BASE_URL );
        mLocationClient.connect();
    }


    private void animateMapCamera( final Location location ) {
        final LatLng loc = new LatLng( location.getLatitude(),
                                       location.getLongitude() );
        final CameraUpdate update = newLatLngZoom( loc, DEFAULT_MAP_ZOOM );
        mMapFragment.getMap().animateCamera( update );
    }


    @Override
    protected void onPause() {
        super.onPause();
        mAccess.cancel( this );
    }


    @Override
    public void onStop() {
        if( mLocationClient.isConnected() ) {
            stopPeriodicUpdates();
        }
        mLocationClient.disconnect();
        super.onStop();
    }


    /** @return true if Google Play services is available, otherwise false */
    private boolean servicesConnected() {
        final int resultCode = isGooglePlayServicesAvailable( this );

        if( ConnectionResult.SUCCESS == resultCode ) {
            Log.d( APPTAG, getString( R.string.play_services_available ) );
            return true;
        } else {
            final int code = 0;
            showErrorFragment( resultCode, code );
            return false;
        }
    }


    private void showErrorFragment( final int errorCode,
                                    final int requestCode ) {
        Dialog dialog = getErrorDialog( errorCode, this, requestCode );
        if( dialog != null ) {
            final ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog( dialog );
            errorFragment.show( getFragmentManager(), APPTAG );
        }
    }


    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates( mLocationListener );
    }


    private String getDataSetId() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                this );
        return pref.getString( "data_set", null );
    }


    private class MyOnCameraChangeListener
            implements GoogleMap.OnCameraChangeListener
    {
        @Override
        public void onCameraChange( final CameraPosition cameraPosition ) {
            if( cameraPosition.zoom > MIN_ZOOM_SHOW_STOPS ) {
                updateStops();
            } else {
                mMapFragment.getMap().clear();
            }
        }


        private void updateStops() {
            final String dataSetId = getDataSetId();
            if( dataSetId == null ) {
                Log.i( "Stops", "Data Set is not chosen" );
                return;
            }

            final LatLngBounds bounds = mMapFragment.getMap()
                                                    .getProjection()
                                                    .getVisibleRegion().latLngBounds;
            mAccess.stopsInBounds( this,
                                   dataSetId,
                                   (float) bounds.northeast.latitude,
                                   (float) bounds.northeast.longitude,
                                   (float) bounds.southwest.latitude,
                                   (float) bounds.southwest.longitude,
                                   new StopsResponse() );
        }
    }

    private class StopsResponse
            extends Response<List<Stop>>
    {
        @Override
        public void onResponse( final List<Stop> response ) {
            final GoogleMap map = mMapFragment.getMap();

            final Map<String, Marker> markers = new HashMap<>();
            final Map<String, Stop> stops = new HashMap<>();

            for( final Stop stop : response ) {
                final Marker marker = getOrCreateMarker( map, stop );
                markers.put( stop.getId(), marker );
                stops.put( marker.getId(), stop );
            }
            removeVisibleMarkersNotInMap( markers );
            mVisibleMarkers = markers;
            mMarkerStops = stops;
        }


        private Marker getOrCreateMarker( final GoogleMap map,
                                          final Stop stop ) {
            final String id = stop.getId();
            if( mVisibleMarkers.containsKey( id ) ) {
                return mVisibleMarkers.get( id );
            } else {
                return map.addMarker( createMarkerOptions( stop ) );
            }
        }


        private void removeVisibleMarkersNotInMap( final Map<String, Marker> markers ) {
            for( final String id : mVisibleMarkers.keySet() ) {
                if( !markers.containsKey( id ) ) {
                    mVisibleMarkers.get( id ).remove();
                }
            }
        }


        private MarkerOptions createMarkerOptions( final Stop stop ) {
            final LatLng loc = new LatLng( stop.getLatitude(),
                                           stop.getLongitude() );
            return new MarkerOptions().position( loc )
                                      .draggable( false )
                                      .title( stop.getName() );
        }
    }

    private class MyConnectionCallbacks
            implements GooglePlayServicesClient.ConnectionCallbacks
    {
        @Override
        public void onConnected( Bundle bundle ) {
            if( servicesConnected() ) {
                final Location location = mLocationClient.getLastLocation();
                if( location != null ) {
                    animateMapCamera( location );
                }
            }
        }


        @Override
        public void onDisconnected() {
        }
    }

    private class MyOnConnectionFailedListener
            implements GooglePlayServicesClient.OnConnectionFailedListener
    {
        @Override
        public void onConnectionFailed( final ConnectionResult result ) {
            if( result.hasResolution() ) {
                try {
                    result.startResolutionForResult( NearbyStopsActivity.this,
                                                     CONNECTION_FAILURE_RESOLUTION_REQUEST );
                } catch( IntentSender.SendIntentException e ) {
                    e.printStackTrace();
                }
            } else {
                showErrorFragment( result.getErrorCode(),
                                   CONNECTION_FAILURE_RESOLUTION_REQUEST );
            }
        }
    }

    private class StopMarkerInfoWindowAdapter
            implements GoogleMap.InfoWindowAdapter
    {
        @Override
        public View getInfoContents( final Marker marker ) {
            final View view = getLayoutInflater().inflate( R.layout.map_info_stop,
                                                           null );
            final TextView nameView = cacheView( view,
                                                 R.id.map_info_stop_name );
            final TextView descView = cacheView( view,
                                                 R.id.map_info_stop_description );

            final Stop stop = mMarkerStops.get( marker.getId() );

            nameView.setText( stop.getId() + ": " + stop.getName() );
            if( stop.getDescription().isPresent() ) {
                descView.setVisibility( View.VISIBLE );
                descView.setText( stop.getDescription().get() );
            } else {
                descView.setVisibility( View.GONE );
            }
            return view;
        }


        @Override
        public View getInfoWindow( final Marker marker ) {
            return null; // will use getInfoContentsInstead()
        }
    }

    private class RoutesForStopResponse
            extends Response<List<Route>>
    {
        private final Stop mStop;
        private final Marker mMarker;


        public RoutesForStopResponse( final Stop stop, final Marker marker ) {
            mStop = stop;
            mMarker = marker;
        }


        @Override
        public void onResponse( final List<Route> routes ) {
            // Recreate the view
            mLastStop = Optional.of( mStop );
            mLastStopRoutes = routes;
            updateRoutesList( mMarker, mDrawer, mStop );
        }
    }

    private class MyOnInfoWindowClickListener
            implements GoogleMap.OnInfoWindowClickListener
    {
        @Override
        public void onInfoWindowClick( final Marker marker ) {
            mDrawerLayout.openDrawer( Gravity.START );
        }
    }

    private class MyOnMarkerClickListener
            implements GoogleMap.OnMarkerClickListener
    {
        @Override
        public boolean onMarkerClick( final Marker marker ) {
            updateRoutesList( marker,
                              mDrawer,
                              mMarkerStops.get( marker.getId() ) );
            return false;
        }
    }
}
