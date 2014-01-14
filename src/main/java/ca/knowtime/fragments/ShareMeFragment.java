package ca.knowtime.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import ca.knowtime.R;
import ca.knowtime.RoutePickerActivity;
import ca.knowtime.WebApiService;
import ca.knowtime.comm.types.User;

import java.io.IOException;
import java.util.Date;

public class ShareMeFragment
        extends Fragment
{
    private ImageButton mOnMyWayButton;
    private ImageView mConnectToServerImage;
    private ImageView mSendingImage;
    private Boolean mIsSharing = false;
    private User mUser;
    private final Handler mHandler = new Handler();
    private float mPollRate;
    private ImageView mSendingLineImage;
    private Date mStartTime;
    private int mLoopCounter;


    private boolean isSendingLocations() {
        Date currentDate = new Date();
        long diffTime = currentDate.getTime() - mStartTime.getTime();
        return mIsSharing && diffTime < 108000000;
    }


    private final Runnable mUpdateUI = new Runnable()
    {
        public void run() {
            if( isSendingLocations() ) {
                if( mLoopCounter == 0 ) {
                    mSendingLineImage.setImageResource( R.drawable.sending1 );
                    mLoopCounter++;
                } else if( mLoopCounter == 1 ) {
                    mSendingLineImage.setImageResource( R.drawable.sending2 );
                    mLoopCounter++;
                } else {
                    mSendingLineImage.setImageResource( R.drawable.sending3 );
                    try {
                        shareMyLocation();
                    } catch( IOException e ) {
                        throw new RuntimeException( e );
                    }
                    mLoopCounter = 0;
                }
                mHandler.postDelayed( mUpdateUI, (long) (mPollRate * 1000 / 3) ); // 1 second
            }
        }
    };


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
    }


    @Override
    public View onCreateView( final LayoutInflater inflater, final ViewGroup container,
                              final Bundle savedInstanceState ) {
        final View view = inflater.inflate( R.layout.activity_share_me, container, false );
        mConnectToServerImage = (ImageView) view.findViewById( R.id.connecttoserverimage );
        mConnectToServerImage.setVisibility( View.INVISIBLE );

        mSendingImage = (ImageView) view.findViewById( R.id.sendingimage );
        mSendingImage.setVisibility( View.INVISIBLE );

        mSendingLineImage = (ImageView) view.findViewById( R.id.sendinglineimage );

        mOnMyWayButton = (ImageButton) view.findViewById( R.id.onmywaybutton );
        mOnMyWayButton.setOnTouchListener( new OnMyWayTouchListener() );

        return view;
    }


    private void startSharing() {
        mOnMyWayButton.setBackgroundResource( R.drawable.stop );
        mIsSharing = true;
        mConnectToServerImage.setVisibility( View.VISIBLE );
        mSendingImage.setVisibility( View.VISIBLE );
        mLoopCounter = 0;
    }


    private void stopSharing() {
        mOnMyWayButton.setBackgroundResource( R.drawable.onmywaybutton );
        mIsSharing = false;
        mConnectToServerImage.setVisibility( View.INVISIBLE );
        mSendingImage.setVisibility( View.INVISIBLE );
        mSendingLineImage.setImageResource( R.drawable.sendingline );
    }


    private void createNewUser( final String route ) {
        new Thread( new Runnable()
        {
            @Override
            public void run() {
                try {
                    mUser = WebApiService.createUser( Integer.parseInt( route ) );
                    mPollRate = WebApiService.pollRate();

                    mStartTime = new Date();
                    mHandler.post( mUpdateUI );
                } catch( Exception e ) {
                    stopSharing();
                }
            }
        } ).start();
    }


    private void shareMyLocation()
            throws IOException {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        Location loc = locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );

        mUser.postLocation(
                new ca.knowtime.comm.types.Location( (float) loc.getLatitude(), (float) loc.getLongitude() ) );
    }


    private class OnMyWayTouchListener
            implements OnTouchListener
    {
        @Override
        public boolean onTouch( View v, MotionEvent event ) {
            switch( event.getAction() ) {
                case MotionEvent.ACTION_DOWN:
                    mOnMyWayButton.setBackgroundResource( mIsSharing ? R.drawable.onmywaybutton : R.drawable.stop );
                    return false;
                case MotionEvent.ACTION_UP:
                    if( mIsSharing ) {
                        stopSharing();
                    } else {
                        startActivityForResult( new Intent( getActivity(), RoutePickerActivity.class ), 1 );
                        startSharing();
                    }
                    break;
            }
            return false;
        }
    }
}
