package ca.knowtime;

import android.net.Uri;
import android.util.Log;
import ca.knowtime.comm.KnowTime;
import ca.knowtime.comm.KnowTimeAccess;
import ca.knowtime.comm.types.Path;
import ca.knowtime.comm.types.RouteName;
import ca.knowtime.comm.types.RouteStopTimes;
import ca.knowtime.comm.types.User;
import ca.knowtime.map.StopMarker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Created by aeisses on 2013-11-19. */
public class WebApiService
{
    private static final String BASE_URL = "http://api.knowtime.ca/alpha_1/";
    private static final String SHORTS = "short:";
    private static final String PATHS = "paths/";
    private static final String ESTIMATE = "estimates/";


    private static final KnowTimeAccess KNOW_TIME = KnowTime.connect( Uri.parse( BASE_URL ), new RestCache() );


    public static void fetchAllRoutes() {
        new Thread( new Runnable()
        {
            @Override
            public void run() {
                try {
                    final List<RouteName> routeNames = KNOW_TIME.routeNames();
                    for( final RouteName routeName : routeNames ) {
                        final Route route = new Route( routeName.getLongName(), routeName.getShortName() );
                        if( DatabaseHandler.getInstance().getRoute( route.getShortName() ) == null ) {
                            DatabaseHandler.getInstance().addRoute( route );
                        }
                    }
                } catch( final Exception e ) {
                    throw new RuntimeException( e );
                }
            }
        } ).start();
    }


    public static Map<String, MarkerOptions> fetchAllStops() {
        try {
            return StopMarker.stopMarkersMap( KNOW_TIME.stops() );
        } catch( Exception e ) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }


    public static JSONArray getEstimatesForRoute( final int routeId ) {
        try {
            return getJSONArrayFromUrl( BASE_URL + ESTIMATE + SHORTS + routeId );
        } catch( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }


    public static List<RouteStopTimes> getRouteStopTimes( final int stopNumber )
            throws IOException, JSONException {
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


    public static List<Path> getPathsForRouteId( final UUID routeId )
            throws IOException, JSONException {
        final int[] date = todaysDateParts();
        return KNOW_TIME.routePaths( routeId, date[0], date[1], date[2] );
    }


    private static JSONArray getJSONArrayFromUrl( String url ) {
        JSONArray jObj = null;
        Log.d( "ca.knowtime", "URL: " + url );
        // try parse the string to a JSON object
        try {
            jObj = new JSONArray( WebApiService.getResponseFromUrl( url ) );
        } catch( JSONException e ) {
            Log.e( "JSON Parser", "Error parsing data " + e.toString() );
        }

        // return JSON String
        return jObj;
    }


    private static String getResponseFromUrl( String url ) {
        InputStream is = null;
        String response = "";

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet( url );

            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch( UnsupportedEncodingException e ) {
            e.printStackTrace();
        } catch( ClientProtocolException e ) {
            e.printStackTrace();
        } catch( IOException e ) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader( new InputStreamReader( is, "iso-8859-1" ), 8 );
            StringBuilder sb = new StringBuilder();
            String line = null;
            while( (line = reader.readLine()) != null ) {
                sb.append( line + "\n" );
            }
            is.close();
            response = sb.toString();
        } catch( Exception e ) {
            Log.e( "Buffer Error", "Error converting result " + e.toString() );
        }
        return response;
    }


    private static Boolean isStringInt( String value ) {
        try {
            Integer.parseInt( value );
        } catch( Exception e ) {
            return false;
        }
        return true;
    }


    public static List<String> getRoutes() {
        List<String> list = new ArrayList<String>();

        List<Route> routes = DatabaseHandler.getInstance().getAllRoutes();

        for( final Route route : routes ) {
            if( WebApiService.isStringInt( route.getShortName() ) ) {
                list.add( route.getShortName() );
            }
        }
        return list;
    }


    public static User createUser( final int routeId )
            throws IOException {
        return KNOW_TIME.createUser( routeId );
    }


    public static float pollRate()
            throws IOException, JSONException {
        return KNOW_TIME.pollRate();
    }
}
