package ca.knowtime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import ca.knowtime.comm.types.Estimate;
import ca.knowtime.comm.types.Location;
import ca.knowtime.comm.types.Path;
import ca.knowtime.map.LocationBoundsBuilder;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RouteMapActivity
        extends Activity
{
    private String mRouteNumber;
    private UUID mRouteId;
    private GoogleMap mMap;
    private ProgressBar mProgressBar;
    private final Handler mHandler = new Handler();
    private List<Marker> mMarkers = new ArrayList<Marker>();
    private Context mContext;
    private boolean mIsUpdatingLocations;
    private ImageButton mFavouriteButton;
    private Route mRoute;

    private final Runnable mUpdateUI = new Runnable()
    {
        public void run() {
            if( !mIsUpdatingLocations ) {
                updateBusesLocation();
            }
            mHandler.postDelayed( mUpdateUI, 3000 ); // 1 second
        }
    };


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_route );
        mContext = this;
        mIsUpdatingLocations = false;
        Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            mRouteId = UUID.fromString( extras.getString( "ROUTE_ID" ) );
            mRouteNumber = extras.getString( "ROUTE_NUMBER" );
            mRoute = DatabaseHandler.getInstance().getRoute( mRouteNumber );
            mRoute.setId( mRouteId.toString() );
            DatabaseHandler.getInstance().updateRoute( mRoute );
        }
        if( mMap == null ) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById( R.id.map1 )).getMap();

            if( mMap != null ) {
                //				mMap.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );
                String locationProvider = LocationManager.NETWORK_PROVIDER;
                android.location.Location lastKnownLocation = locationManager.getLastKnownLocation( locationProvider );
                mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(
                        new LatLng( lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude() ),
                        MainActivity.DEFAULT_HALIFAX_LAT_LNG_ZOOM ) );
            }
        }
        // Load the route
        mProgressBar = (ProgressBar) findViewById( R.id.routesProgressBar );
        mFavouriteButton = (ImageButton) this.findViewById( R.id.favouritebutton );
        mFavouriteButton.setSelected( mRoute.getFavourite() );
        getRoute();
    }


    private void getRoute() {
        new Thread( new Runnable()
        {
            @Override
            public void run() {
                final List<Path> paths;
                try {
                    paths = WebApiService.getPathsForRouteId( mRouteId );
                } catch( Exception e ) {
                    throw new RuntimeException( e );
                }

                if( paths.size() <= 1 ) {
                    return; // TODO One path is not good enough?
                }

                final LocationBoundsBuilder bounds = new LocationBoundsBuilder();

                for( final Path path : paths ) {
                    PolylineOptions routePointsValues = new PolylineOptions().width( 5 ).color( Color.BLACK );

                    for( final ca.knowtime.comm.types.Location loc : path.getPathPoints() ) {
                        bounds.add( loc );
                        routePointsValues.add( new LatLng( loc.getLat(), loc.getLng() ) );
                    }
                    runOnUiThread( new AddPolylineRunnable( routePointsValues ) );
                }

                bounds.expand( 0.001f );

                runOnUiThread( new MoveCameraRunnable( bounds.minLocation(), bounds.maxLocation() ) );
            }
        } ).start();
    }


    public void touchBackButton( View view ) {
        mRoute.setFavourite( mFavouriteButton.isSelected() );
        DatabaseHandler.getInstance().updateRoute( mRoute );
        this.finish();
    }


    public void touchFavouriteButton( View view ) {
        view.setSelected( !view.isSelected() );
    }


    public void updateBusesLocation() {
        if( !mIsUpdatingLocations ) {
            new Thread( new UpdateLocationsRunnable() ).start();
        }
    }


    private void loadAlertDialog() {
        mIsUpdatingLocations = false;
        mHandler.removeCallbacks( mUpdateUI );
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( mContext );
        alertDialog.setTitle( "Alert" );
        alertDialog.setMessage(
                "No buses can currently be found. This can be because no one is sending a signal or a server issue." );
        alertDialog.setNegativeButton( "ok", new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int id ) {
                dialog.cancel();
            }
        } );
        alertDialog.show();
    }


    private void clearMarkers() {
        new Thread( new RemoveMarkersFromMapRunnable( new ArrayList<Marker>( mMarkers ) ) ).start();
        mMarkers.clear();
    }


    private static class RemoveMarkersFromMapRunnable
            implements Runnable
    {
        private final ArrayList<Marker> mMarkers;


        public RemoveMarkersFromMapRunnable( final ArrayList<Marker> markers ) {
            mMarkers = markers;
        }


        @Override
        public void run() {
            for( final Marker marker : mMarkers ) {
                marker.remove();
            }
        }
    }


    private class AddPolylineRunnable
            implements Runnable
    {
        private final PolylineOptions mRouteLine;


        public AddPolylineRunnable( final PolylineOptions routeLine ) {
            mRouteLine = routeLine;
        }


        @Override
        public void run() {
            mMap.addPolyline( mRouteLine );
        }
    }


    private class MoveCameraRunnable
            implements Runnable
    {
        private final Location mMin;
        private final Location mMax;


        public MoveCameraRunnable( final Location min, final Location max ) {
            mMin = min;
            mMax = max;
        }


        @Override
        public void run() {
            final LatLngBounds bounds = new LatLngBounds( new LatLng( mMin.getLat(), mMin.getLng() ),
                                                          new LatLng( mMax.getLat(), mMax.getLng() ) );

            mMap.moveCamera( CameraUpdateFactory.newLatLngBounds( bounds, 0 ) );
            mProgressBar.setVisibility( View.INVISIBLE );
            mHandler.post( mUpdateUI );
        }
    }


    private class AddMarkerRunnable
            implements Runnable
    {
        private final Location mLoc;


        public AddMarkerRunnable( final Location loc ) {
            mLoc = loc;
        }


        @Override
        public void run() {
            final LatLng pos = new LatLng( mLoc.getLat(), mLoc.getLng() );
            final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource( R.drawable.busicon );

            MarkerOptions marker = new MarkerOptions().position( pos );
            marker = marker.anchor( 0.5f, 0.5f ).icon( icon );

            mMarkers.add( mMap.addMarker( marker ) );
        }
    }

    private class AlertDialogRunnable
            implements Runnable
    {
        @Override
        public void run() {
            loadAlertDialog();
        }
    }


    private class UpdateLocationsRunnable
            implements Runnable
    {
        @Override
        public void run() {
            mIsUpdatingLocations = true;
            clearMarkers();

            final List<Estimate> routes = WebApiService.getEstimatesForRoute( Integer.parseInt( mRouteNumber ) );
            if( !routes.isEmpty() ) {
                final Estimate first = routes.get( 0 );
                for( final Estimate route : routes ) {
                    if( route.getLocation().isValid() ) {
                        runOnUiThread( new AddMarkerRunnable( route.getLocation() ) );
                    } else if( route == first ) {
                        runOnUiThread( new AlertDialogRunnable() );
                    }
                }
            } else {
                runOnUiThread( new AlertDialogRunnable() );
            }
            mIsUpdatingLocations = false;
        }
    }
}
