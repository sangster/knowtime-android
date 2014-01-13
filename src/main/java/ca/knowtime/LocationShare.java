package ca.knowtime;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class LocationShare
        extends IntentService
{
    public static final String serviceName = "ca.knowtime.locationservice";


    public LocationShare() {
        super( serviceName );
    }


    public void startLocationShare() {
        WebApiService.createNewUser( 1 );
    }


    @Override
    protected void onHandleIntent( Intent arg0 ) {
        // TODO Auto-generated method stub
        Log.d( "ca.knowtime", "here we are" );
    }
}
