package ca.knowtime;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
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


    private static class DataSetArrayAdapter
            extends BaseAdapter
    {
        private final Context mContext;
        private final List<DataSetSummary> mDataSets;


        private DataSetArrayAdapter( final Context context, final List<DataSetSummary> dataSets ) {
            mContext = context;
            mDataSets = dataSets;
        }


        @Override
        public int getCount() {
            return mDataSets.size();
        }


        @Override
        public DataSetSummary getItem( final int position ) {
            return mDataSets.get( position );
        }


        @Override
        public long getItemId( final int position ) {
            return position;
        }


        @Override
        public View getView( final int position, View view, final ViewGroup parent ) {
            if( view == null ) {
                view = LayoutInflater.from( mContext )
                                     .inflate( R.layout.data_set_chooser_item, parent, false );
            }
            final TextView nameView = ViewHolder.get( view, R.id.data_set_chooser_item_name );
            final TextView updatedView = ViewHolder.get( view,
                                                         R.id.data_set_chooser_item_last_updated );
            final TextView datesView = ViewHolder.get( view, R.id.data_set_chooser_item_dates );

            final DataSetSummary item = getItem( position );
            nameView.setText( item.getName() );
            updatedView.setText( item.getLastUpdated() );
            if( item.getStartDate().isPresent() ) {
                datesView.setText( String.format( "%s -> %s",
                                                  item.getStartDate().get(),
                                                  item.getEndDate().get() ) );
            } else {
                datesView.setText( "Unknown" );
            }

            return view;
        }
    }
}
