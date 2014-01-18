package ca.knowtime;

import android.net.Uri;
import ca.knowtime.comm.AsyncKnowTimeAccess;
import ca.knowtime.comm.KnowTime;
import ca.knowtime.comm.async.AsyncGet;
import ca.knowtime.comm.async.AsyncCallback;
import ca.knowtime.comm.types.*;
import ca.knowtime.comm.types.Stop;
import ca.knowtime.map.StopMarker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Created by aeisses on 2013-11-19. */
public class WebApiService
{
    private static final String BASE_URL = "http://api.knowtime.ca/alpha_1/";
    private static final AsyncKnowTimeAccess KNOW_TIME = KnowTime.async( Uri.parse( BASE_URL ), new RestCache() );


    public static void fetchAllRoutes() {
        KNOW_TIME.routeNames().execute( new AsyncCallback<List<RouteName>>()
        {
            @Override
            public void requestComplete( final List<RouteName> routeNames ) {
                for( final RouteName routeName : routeNames ) {
                    final Route route = new Route( routeName.getLongName(), routeName.getShortName() );

                    if( DatabaseHandler.getInstance().getRoute( route.getShortName() ) == null ) {
                        DatabaseHandler.getInstance().addRoute( route );
                    }
                }
            }
        } );
    }


    public static void fetchAllStops( final AsyncCallback<Map<String, MarkerOptions>> result ) {
        KNOW_TIME.stops().execute( new AsyncCallback<List<Stop>>()
        {
            @Override
            public void requestComplete( final List<Stop> stops ) {
                result.requestComplete( StopMarker.stopMarkersMap( stops ) );
            }
        } );
    }


    public static AsyncGet<List<Estimate>> getEstimatesForRoute( final int routeId ) {
        return KNOW_TIME.estimatesForShortName( Integer.toString( routeId ) );
    }


    public static AsyncGet<List<RouteStopTimes>> getRouteStopTimes( final int stopNumber ) {
        final int[] date = todaysDateParts();
        return KNOW_TIME.routesStopTimes( stopNumber, date[0], date[1], date[2] );
    }


    private static int[] todaysDateParts() {
        return dateParts( new Date() );
    }


    private static int[] dateParts( final Date date ) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime( date );
        return new int[]{ cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH ) };
    }


    public static AsyncGet<List<Path>> getPathsForRouteId( final UUID routeId ) {
        final int[] date = todaysDateParts();
        return KNOW_TIME.routePaths( routeId, date[0], date[1], date[2] );
    }


    public static List<String> getRoutes() {
        List<String> list = new ArrayList<String>();

        List<Route> routes = DatabaseHandler.getInstance().getAllRoutes();

        for( final Route route : routes ) {
            if( isInt( route.getShortName() ) ) {
                list.add( route.getShortName() );
            }
        }
        return list;
    }


    private static Boolean isInt( String value ) {
        try {
            Integer.parseInt( value );
            return true;
        } catch( Exception e ) {
            return false;
        }
    }


    public static AsyncGet<User> createUser( final int routeId ) {
        return KNOW_TIME.createUser( routeId );
    }


    public static AsyncGet<Float> pollRate() {
        return KNOW_TIME.pollRate();
    }
}
