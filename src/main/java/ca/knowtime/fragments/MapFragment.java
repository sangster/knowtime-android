package ca.knowtime.fragments;


import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import ca.knowtime.R;
import ca.knowtime.comm.KnowTime;
import ca.knowtime.comm.Response;
import ca.knowtime.comm.types.Agency;
import ca.knowtime.comm.types.DataSetSummary;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.List;

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
    private View mView;


    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        mContext = getActivity();
        mShowStops = true;
    }


    @Override
    public View onCreateView( final LayoutInflater inflater, final ViewGroup container,
                              final Bundle savedInstanceState ) {
        if( mView == null ) {
            mView = inflater.inflate( R.layout.activity_main, container, false );
            mMapMarkerProgressBar = (ProgressBar) mView.findViewById( R.id.mapMarkerProgressBar );
            mMapMarkerProgressBarText = (TextView) mView.findViewById( R.id.mapMarkerProgressBarText );
        }
        return mView;
    }


    @Override
    public void onActivityCreated( final Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );
        if( mMap != null ) {
            return;
        }

        mMap = ((com.google.android.gms.maps.MapFragment) getFragmentManager().findFragmentById( R.id.map1 ))
                .getMap();

        if( mMap != null ) {
            mMap.setMyLocationEnabled( true );

            //            final LocationManager locationManager = (LocationManager) getActivity().getSystemService(
            //                    Context.LOCATION_SERVICE );
            //            final String locationProvider = LocationManager.NETWORK_PROVIDER;
            //            final Location lastKnownLocation = locationManager.getLastKnownLocation( locationProvider );

            mMap.moveCamera( CameraUpdateFactory.newLatLngZoom( DEFAULT_HALIFAX_LAT_LNG,
                                                                //                    new LatLng( lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude() ),
                                                                DEFAULT_HALIFAX_LAT_LNG_ZOOM ) );
        }
    }


    public void toggleStopsVisibility() {
        mShowStops = !mShowStops;
    }


    @Override
    public void onResume() {
        super.onResume();

        final Response<List<DataSetSummary>> response = new Response<List<DataSetSummary>>()
        {
            @Override
            public void onResponse( final List<DataSetSummary> response ) {
                for( final DataSetSummary summary : response ) {
                    summary.agencies( new Response<List<Agency>>()
                    {
                        @Override
                        public void onResponse( final List<Agency> response ) {
                            for( final Agency agency : response ) {
                                Log.d( "Jon", summary.getName() + ": " + agency.toString() );
                            }
                        }


                        @Override
                        public void onErrorResponse( final VolleyError error ) {
                            Log.e( "Jon", error.toString() );
                        }
                    } );
                }
            }


            @Override
            public void onErrorResponse( final VolleyError error ) {
                Log.e( "Jon", error.toString() );
            }
        };

        KnowTime.dataSets( mContext, Uri.parse( "http://aerith:3000/v2" ), response );
    }
}
