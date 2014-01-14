package ca.knowtime.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import ca.knowtime.DatabaseHandler;
import ca.knowtime.R;
import ca.knowtime.RouteMapActivity;
import ca.knowtime.Stop;
import ca.knowtime.WebApiService;
import ca.knowtime.comm.types.RouteStopTimes;
import ca.knowtime.comm.types.StopTimePair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StopsFragment
        extends Fragment
{
    private String mStopNumber;
    private String mStopName;
    private ProgressBar mProgressBar;
    private TableLayout mStopTable;
    private Stop mStop;
    private ImageButton mFavouriteButton;


    public StopsFragment() {
        this( null, null );
    }


    public StopsFragment( final String stopNumber, final String stopName ) {
        mStopNumber = stopNumber;
        mStopName = stopName;
    }


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        if( mStopNumber != null && mStopName != null ) {
            mStop = DatabaseHandler.getInstance( getActivity() ).getStop( mStopNumber );
        }
        getStops();
    }


    @Override
    public View onCreateView( final LayoutInflater inflater, final ViewGroup container,
                              final Bundle savedInstanceState ) {
        final View view = inflater.inflate( R.layout.activity_stops, container, false );
        mProgressBar = (ProgressBar) view.findViewById( R.id.stopsProgressBar );
        mStopTable = (TableLayout) view.findViewById( R.id.stopTable );

        final TextView stopsHeaderTextView = (TextView) mStopTable.findViewById( R.id.stopsHeaderText );
        stopsHeaderTextView.setText( mStopName );

        // Determine if this is a favorite stop
        mFavouriteButton = (ImageButton) view.findViewById( R.id.favouriteButton );
        mFavouriteButton.setSelected( mStop.getFavourite() );

        return view;
    }


    private void getStops() {
        new Thread( new Runnable()
        {
            @Override
            public void run() {
                try {
                    List<RouteStopTimes> stopTimes = WebApiService.getRouteStopTimes( Integer.parseInt( mStopNumber ) );
                    getActivity().runOnUiThread( new GetStopsRunnable( stopTimes ) );
                } catch( Exception e ) {
                    throw new RuntimeException( e );
                }
            }
        } ).start();
    }


    public void touchBackButton( View view ) {
        mStop.setFavourite( mFavouriteButton.isSelected() );
        DatabaseHandler.getInstance( getActivity() ).updateStop( mStop );
        getActivity().finish();
    }


    public void touchFavouriteButton( View view ) {
        view.setSelected( !view.isSelected() );
    }


    private class GetStopsRunnable
            implements Runnable
    {
        private final List<RouteStopTimes> mRoutesStopTimes;


        public GetStopsRunnable( final List<RouteStopTimes> routesStopTimes ) {
            mRoutesStopTimes = routesStopTimes;
        }


        @Override
        public void run() {
            for( final RouteStopTimes routeTimes : mRoutesStopTimes ) {
                final LayoutInflater li = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE );
                final View stopRow = li.inflate( R.layout.stopscellview, null );
                stopRow.setOnTouchListener( new ShowRouteMapActivity( routeTimes ) );

                mStopTable.addView( stopRow, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                         ViewGroup.LayoutParams.WRAP_CONTENT ) );

                ((TextView) stopRow.findViewById( R.id.routenumber )).setText( routeTimes.getShortName() );
                ((TextView) stopRow.findViewById( R.id.routeName )).setText( routeTimes.getLongName() );

                SimpleDateFormat displayFormatter = new SimpleDateFormat( "h:mm", Locale.US );

                final Date currentDate = new Date();
                int counter = -1;
                long minDiff = 0;
                String departTime = "unknown";

                final List<StopTimePair> stopTimes = routeTimes.getStopTimes();
                for( int i = 0, s = stopTimes.size(); i < s; ++i ) {
                    final StopTimePair times = stopTimes.get( i );

                    final Date stopDate = times.getDeparture().forToday();
                    final long diff = stopDate.getTime() - currentDate.getTime();
                    if( diff > 0 && (minDiff <= 0 || minDiff > diff) ) {
                        minDiff = diff;
                        departTime = displayFormatter.format( stopDate );
                        counter = i;
                    }
                }

                if( minDiff != 0 ) {
                    ((TextView) stopRow.findViewById( R.id.time1 )).setText( departTime );
                    ((TextView) stopRow.findViewById( R.id.eta1 )).setText( minDiff / 60000 + " min" );
                }
                if( counter + 1 < stopTimes.size() || counter != -1 ) {
                    final Date nextStopDate = stopTimes.get( counter + 1 ).getDeparture().forToday();

                    ((TextView) stopRow.findViewById( R.id.time2 )).setText( displayFormatter.format( nextStopDate ) );
                    ((TextView) stopRow.findViewById( R.id.eta2 )).setText(
                            (nextStopDate.getTime() - currentDate.getTime()) / 60000 + " min" );

                    if( counter + 2 < stopTimes.size() ) {
                        Date nextNextStopDate = stopTimes.get( counter + 2 ).getDeparture().forToday();

                        ((TextView) stopRow.findViewById( R.id.time3 )).setText(
                                displayFormatter.format( nextNextStopDate ) );
                        ((TextView) stopRow.findViewById( R.id.eta3 )).setText(
                                (nextNextStopDate.getTime() - currentDate.getTime()) / 60000 + " min" );
                    }
                }
            }

            mProgressBar.setVisibility( View.INVISIBLE );
        }


        private class ShowRouteMapActivity
                implements OnTouchListener
        {
            private final RouteStopTimes mStopTimes;


            public ShowRouteMapActivity( final RouteStopTimes stopTimes ) {
                mStopTimes = stopTimes;
            }


            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                if( event.getAction() == MotionEvent.ACTION_DOWN ) {
                    Intent intent = new Intent( getActivity(), RouteMapActivity.class );
                    intent.putExtra( "ROUTE_NUMBER", mStopTimes.getShortName() );
                    intent.putExtra( "ROUTE_ID", mStopTimes.getRouteId().toString() );
                    startActivity( intent );
                }
                return false;
            }
        }
    }
}
