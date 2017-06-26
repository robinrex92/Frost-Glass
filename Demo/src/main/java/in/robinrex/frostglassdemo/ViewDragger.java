package in.robinrex.frostglassdemo;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author ContusTeam <developers@contus.in>
 * @version 1.0
 */

public class ViewDragger implements View.OnTouchListener {

    private float dX, dY;

    private boolean mInvalidate;

    public static ViewDragger enableDragging(View view) {
        ViewDragger dragger = new ViewDragger();
        view.setOnTouchListener(dragger);

        return dragger;
    }

    public void setInavalidateOnDrag(boolean shouldInvalidate) {
        this.mInvalidate = shouldInvalidate;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        if(mInvalidate)
            view.invalidate();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                view.animate()
                        .x(event.getRawX() + dX)
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();
                break;
            default:
                return false;
        }
        return true;
    }
}
