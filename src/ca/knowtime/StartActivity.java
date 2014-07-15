package ca.knowtime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartActivity
        extends Activity
{
    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        if( isDataSetNameSet() ) {
            Log.d( "JON", "data set - exiting" );
            finish();
        } else {
            Log.d( "JON", "no data set" );
            startActivity( new Intent( this, WelcomeActivity.class ) );
        }
    }


    private boolean isDataSetNameSet() {
        return PreferenceManager.getDefaultSharedPreferences( this ).contains( "data_set" );
    }
}
