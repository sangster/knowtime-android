package ca.knowtime.listeners;

import android.view.MotionEvent;

public interface OnBlockableTouchEventListener
{
    boolean onBlockableTouchEvent( final MotionEvent ev );
}
