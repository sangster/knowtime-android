package ca.knowtime.activities;

import android.app.Dialog;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import ca.knowtime.Development;
import ca.knowtime.R;
import ca.knowtime.comm.KnowTime;
import ca.knowtime.comm.KnowTimeAccess;
import ca.knowtime.comm.Response;
import ca.knowtime.comm.models.gtfs.Stop;
import ca.knowtime.fragments.ErrorDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import static ca.knowtime.LocationUtils.APPTAG;
import static ca.knowtime.LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static ca.knowtime.LocationUtils.FAST_INTERVAL_CEILING_IN_MS;
import static ca.knowtime.LocationUtils.UPDATE_INTERVAL_IN_MS;
import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

public class NearbyStopsActivity
        extends FragmentActivity
{
    public static final float DEFAULT_MAP_ZOOM = 17f;
    private static final float MIN_ZOOM_SHOW_STOPS = 12f;

    private MapFragment mMapFragment;
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    private KnowTimeAccess mAccess;


    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.nearby_stops_activity );
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
        map.setMapType( GoogleMap.MAP_TYPE_NORMAL );
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


    private void startPeriodicUpdates() {
        mLocationClient.requestLocationUpdates( mLocationRequest,
                                                mLocationListener );
    }


    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates( mLocationListener );
    }


    private static class MyLocationListener
            implements LocationListener
    {
        @Override
        public void onLocationChanged( final Location location ) {
            Log.i( APPTAG, "Location Changed: " + location.toString() );
        }
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
            Log.i( "Stops", "updateStops()" );

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                    NearbyStopsActivity.this );
            final String name = pref.getString( "data_set", null );
            if( name == null ) {
                Log.i( "Stops", "Data Set is not chosen" );
                return;
            }

            final LatLngBounds bounds = mMapFragment.getMap()
                                                    .getProjection()
                                                    .getVisibleRegion().latLngBounds;
            mAccess.stopsInBounds( this,
                                   name,
                                   (float) bounds.northeast.latitude,
                                   (float) bounds.northeast.longitude,
                                   (float) bounds.southwest.latitude,
                                   (float) bounds.southwest.longitude,
                                   new StopsResponse() );
        }


        private class StopsResponse
                extends Response<List<Stop>>
        {
            @Override
            public void onResponse( final List<Stop> response ) {
                final GoogleMap map = mMapFragment.getMap();

                for( final Stop stop : response ) {
                    final LatLng loc = new LatLng( stop.getLatitude(),
                                                   stop.getLongitude() );
                    map.addMarker( new MarkerOptions().position( loc )
                                                      .draggable( false )
                                                      .title( stop.getName() ) );
                }
            }
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
}
