package ca.knowtime.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ca.knowtime.R;
import ca.knowtime.ViewHolder;
import ca.knowtime.comm.models.DataSetSummary;
import com.google.common.base.Optional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DataSetArrayAdapter
        extends BaseAdapter
{
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
    private final Context mContext;
    private final List<DataSetSummary> mDataSets;


    public DataSetArrayAdapter( final Context context, final List<DataSetSummary> dataSets ) {
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
        final TextView titleView = ViewHolder.get( view, R.id.data_set_chooser_item_title );
        final TextView datesView = ViewHolder.get( view, R.id.data_set_chooser_item_dates );

        final DataSetSummary item = getItem( position );
        titleView.setText( item.getTitle() );
        datesView.setText( getDatesString( item ) );

        return view;
    }


    private String getDatesString( final DataSetSummary item ) {
        final Optional<String> start = item.getStartDate();
        final Optional<String> end = item.getEndDate();

        return mContext.getString( R.string.data_set_valid_until, formatDate( end ) );
    }


    private String formatDate( final Optional<String> end ) {
        try {
            final Date date = DATE_FORMAT.parse( end.get() );
            return new SimpleDateFormat( "MMM d" ).format( date );
        } catch( ParseException e ) {
            Log.e( "JON", "parse error[" + e.getMessage() + "]", e );
            return "";
        }
    }
}
