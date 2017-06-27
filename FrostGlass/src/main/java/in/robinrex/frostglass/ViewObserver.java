package in.robinrex.frostglass;

import android.view.View;
import android.view.ViewTreeObserver;

/**
 * @author ContusTeam <developers@contus.in>
 * @version 1.0
 */

public class ViewObserver {

    public static boolean isReady(View view) {
        return view != null && !(view.getWidth() == 0 && view.getHeight() == 0);
    }

    public interface ViewObserverCallback {

        void onStartedObserving();

        void onViewReady();
    }

    public static void observeView(final View view, final ViewObserverCallback callback) {

        if (callback == null)
            throw new IllegalStateException("Call back cannot be null");

        if (view == null)
            throw new IllegalStateException("View cannot be null");

        if (isReady(view)) {
            callback.onViewReady();
            return;
        }

        final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (view.getWidth() > 0 && view.getHeight() > 0) {

                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    callback.onViewReady();
                }
            }
        });
    }
}
