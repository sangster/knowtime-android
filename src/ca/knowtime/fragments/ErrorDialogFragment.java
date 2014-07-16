package ca.knowtime.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class ErrorDialogFragment
        extends DialogFragment
{
    private Dialog mDialog;


    public ErrorDialogFragment() {
        super();
        mDialog = null;
    }


    public void setDialog( final Dialog dialog ) {
        mDialog = dialog;
    }


    @Override
    public Dialog onCreateDialog( final Bundle savedInstanceState ) {
        return mDialog;
    }
}
