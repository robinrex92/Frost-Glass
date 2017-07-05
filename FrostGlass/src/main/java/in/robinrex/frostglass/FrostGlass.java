package in.robinrex.frostglass;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * This is the core Frost Glass component. This is not a view or a layout. This class can be used with any activity,
 * when you need to just apply the frost effect to  the entire activity.
 *
 * @author Robin Rex G.
 */
public class FrostGlass {

    /* Defaults */

    public static final int DEFAULT_DOWNSAMPLE_FACTOR = 8;

    public static final int DEFAULT_BLUR_RADIUS = 15;

    private static final int DEFAULT_FROSTING_DURATION = 0;

    private static final int DEFAULT_OVERLAY_COLOR = Color.TRANSPARENT;

    /* End of Defaults */

    private Activity mActivity;

    private FrostEngine mFrostEngine;

    private boolean mIsLiveFrostEnabled = false;

    private FGLayout mFrostView = null;

    private FrameLayout mActivityView;

    private Bitmap mFrostedBitmap;

    private int mDownsampleFactor = DEFAULT_DOWNSAMPLE_FACTOR;

    private int mBlurRadius = DEFAULT_BLUR_RADIUS;

    private int mFrostingDuration = DEFAULT_FROSTING_DURATION;

    private Paint mOverlayPaint;

    private boolean mIsFrostRequestPending = false;

    @ColorInt
    private int mOverlayColor = DEFAULT_OVERLAY_COLOR;

    private Canvas mFrostedBitmapCanvas;

    private FrostEngine.FrostMode mFrostMode = FrostEngine.FrostMode.ORIGINAL;

    private boolean mIsFrostingDefrostingInProcess = false;

    /**
     * The public constructor to instantiate this glass.
     *
     * @param context The activity that needs to be frosted.
     */
    public FrostGlass(Activity context) {

        FrostEngine.init(context);
        mActivity = context;
        mFrostEngine = FrostEngine.getInstance();

        mOverlayPaint = new Paint();

        mOverlayPaint.setColor(Color.TRANSPARENT);
    }

    /**
     * Method to set the overlay color, that will be applied on top of the frost view.
     *
     * @param color The color that will be overlaid, on top of the frost effect.
     */
    public void setOverlayColor(@ColorInt int color) {
        mOverlayColor = color;
        mOverlayPaint.setColor(mOverlayColor);
    }

    /**
     * Method to set the downsample factor, that will be used when creating bitmaps for the frost view. The higher
     * the value, the faster the frosting will be. But the quality of the frosting may be low for small values of
     * {@link #mBlurRadius}.
     */
    public void setDownsampleFactor(@IntRange(from = 1, to = 100) int downsampleFactor) {
        this.mDownsampleFactor = downsampleFactor;
    }

    /**
     * Method to set the frosting duration.
     *
     * @param duration The duration with which the frosting effect has to be applied. Frosting will be done gradually in
     *                 a linear fashion throughout the duration.
     */
    public void setFrostingDuration(int duration) {
        this.mFrostingDuration = duration;
    }

    /**
     * Applies a frosted glass effect to the activity this view is initialized with. This method takes a screen shot
     * of the activity and applies the frost effect to that screen shot. The frosted view is then added to the top of
     * the activity.
     * <p>
     * If the content of the activity changes after applying the frosting, the frost effect doesn't change. In other
     * words, once frosted, the screen will be a static blurred image with whatever content the activity had, when
     * this method was called.
     *
     * @param blurRadius The blur radius that will be passed to the frost engine. The higher the radius, the more the
     *                   frosting effect.
     */
    public void staticFrost(int blurRadius) {

        if (!canFrost())
            return;

        if (isFrosted() && isLive()) {
            frostImmediate(blurRadius, false);
            return;
        }

        frostScreen(blurRadius, false);
    }

    /**
     * Same as {@link #staticFrost(int)}, with a difference that the frosting effect created by this method, is LIVE.
     * Which means, even after the frost view is created and overlaid on to the activity view, the content of the
     * activity view will be tracked. If the original activity view changes, so will the frosted view.
     * <p>
     * However, this method uses more CPU power that {@link #staticFrost(int)} and should be used only when the
     * content behind the frost view should be tracked.
     *
     * @param blurRadius The blur radius that will be passed to the frost engine. The higher the radius, the more the
     *                   frosting effect.
     */
    public void liveFrost(int blurRadius) {

        if (!canFrost())
            return;

        if (isFrosted() && !isLive()) {
            frostImmediate(blurRadius, true);
            return;
        }

        frostScreen(blurRadius, true);
    }

    private void frostImmediate(int blurRadius, boolean isLive) {
        mFrostView.setAlpha(0);
        Bitmap bitmapToBlur = mFrostEngine.getBitmapForView(mActivityView, mDownsampleFactor);
        mFrostView.setAlpha(1);

        Bitmap blurredSourceBitmap = mFrostEngine.frost(bitmapToBlur, blurRadius);

        //Overlay drawing
        mFrostedBitmapCanvas = new Canvas(blurredSourceBitmap);
        mFrostedBitmapCanvas.drawPaint(mOverlayPaint);

        mFrostView.setBackground(new BitmapDrawable(mActivity.getResources(), blurredSourceBitmap));

        if (isLive) {
            Logger.info("Moving from static to live frosting");
            mIsLiveFrostEnabled = true;
        } else {
            Logger.info("Moving from live to static frosting");
        }

    }

    private void frostScreen(final int blurRadius, boolean isLive) {

        mBlurRadius = blurRadius;

        mIsLiveFrostEnabled = isLive;

        //Check if is view is laid out and is ready to be frosted.
        ViewObserver.observeView(mActivityView, new ViewObserver.ViewObserverCallback() {

            @Override
            public void onStartedObserving() {
                mIsFrostRequestPending = true;
            }

            @Override
            public void onViewReady() {
                frost();
                mIsFrostRequestPending = false;
            }
        });
    }

    /**
     * Defrosts, i.e removes the frosting effect applied to the activity view. Also removes the view added to the
     * activity to show the frosting effect. If no frosting is active, either static or live, this method doesn't
     * do anything.
     */
    public void defrostScreen() {
        if (!canDefrost())
            return;

        defrost();
    }

    private void frost() {

        //Check whether we already have added the frosted overlay view to the activity.
        mActivityView.setBackgroundColor(Color.BLACK);

        //Screen hasn't been frosted before.
        if (mFrostView == null) {
            mFrostView = new FGLayout(mActivity);
            mFrostView.setId(R.id.blur_view_id);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup

                            .LayoutParams.MATCH_PARENT);
            mFrostView.setLayoutParams(lp);
            mFrostView.setLiveMode(isLive());
            mFrostView.frostWith(mActivityView.getChildAt(0));
            mActivityView.addView(mFrostView);
        } else {

            Logger.info("Reusing existing Frost Glass.");

            //Screen has been frosted before, so hide the frost view before getting another screen shot.
            mFrostView.setAlpha(0);
        }

        FrostEngine.GradualFroster froster = new FrostEngine.GradualFroster(1, mBlurRadius) {

            @Override
            public void onFrostNextFrame(int currentFrostRadius) {

                FrostTimeTracker.start();

                mFrostView.setBlurRadius(currentFrostRadius);

                FrostTimeTracker.frameComplete();
            }

            @Override
            public void onFrostingComplete() {
                mIsFrostingDefrostingInProcess = false;

                FrostTimeTracker.printAverageTime();
            }

            @Override
            public void onFrostingStarted() {
                mIsFrostingDefrostingInProcess = true;
                FrostTimeTracker.reset();
            }
        };

        froster.setDuration(mFrostingDuration);

        froster.startFrosting();

    }

    private FrameLayout getContentView() {
        return (FrameLayout) mActivity.getWindow().findViewById(android.R.id.content);
    }

    private void defrostImmediate() {

        Logger.warn("Defrosting immediately.");

        if (mFrostView != null && mActivityView != null)
            mActivityView.removeView(mFrostView);

        mActivityView = null;

        mFrostView = null;
    }

    private void defrost() {

        FrostEngine.GradualFroster defroster = new FrostEngine.GradualFroster(mBlurRadius, 1) {

            @Override
            public void onFrostNextFrame(int currentFrostRadius) {
                mFrostView.setBlurRadius(currentFrostRadius);
            }

            @Override
            public void onFrostingComplete() {
                mIsFrostingDefrostingInProcess = false;

                //if live frost has been enabled, mark as disabled, and remove callbacks.
                mIsLiveFrostEnabled = false;

                mActivityView.removeView(mFrostView);

                mActivityView = null;

                mFrostView = null;
            }

            @Override
            public void onFrostingStarted() {
                mIsFrostingDefrostingInProcess = true;
            }
        };

        defroster.setDuration(mFrostingDuration);
        defroster.startFrosting();

    }

    private boolean canDefrost() {

        if (mIsFrostingDefrostingInProcess) {
            Logger.error("Frosting/Defrosting already in progress.");
            return false;
        }

        if (mActivityView == null) {
            Logger.info("Activity not frosted. Nothing to defrost.");
            return false;
        }

        return true;
    }

    private boolean canFrost() {

        if (mIsFrostingDefrostingInProcess) {
            Logger.error("Frosting/Defrosting already in progress.");
            return false;
        }

        if (mActivity == null) {
            Logger.debug("Activity is null. Cannot frost.");
            return false;
        }

        if (mIsFrostRequestPending) {
            Logger.error("A previous frost request is pending.");
            return false;
        }

        mActivityView = getContentView();

        if (mActivityView == null) {
            Logger.debug("Activity content view null.");
            return false;
        }

        return true;
    }

    /**
     * Method to check whether live frosting is active on the activity.
     *
     * @return Returns true if live frosting is enabled, false otherwise.
     */
    public boolean isLive() {
        return mIsLiveFrostEnabled;
    }

    /**
     * Method to check whether any type of frosting is applied on the activity. Either static or live.
     *
     * @return Returns true if either static or live frosting is currently applied, false otherwise.
     */
    public boolean isFrosted() {
        return (mFrostView != null);
    }

    /**
     * Resumes the live frosting if enabled. If live frosting is not enabled, calling this method doesn't do anything.
     */
    public void resumeFrosting() {
        if(mFrostView != null)
            mFrostView.resume();
        Logger.info(this, "Resumed frosting.");
    }

    /**
     * Pauses the live frosting if enabled. If live frosting is not enabled, calling this method doesn't do anything.
     */
    public void pauseFrosting() {
        Logger.info(this, "Paused frosting.");
        if(mFrostView != null)
            mFrostView.pause();
    }

    /**
     * Removes any frosting applied to the view, and destroys the frost engine.
     */
    public void destroy() {

        if (isFrosted()) {
            defrostImmediate();
        }

        mFrostEngine.shutdown();

    }
}
