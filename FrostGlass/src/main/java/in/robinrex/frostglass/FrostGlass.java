package in.robinrex.frostglass;

import android.animation.Animator;
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
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * This is the core Frost Glass component.
 */
public class FrostGlass implements Choreographer.FrameCallback {

    public static final int DEFAULT_DOWNSAMPLE_FACTOR = 8;

    public static final int DEFAULT_BLUR_RADIUS = 15;

    private static final int DEFAULT_FROSTING_DURATION = 0;

    private static final int DEFAULT_OVERLAY_COLOR = Color.TRANSPARENT;

    private final Activity mActivity;

    private FrostEngine mFrostEngine;

    private boolean mIsLiveFrostEnabled = false;

    private View mFrostView = null;

    private FrameLayout mActivityView;

    private Bitmap mFrostedBitmap;

    private int mDownsampleFactor = DEFAULT_DOWNSAMPLE_FACTOR;

    private int mBlurRadius = DEFAULT_BLUR_RADIUS;

    private int mFrostingDuration = DEFAULT_FROSTING_DURATION;

    private Paint mOverlayPaint = new Paint();

    @ColorInt
    private int mOverlayColor = DEFAULT_OVERLAY_COLOR;

    private Canvas mFrostedBitmapCanvas;

    private FrostEngine.FrostMode mFrostMode = FrostEngine.FrostMode.ORIGINAL;

    private boolean mIsFrostingDefrostingInProcess = false;

    public FrostGlass(Activity context) {

        FrostEngine.init(context);
        mActivity = context;
        mFrostEngine = FrostEngine.getInstance();

        mOverlayPaint.setColor(Color.TRANSPARENT);

        Choreographer.getInstance().postFrameCallback(this);

    }

    public void setOverlayColor(@ColorInt int color) {
        mOverlayColor = color;
        mOverlayPaint.setColor(mOverlayColor);
    }

    public void setFrostQuality(@IntRange(from = 1, to = 100) int downsampleFactor) {
        this.mDownsampleFactor = downsampleFactor;
    }

    public void setFrostingDuration(int duration) {
        this.mFrostingDuration = duration;
    }

    public void staticFrost(int blurRadius) {
        frostScreen(blurRadius, false);
    }

    public void liveFrost(int blurRadius) {
        //// TODO: 26/6/17 Check if activity is frosted already. If yes skip the animaton
        frostScreen(blurRadius, true);
    }

    private void frostScreen(final int blurRadius, boolean isLive) {


        if(!canFrost())
            return;

        mBlurRadius = blurRadius;

        mIsLiveFrostEnabled = isLive;

        //View has not been laid out yet. Attach a layout listener.
        if(mActivityView.getWidth() == 0 && mActivityView.getHeight() == 0) {
            final ViewTreeObserver viewTreeObserver = mActivityView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    if(mActivityView.getWidth() > 0 && mActivityView.getHeight() > 0) {

                        mActivityView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        proceedWithFrosting();
                    }
                }
            });
        } else {
            proceedWithFrosting();
        }
    }

    private boolean canFrost() {

        if(mIsFrostingDefrostingInProcess){
            Logger.error("Frosting/Defrosting already in progress.");
            return false;
        }

        if (mActivity == null) {
            Logger.debug("Activity is null. Cannot frost.");
            return false;
        }

        mActivityView = getContentView();

        if (mActivityView == null) {
            Logger.debug("Activity content view null.");
            return false;
        }

        return true;
    }

    private void proceedWithFrosting() {

        //Check whether we already have added the frosted overlay view to the activity.
        final View prevFrostView = mActivityView.findViewById(R.id.blur_view_id);

        final View frostOverlay;

        //Screen hasn't been frosted before.
        if (prevFrostView == null) {
            frostOverlay = new View(mActivity);
            frostOverlay.setId(R.id.blur_view_id);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup

                            .LayoutParams.MATCH_PARENT);
            frostOverlay.setLayoutParams(lp);
        } else {
            frostOverlay = prevFrostView;

            //Screen has been frosted before, so hide the frost view before getting another screen shot.
            frostOverlay.setAlpha(0);
        }

        final Bitmap[] blurredContent = new Bitmap[1];
        final Bitmap sourceBitmap = mFrostEngine.getBitmapForView(mActivityView, mDownsampleFactor);

        //frostGradually();

        ValueAnimator frostAnimator = ValueAnimator.ofInt(1, mBlurRadius);
        frostAnimator.setDuration(mFrostingDuration);

        // If the background of the blurred view is a color drawable, we use it to clear
        // the blurring canvas, which ensures that edges of the child views are blurred
        // as well; otherwise we clear the blurring canvas with a transparent color.
        if (mActivityView.getBackground() != null && mActivityView.getBackground() instanceof ColorDrawable) {
            sourceBitmap.eraseColor(((ColorDrawable) mActivityView.getBackground()).getColor());
        } else {
            sourceBitmap.eraseColor(Color.TRANSPARENT);
        }

        frostAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                FrostTimeTracker.start();

                Bitmap bitmapToBlur;

                // TODO: 26/6/17 Check frost mode implementation
                if (!mIsLiveFrostEnabled) {
                    if(mFrostMode == FrostEngine.FrostMode.REFROST) {
                        bitmapToBlur = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap
                                .getHeight());
                    } else {
                        frostOverlay.setAlpha(0);
                        bitmapToBlur = mFrostEngine.getBitmapForView(mActivityView, mDownsampleFactor);
                        frostOverlay.setAlpha(1);
                    }

                } else {
                    frostOverlay.setAlpha(0);
                    bitmapToBlur = mFrostEngine.getBitmapForView(mActivityView, mDownsampleFactor);
                    frostOverlay.setAlpha(1);
                }

                blurredContent[0] = mFrostEngine.frost(bitmapToBlur, (int) animation.getAnimatedValue());

                //Overlay drawing
                //mFrostedBitmapCanvas = new Canvas(blurredContent[0]);
                //mFrostedBitmapCanvas.drawPaint(mOverlayPaint);

                frostOverlay.setBackground(new BitmapDrawable(mActivity.getResources(), blurredContent[0]));

                FrostTimeTracker.frameComplete();

            }
        });

        frostAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Do nothing.
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                mIsFrostingDefrostingInProcess = false;

                FrostTimeTracker.printAverageTime();
                FrostTimeTracker.reset();

                if (mIsLiveFrostEnabled) {
                    Logger.debug("Enabling live frosting");
                    Choreographer.getInstance().postFrameCallback(FrostGlass.this);
                }

                mFrostView = frostOverlay;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //Do nothing
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //Do nothing
            }
        });

        if (prevFrostView == null)
            mActivityView.addView(frostOverlay);

        Logger.debug("Animator started");
        frostAnimator.start();

        mIsFrostingDefrostingInProcess = true;
    }

    private FrameLayout getContentView() {
        return (FrameLayout) mActivity.getWindow().findViewById(android.R.id.content);
    }

    public void defrost() {

        if(!canDefrost()) {
            return;
        }

        final Bitmap[] blurredContent = new Bitmap[1];

        ValueAnimator defrostAnimator = ValueAnimator.ofInt(mBlurRadius, 1);
        defrostAnimator.setDuration(mFrostingDuration);

        defrostAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                Bitmap bitmapToBlur;

                mFrostView.setAlpha(0);
                bitmapToBlur = mFrostEngine.getBitmapForView(mActivityView, mDownsampleFactor);
                mFrostView.setAlpha(1);

                blurredContent[0] = mFrostEngine.frost(bitmapToBlur, (int) animation.getAnimatedValue());

                mFrostView.setBackground(new BitmapDrawable(mActivity.getResources(), blurredContent[0]));
            }
        });

        defrostAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Do nothing.
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                mIsFrostingDefrostingInProcess = false;

                //if live frost has been enabled, mark as disabled, and remove callbacks.
                mIsLiveFrostEnabled = false;
                Choreographer.getInstance().removeFrameCallback(FrostGlass.this);

                mActivityView.removeView(mFrostView);

                mActivityView = null;

                mFrostView = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //Do nothing
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //Do nothing
            }
        });

        defrostAnimator.start();

        mIsFrostingDefrostingInProcess = true;
    }

    private boolean canDefrost() {

        if(mIsFrostingDefrostingInProcess){
            Logger.error("Frosting/Defrosting already in progress.");
            return false;
        }

        if(mActivityView == null) {
            Logger.info("Activity not frosted. Not defrosting.");
            return false;
        }

        return true;
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (mFrostView != null && mActivityView != null) {
            mFrostView.setAlpha(0);
            mFrostedBitmap = mFrostEngine.fastFrost(mActivityView, mBlurRadius, mDownsampleFactor);
            mFrostView.setAlpha(1);

            //Overlay paint
            //mFrostedBitmapCanvas = new Canvas(mFrostedBitmap);
            //mFrostedBitmapCanvas.drawPaint(mOverlayPaint);

            mFrostView.setBackground(new BitmapDrawable(mActivityView.getResources(), mFrostedBitmap));

            //Re-post the callback for next frame.
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    public boolean isLive() {
        return mIsLiveFrostEnabled;
    }

    public boolean isFrosted() {
        return (mActivityView != null);
    }

    public void resumeFrosting() {
        Choreographer.getInstance().postFrameCallback(this);
    }

    public void pauseFrosting() {
        Choreographer.getInstance().removeFrameCallback(this);
    }

    public void destroy() {
        mFrostEngine.shutdown();
    }
}
