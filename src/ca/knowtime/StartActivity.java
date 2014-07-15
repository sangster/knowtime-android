package ca.knowtime;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import ca.knowtime.adapters.DataSetArrayAdapter;
import ca.knowtime.comm.KnowTime;
import ca.knowtime.comm.KnowTimeAccess;
import ca.knowtime.comm.Response;
import ca.knowtime.comm.models.DataSetSummary;

import java.util.List;

import static ca.knowtime.Development.BASE_URL;

public class StartActivity
        extends Activity
{
    private KnowTimeAccess mAccess;
    private ListView mDataSetsView;


    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.frag_data_set_chooser );
        mDataSetsView = (ListView) findViewById( R.id.dataSetChooser );
        mAccess = KnowTime.connect( this, BASE_URL );
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAccess.dataSets( this, new Response<List<DataSetSummary>>()
        {
            @Override
            public void onResponse( final List<DataSetSummary> response ) {
                mDataSetsView.setAdapter( new DataSetArrayAdapter( StartActivity.this, response ) );
            }
        } );
    }


    @Override
    protected void onPause() {
        super.onPause();
        mAccess.cancel( this );
    }
}
