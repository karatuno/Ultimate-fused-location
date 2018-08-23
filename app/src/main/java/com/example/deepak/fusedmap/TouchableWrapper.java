package com.example.deepak.fusedmap;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;


/**
 * Created by saini on 24-03-2018.
 */

public class TouchableWrapper extends FrameLayout {
    private UpdateMapAfterUserInterection updateMapAfterUserInterection;

    public TouchableWrapper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public void setListener(UpdateMapAfterUserInterection listener) {
        updateMapAfterUserInterection = listener;
    }

    Point touchPoint = new Point();
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if(ev.getPointerCount()<2) {
                    final Point newTouchPoint = new Point();  // the new position of user's finger on screen after movement is detected
                    newTouchPoint.x = (int) ev.getX();
                    newTouchPoint.y = (int) ev.getY();
                    updateMapAfterUserInterection.onUpdateMapAfterUserInterection(touchPoint,newTouchPoint);
                    touchPoint = newTouchPoint;
                    Log.i("touchpoint", String.valueOf(touchPoint));
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.i("","up");
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // Map Activity must implement this interface
    public interface UpdateMapAfterUserInterection {
        public void onUpdateMapAfterUserInterection(Point touchpoint, Point newTouchpoint);
    }


}
