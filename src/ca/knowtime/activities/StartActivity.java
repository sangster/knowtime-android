package ca.knowtime.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import ca.knowtime.R;
import ca.knowtime.fragments.ErrorDialogFragment;
import com.google.android.gms.common.ConnectionResult;

import static ca.knowtime.LocationUtils.APPTAG;
import static ca.knowtime.LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

public class StartActivity
        extends Activity
{
    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        final boolean isConnected = servicesConnected();
        if( !isConnected ) {
            Toast.makeText( this,
                            getText( R.string.no_play_services ),
                            Toast.LENGTH_LONG );
            finish();
        } else if( isDataSetNameSet() ) {
            startActivity( new Intent( this, NearbyStopsActivity.class ) );
        } else {
            startActivity( new Intent( this, WelcomeActivity.class ) );
        }
    }


    private boolean isDataSetNameSet() {
        return PreferenceManager.getDefaultSharedPreferences( this )
                                .contains( "data_set" );
    }


    @Override
    protected void onActivityResult( final int requestCode,
                                     final int resultCode,
                                     final Intent intent ) {
        switch( requestCode ) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch( resultCode ) {
                    case Activity.RESULT_OK:
                        Log.d( APPTAG, getString( R.string.resolved ) );
                        break;

                    default:
                        Log.d( APPTAG, getString( R.string.no_resolution ) );
                        break;
                }
            default:
                Log.d( APPTAG,
                       getString( R.string.unknown_activity_request_code,
                                  requestCode ) );

                break;
        }
    }


    private boolean servicesConnected() {
        final int resultCode = isGooglePlayServicesAvailable( this );

        if( ConnectionResult.SUCCESS == resultCode ) {
            Log.d( APPTAG, "Google Play services is available." );
            return true;
        } else {
            final Dialog dialog = getErrorDialog( resultCode, this, 0 );
            if( dialog != null ) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog( dialog );
                errorFragment.show( getFragmentManager(), APPTAG );
            }
            return false;
        }
    }
}
