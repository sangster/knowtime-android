package ca.knowtime.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import ca.knowtime.listeners.OnBlockableTouchEventListener;
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
        final Optional<OnBlockableTouchEventListener> opt = mOnTouchEventBlockedListener;
        final boolean block = opt.isPresent() && opt.get().onBlockableTouchEvent( ev );

        return !block && super.onInterceptTouchEvent( ev );
    }


    public Optional<OnBlockableTouchEventListener> getOnTouchEventBlockedListener() {
        return mOnTouchEventBlockedListener;
    }


    public void setOnTouchEventBlockedListener( final OnBlockableTouchEventListener onBlockableTouchEventListener ) {
        mOnTouchEventBlockedListener = Optional.fromNullable( onBlockableTouchEventListener );
    }
}
