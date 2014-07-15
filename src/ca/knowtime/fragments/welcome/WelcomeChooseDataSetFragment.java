package ca.knowtime.fragments.welcome;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import ca.knowtime.Development;
import ca.knowtime.R;
import ca.knowtime.adapters.DataSetArrayAdapter;
import ca.knowtime.comm.KnowTime;
import ca.knowtime.comm.KnowTimeAccess;
import ca.knowtime.comm.Response;
import ca.knowtime.comm.models.DataSetSummary;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;


public class WelcomeChooseDataSetFragment
        extends Fragment
{
    public static final float DEFAULT_MAP_ZOOM = 14f;
    private KnowTimeAccess mAccess;
    private ListView mDataSetsView;
    private MapView mMapView;
    private LocationManager mLocationManager;


    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        mAccess = KnowTime.connect( getActivity(), Development.BASE_URL );
        mLocationManager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
    }


    @Override
    public View onCreateView( LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState ) {
        final View view = inflater.inflate( R.layout.welcome_choose_data_set_fragment,
                                            container,
                                            false );

        mDataSetsView = (ListView) view.findViewById( R.id.data_set_list );
        mDataSetsView.setOnItemClickListener( new DataSetClickListener() );
        mMapView = findMapView( view, R.id.data_set_map );
        mMapView.onCreate( savedInstanceState );


        return view;
    }


    private MapView findMapView( final View view, final int mapId ) {
        final MapView mapView = (MapView) view.findViewById( mapId );
        mapView.setClickable( false );
        return mapView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        setUpMapView();

        final Location location = mLocationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
        if( location != null ) {
            animateMapCamera( location );
        }

        mAccess.dataSets( this, new Response<List<DataSetSummary>>()
        {
            @Override
            public void onResponse( final List<DataSetSummary> response ) {
                mDataSetsView.setAdapter( new DataSetArrayAdapter( getActivity(), response ) );
            }
        } );
    }


    private void animateMapCamera( final Location location ) {
        final LatLng loc = new LatLng( location.getLatitude(), location.getLongitude() );
        final CameraUpdate update = CameraUpdateFactory.newLatLngZoom( loc, DEFAULT_MAP_ZOOM );
        mMapView.getMap().animateCamera( update );
    }


    private void setUpMapView() {
        MapsInitializer.initialize( getActivity() );
        final GoogleMap map = mMapView.getMap();
        map.setMapType( GoogleMap.MAP_TYPE_NORMAL );
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mAccess.cancel( this );
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }


    @Override
    public void onSaveInstanceState( final Bundle outState ) {
        super.onSaveInstanceState( outState );
        mMapView.onSaveInstanceState( outState );
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    private class DataSetClickListener
            implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick( final AdapterView<?> parent,
                                 final View view,
                                 final int position,
                                 final long id ) {
            view.setSelected( true );
            final DataSetSummary dataSet = (DataSetSummary) mDataSetsView.getItemAtPosition(
                    position );

            final CameraUpdate update = CameraUpdateFactory.newLatLngBounds( bounds( dataSet ), 0 );
            mMapView.getMap().animateCamera( update );
        }


        private LatLngBounds bounds( final DataSetSummary dataSet ) {
            final DataSetSummary.Location nw = dataSet.getNorthWestCorner();
            final DataSetSummary.Location se = dataSet.getSouthEastCorner();
            return LatLngBounds.builder()
                               .include( new LatLng( nw.getLatitude(), nw.getLongitude() ) )
                               .include( new LatLng( se.getLatitude(), se.getLongitude() ) )
                               .build();
        }
    }
}
