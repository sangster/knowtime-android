package ca.knowtime.map;


import ca.knowtime.comm.types.Location;

public class LocationBoundsBuilder
{
    private float mLatMin = Float.NaN;
    private float mLatMax = Float.NaN;

    private float mLngMin = Float.NaN;
    private float mLngMax = Float.NaN;


    public void add( final Location location ) {
        final float lat = location.getLat();
        final float lng = location.getLng();

        if( Float.isNaN( mLatMin ) || lat < mLatMin ) {
            mLatMin = lat;
        }
        if( Float.isNaN( mLatMax ) || lat > mLatMax ) {
            mLatMax = lat;
        }
        if( Float.isNaN( mLngMin ) || lng < mLngMin ) {
            mLngMin = lng;
        }
        if( Float.isNaN( mLngMax ) || lng > mLngMax ) {
            mLngMax = lng;
        }
    }


    public void expand( final float amount ) {
        mLatMin -= amount;
        mLatMax += amount;

        mLngMin -= amount;
        mLngMax += amount;
    }


    public Location minLocation() {
        return new Location( mLatMin, mLngMin );
    }


    public Location maxLocation() {
        return new Location( mLatMax, mLngMax );
    }


    private float constrainLat( final float lat ) {
        if( Float.isNaN( lat ) ) {
            return Float.NaN;
        }
        return Math.max( -180f, Math.min( lat, 180f ) );
    }


    private float constrainLng( final float lng ) {
        if( Float.isNaN( lng ) ) {
            return Float.NaN;
        }
        return Math.max( -360f, Math.min( lng, 360f ) );
    }
}
