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
        return inflater.inflate( R.layout.activity_about, container, false );
    }


    public void touchBackButton( View view ) {
        getActivity().finish();
    }


    public void touchTwitterButton( View view ) {

    }


    public void touchFacebookButton( View view ) {

    }


    public void touchFeedbackButton( View view ) {
        Intent email = new Intent( android.content.Intent.ACTION_SEND );
        email.setType( "text/html" );
        email.putExtra( Intent.EXTRA_EMAIL, new String[]{ "feedback@knowtime.ca" } );
        startActivity( Intent.createChooser( email, "Choose an Email client:" ) );
    }
}
