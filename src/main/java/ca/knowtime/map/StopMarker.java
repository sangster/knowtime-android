package ca.knowtime.map;

import ca.knowtime.R;
import ca.knowtime.comm.types.Stop;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StopMarker
{
    public static Map<String, MarkerOptions> stopMarkersMap( final List<Stop> stops ) {
        final Map<String, MarkerOptions> map = new HashMap<String, MarkerOptions>();
        for( final Stop stop : stops ) {
            map.put( Integer.toString( stop.getStopNumber() ), stopMarker( stop ) );
        }
        return map;
    }


    public static MarkerOptions stopMarker( final Stop stop ) {
        final MarkerOptions options = new MarkerOptions();

        options.draggable( false );
        options.anchor( .6f, .6f );

        options.icon( BitmapDescriptorFactory.fromResource( R.drawable.bus_stop ) );
        options.position( new LatLng( stop.getLocation().getLat(), stop.getLocation().getLng() ) );

        options.snippet( Integer.toString( stop.getStopNumber() ) );
        options.title( stop.getName() );

        return options;
    }
}
