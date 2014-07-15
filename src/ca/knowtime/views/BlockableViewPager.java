package ca.knowtime.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import ca.knowtime.views.listeners.OnBlockableTouchEventListener;
import com.google.common.base.Optional;

public class BlockableViewPager
        extends ViewPager
{
    private Optional<OnBlockableTouchEventListener> mOnTouchEventBlockedListener;


    public BlockableViewPager( final Context context ) {
        super( context );
    }


    public BlockableViewPager( final Context context, final AttributeSet attrs ) {
        super( context, attrs );
    }


    @Override
    public boolean onInterceptTouchEvent( final MotionEvent ev ) {
        return mOnTouchEventBlockedListener.isPresent() && mOnTouchEventBlockedListener.get()
                                                                                       .onBlockableTouchEvent();
    }


    public Optional<OnBlockableTouchEventListener> getOnTouchEventBlockedListener() {
        return mOnTouchEventBlockedListener;
    }


    public void setOnTouchEventBlockedListener( final OnBlockableTouchEventListener onBlockableTouchEventListener ) {
        mOnTouchEventBlockedListener = Optional.fromNullable( onBlockableTouchEventListener );
    }
}
