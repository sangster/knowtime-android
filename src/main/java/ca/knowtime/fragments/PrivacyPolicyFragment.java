package ca.knowtime.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.knowtime.R;

public class PrivacyPolicyFragment
        extends Fragment
{
    @Override
    public View onCreateView( final LayoutInflater inflater, final ViewGroup container,
                              final Bundle savedInstanceState ) {
        return inflater.inflate( R.layout.activity_privacypolicy, container, false );
    }
}
