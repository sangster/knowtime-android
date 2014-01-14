package ca.knowtime;

import android.text.format.DateFormat;
import android.util.Log;
import ca.knowtime.comm.KnowTime;
import ca.knowtime.comm.KnowTimeAccess;
import ca.knowtime.comm.types.RouteName;
import ca.knowtime.comm.types.RouteStopTimes;
import ca.knowtime.comm.types.User;
import ca.knowtime.map.StopMarker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Created by aeisses on 2013-11-19. */
public class WebApiService
{
    private static final String SANGSTERBASEURL = "http://api.knowtime.ca/alpha_1/";
    private static final String ROUTES = "routes/";
    private static final String SHORTS = "short:";
    private static final String PATHS = "paths/";
    private static final String HEADSIGNS = "headsigns/";
    private static final String ESTIMATE = "estimates/";


    private static final KnowTimeAccess KNOW_TIME = KnowTime.connect( URI.create( SANGSTERBASEURL ), new RestCache() );


    public static void fetchAllRoutes() {
        new Thread( new Runnable()
        {
            @Override
            public void run() {
                try {
                    for( final RouteName routeName : KNOW_TIME.routeNames() ) {
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
            return getJSONArrayFromUrl( SANGSTERBASEURL + ESTIMATE + SHORTS + routeId );
        } catch( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }


    public static List<RouteStopTimes> getRouteStopTimes( final int stopNumber )
            throws IOException, JSONException {
        return KNOW_TIME.routesStopTimes( stopNumber, new Date() );
    }


    public static JSONArray getPathForRouteId( final String routeId ) {
        try {
            return getJSONArrayFromUrl(
                    SANGSTERBASEURL + PATHS + DateFormat.format( "yyyy-MM-dd", new Date() ) + "/" + routeId );
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }


    public static JSONArray loadPathForRoute( final String shortName ) {
        try {
            return getJSONArrayFromUrl(
                    SANGSTERBASEURL + ROUTES + SHORTS + shortName + "/" + HEADSIGNS + DateFormat.format( "yyyy-MM-dd",
                                                                                                         new Date() ) + "/" + DateFormat.format(
                            "HH:MM", new Date() ) );
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }


    public static String sendUrlRequest( String url ) {
        HttpClient client = new DefaultHttpClient();
        HttpGet post = new HttpGet( url );
        HttpResponse response;
        try {
            response = client.execute( post );
            HttpEntity entity = response.getEntity();
            if( entity != null ) {
                InputStream instream = entity.getContent();
                String result = convertStreamToString( instream );
                instream.close();
                return result;
            }
        } catch( ClientProtocolException e ) {
            e.printStackTrace();
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return "";
    }


    private static String convertStreamToString( InputStream is ) {
        BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while( (line = reader.readLine()) != null ) {
                sb.append( line + "\n" );
            }
        } catch( IOException e ) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch( IOException e ) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    private static JSONObject getJSONObjectFromUrl( String url ) {
        JSONObject jObj = null;
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject( WebApiService.getResponseFromUrl( url ) );
        } catch( JSONException e ) {
            Log.e( "JSON Parser", "Error parsing data " + e.toString() );
        }

        // return JSON String
        return jObj;
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


    public static List<Route> getRoutesList() {
        return DatabaseHandler.getInstance().getAllRoutes();
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
