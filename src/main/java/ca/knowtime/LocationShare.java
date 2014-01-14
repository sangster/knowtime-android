package ca.knowtime;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

public class LocationShare
        extends IntentService
{
    public static final String serviceName = "ca.knowtime.locationservice";


    public LocationShare() {
        super( serviceName );
    }


    public void startLocationShare()
            throws IOException {
        WebApiService.createNewUser( 1 );
    }


    @Override
    protected void onHandleIntent( Intent arg0 ) {
        // TODO Auto-generated method stub
        Log.d( "ca.knowtime", "here we are" );
    }
}
