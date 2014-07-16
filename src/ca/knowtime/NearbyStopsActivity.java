package ca.knowtime;

import android.app.Dialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import ca.knowtime.fragments.ErrorDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.MapView;

import static ca.knowtime.LocationUtils.APPTAG;
import static ca.knowtime.LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static ca.knowtime.LocationUtils.FAST_INTERVAL_CEILING_IN_MS;
import static ca.knowtime.LocationUtils.UPDATE_INTERVAL_IN_MS;
import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class NearbyStopsActivity
        extends FragmentActivity
{
    private MapView mMapView;
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;


    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.nearby_stops_activity );
        mMapView = (MapView) findViewById( R.id.nearby_stops_map );


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval( UPDATE_INTERVAL_IN_MS );
        mLocationRequest.setPriority( PRIORITY_HIGH_ACCURACY );
        mLocationRequest.setFastestInterval( FAST_INTERVAL_CEILING_IN_MS );

        mLocationListener = new MyLocationListener();

        mLocationClient = new LocationClient( this,
                                              new MyConnectionCallbacks(),
                                              new MyOnConnectionFailedListener() );
    }


    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }


    @Override
    public void onStop() {
        if( mLocationClient.isConnected() ) {
            stopPeriodicUpdates();
        }
        mLocationClient.disconnect();
        super.onStop();
    }


    public void startUpdates( View v ) {
        if( servicesConnected() ) {
            startPeriodicUpdates();
        }
    }


    public void stopUpdates( View v ) {
        if( servicesConnected() ) {
            stopPeriodicUpdates();
        }
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


    private class MyConnectionCallbacks
            implements GooglePlayServicesClient.ConnectionCallbacks
    {
        @Override
        public void onConnected( Bundle bundle ) {
            startPeriodicUpdates();
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
