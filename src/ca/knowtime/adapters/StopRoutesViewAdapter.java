package ca.knowtime.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ca.knowtime.R;
import ca.knowtime.comm.models.gtfs.Route;
import com.google.common.base.Preconditions;

import java.util.List;

import static ca.knowtime.ViewHolder.cacheView;

public class StopRoutesViewAdapter
        extends BaseAdapter
{
    private final LayoutInflater mInflater;
    private final List<Route> mRoutes;


    public StopRoutesViewAdapter( LayoutInflater inflater,
                                  final List<Route> routes ) {
        mInflater = Preconditions.checkNotNull( inflater );
        mRoutes = Preconditions.checkNotNull( routes );
    }


    @Override
    public int getCount() {
        return mRoutes.size();
    }


    @Override
    public Route getItem( final int position ) {
        return mRoutes.get( position );
    }


    @Override
    public long getItemId( final int position ) {
        return position;
    }


    @Override
    public View getView( final int position,
                         View view,
                         final ViewGroup parent ) {
        if( view == null ) {
            view = mInflater.inflate( R.layout.stops_route_item,
                                      parent,
                                      false );
        }
        final TextView shortView = cacheView( view,
                                              R.id.map_info_stops_route_item_short );
        final TextView longView = cacheView( view,
                                             R.id.map_info_stops_route_item_long );

        final Route route = getItem( position );
        shortView.setText( route.getShortName() );
        longView.setText( route.getLongName() );
        return view;
    }
}
