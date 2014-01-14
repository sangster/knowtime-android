package ca.knowtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;

import java.util.Arrays;
import java.util.List;

public class RoutePickerActivity
        extends Activity
{
    TableLayout routeSelectionTable;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_route_picker );
        routeSelectionTable = (TableLayout) findViewById( R.id.routePickerTable );
        Thread thread = new Thread( new Runnable()
        {
            @Override
            public void run() {
                getRoutes();
            }
        } );
        thread.start();
    }


    private void getRoutes() {
        final List<String> routes = WebApiService.getRoutes();
        runOnUiThread( new Runnable()
        {
            @Override
            public void run() {
                parseRoutes( routes );
            }
        } );
    }


    private void parseRoutes( List<String> routes ) {
        for( int i = 0; i < routes.size(); i += 6 ) {
            LayoutInflater li = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View routeRow = li.inflate( R.layout.routeselectorcell, null );
            routeSelectionTable.addView( routeRow, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                               ViewGroup.LayoutParams.WRAP_CONTENT ) );

            final List<Integer> buttons = Arrays.asList( R.id.selectorButton1, R.id.selectorButton2,
                                                         R.id.selectorButton3, R.id.selectorButton4,
                                                         R.id.selectorButton5, R.id.selectorButton6 );

            for( int j = 0, s = buttons.size(); j < s; ++j ) {
                setButtonVisible( routes, i + j, (Button) routeRow.findViewById( buttons.get( j ) ) );
            }
        }
    }


    private void setButtonVisible( final List<String> routes, final int index, final Button button ) {
        if( index < routes.size() ) {
            createButton( button, routes.get( index ) );
            button.setVisibility( View.VISIBLE );
        } else {
            button.setVisibility( View.INVISIBLE );
        }
    }


    private void createButton( Button stopButton, final String routeNumber ) {
        stopButton.setText( routeNumber );
        stopButton.setOnTouchListener( new OnTouchListener()
        {
            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                if( event.getAction() == MotionEvent.ACTION_UP ) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra( "routeNumber", routeNumber );
                    setResult( Activity.RESULT_OK, resultIntent );
                    finish();
                }
                return false;
            }
        } );
    }


    public void touchBackButton( View view ) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra( "routeNumber", "-1" );
        setResult( Activity.RESULT_OK, resultIntent );
        this.finish();
    }
}
