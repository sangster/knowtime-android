package ca.knowtime.listeners;

import android.location.Location;
import android.util.Log;
import com.google.android.gms.location.LocationListener;

import static ca.knowtime.LocationUtils.APPTAG;

public class MyLocationListener
        implements LocationListener
{
    @Override
    public void onLocationChanged( final Location location ) {
        Log.i( APPTAG, "Location Changed: " + location.toString() );
    }
}
