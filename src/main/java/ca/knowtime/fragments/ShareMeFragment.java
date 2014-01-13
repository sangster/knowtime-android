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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class ShareMeFragment
        extends Fragment
{
    private ImageButton mOnMyWayButton;
    private ImageView mConnectToServerImage;
    private ImageView mSendingImage;
    private Boolean mIsSharing = false;
    private String mLocationUrl;
    private final Handler mHandler = new Handler();
    private int mPollRate;
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
                    shareMyLocation();
                    mLoopCounter = 0;
                }
                mHandler.postDelayed( mUpdateUI, (mPollRate * 1000) / 3 ); // 1 second
            }
        }
    };


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        mLocationUrl = "";
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


    public void touchBackButton( View view ) {
        getActivity().finish();
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
        Thread thread = new Thread( new Runnable()
        {
            @Override
            public void run() {
                mLocationUrl = WebApiService.createNewUser( Integer.parseInt( route ) );
                if( !mLocationUrl.equals( "" ) ) {
                    final JSONObject jsonPollRate = WebApiService.getPollRate();
                    try {
                        mPollRate = jsonPollRate.getInt( "rate" );
                    } catch( JSONException e ) {
                        e.printStackTrace();
                    }
                    mStartTime = new Date();
                    mHandler.post( mUpdateUI );
                } else {
                    stopSharing();
                }
            }
        } );
        thread.start();
    }


    private void shareMyLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation( locationProvider );
        WebApiService.sendLocationToServer( mLocationUrl, lastKnownLocation );
    }


    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        //        if( requestCode == 1 && resultCode == RESULT_OK && data != null ) {
        //            String routeNumber = data.getStringExtra( "routeNumber" );
        //            if( Integer.parseInt( routeNumber ) == -1 ) {
        //                stopSharing();
        //            } else {
        //                createNewUser( routeNumber );
        //            }
        //        }
    }


    private class OnMyWayTouchListener
            implements OnTouchListener
    {
        @Override
        public boolean onTouch( View v, MotionEvent event ) {
            if( event.getAction() == MotionEvent.ACTION_DOWN ) {
                if( mIsSharing ) {
                    mOnMyWayButton.setBackgroundResource( R.drawable.onmywaybutton );
                } else {
                    mOnMyWayButton.setBackgroundResource( R.drawable.stop );
                }
            }
            if( event.getAction() == MotionEvent.ACTION_UP ) {
                if( mIsSharing ) {
                    stopSharing();
                } else {
                    Intent intent = new Intent( getActivity(), RoutePickerActivity.class );
                    startActivityForResult( intent, 1 );
                    startSharing();
                }
            }
            return false;
        }
    }
}
