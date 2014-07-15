package ca.knowtime.fragments.welcome;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.knowtime.R;

public class WelcomeStartFragment
        extends Fragment
{
    @Override
    public View onCreateView( LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState ) {
        return inflater.inflate( R.layout.welcome_start_fragment, container, false );
    }
}
