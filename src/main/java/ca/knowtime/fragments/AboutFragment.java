package ca.knowtime.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.knowtime.R;

public class AboutFragment
        extends Fragment
{
    @Override
    public View onCreateView( final LayoutInflater inflater, final ViewGroup container,
                              final Bundle savedInstanceState ) {
        final View view = inflater.inflate( R.layout.activity_about, container, false );

        view.findViewById( R.id.twitterButton ).setOnClickListener( new EmailOnClickListener() );

        return view;
    }


    private class EmailOnClickListener
            implements View.OnClickListener
    {
        @Override
        public void onClick( final View v ) {
            final Intent email = new Intent( Intent.ACTION_SEND );
            email.setType( "text/html" );
            email.putExtra( Intent.EXTRA_EMAIL, new String[]{ "feedback@knowtime.ca" } );

            getActivity().startActivity( Intent.createChooser( email, "Choose an Email client:" ) );
        }
    }
}
