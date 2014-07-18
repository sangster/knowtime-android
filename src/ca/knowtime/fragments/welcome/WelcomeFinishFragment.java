package ca.knowtime.fragments.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ca.knowtime.R;
import ca.knowtime.activities.NearbyStopsActivity;

public class WelcomeFinishFragment
        extends Fragment
{


    @Override
    public View onCreateView( LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState ) {
        final View view = inflater.inflate( R.layout.welcome_finish_fragment,
                                            container,
                                            false );

        final Button finishButton = (Button) view.findViewById( R.id.welcome_finish_button );
        finishButton.setOnClickListener( new FinishButtonClickListener() );

        return view;
    }


    private class FinishButtonClickListener
            implements View.OnClickListener
    {
        @Override
        public void onClick( final View v ) {
            startActivity( new Intent( getActivity(),
                                       NearbyStopsActivity.class ) );
        }
    }
}
