package ca.knowtime;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StopsMarkerLoader
        extends AsyncTaskLoader<Map<String, MarkerOptions>>
{
    private static Map<String, MarkerOptions> stopMarkers = Collections.emptyMap();
    private double mBottomLat;
    private double mBottomLng;
    private double mTopLat;
    private double mTopLng;
    private float mZoom;
    private boolean mShowStops;


    public StopsMarkerLoader( final Context context, final double bottomLat, final double bottomLng,
                              final double topLat, final double topLng, final float zoom, final boolean showStops ) {
        super( context );
        mBottomLat = bottomLat;
        mBottomLng = bottomLng;
        mTopLat = topLat;
        mTopLng = topLng;
        mZoom = zoom;
        mShowStops = showStops;
    }


    @Override
    public Map<String, MarkerOptions> loadInBackground() {
        if( stopMarkers.isEmpty() ) {
            stopMarkers = WebApiService.fetchAllStops();
        }
        return limitMarkerByBounds( stopMarkers );
    }


    private Map<String, MarkerOptions> limitMarkerByBounds( final Map<String, MarkerOptions> markers ) {
        final Map<String, MarkerOptions> resultMarker = new HashMap<String, MarkerOptions>();

        Log.d( getClass().getCanonicalName(),
               "current zoom :" + mZoom + "Default zoom:" + MainActivity.DEFAULT_HALIFAX_LAT_LNG_ZOOM );

        if( mShowStops && mZoom >= MainActivity.DEFAULT_HALIFAX_LAT_LNG_ZOOM ) {
            for( String currentStop : markers.keySet() ) {
                final MarkerOptions marker = markers.get( currentStop );
                if( isMarkerWithinRange( marker ) ) {
                    resultMarker.put( currentStop, marker );
                }
            }
        }

        Log.d( getClass().getCanonicalName(), "limited result size :" + resultMarker.size() );
        return resultMarker;
    }


    private boolean isMarkerWithinRange( final MarkerOptions marker ) {
        final LatLng latlng = marker.getPosition();
        final double lat = latlng.latitude;
        final double lng = latlng.longitude;

        return mTopLat > lat && lat > mBottomLat && mTopLng > lng && lng > mBottomLng;
    }
}
