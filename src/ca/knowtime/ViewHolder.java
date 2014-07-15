package ca.knowtime;

import android.util.SparseArray;
import android.view.View;

public class ViewHolder
{
    @SuppressWarnings("unchecked")
    public static <T extends View> T get( View view, int id ) {
        final SparseArray<View> viewHolder = getViewHolder( view );

        View childView = viewHolder.get( id );
        if( childView == null ) {
            childView = view.findViewById( id );
            viewHolder.put( id, childView );
        }

        return (T) childView;
    }


    @SuppressWarnings("unchecked")
    private static SparseArray<View> getViewHolder( final View view ) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if( viewHolder == null ) {
            viewHolder = new SparseArray<>();
            view.setTag( viewHolder );
        }
        return viewHolder;
    }
}
