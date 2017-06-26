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

    private final Activity mContext;

    FrostEngine mFrostEngine;

    private boolean mIsLiveFrostEnabled = false;

    private View mFrostView = null;

    private FrameLayout mActivityView;

    private Bitmap mFrostedBitmap;

    private int mDownsampleFactor = DEFAULT_DOWNSAMPLE_FACTOR;

    private int mBlurRadius = DEFAULT_BLUR_RADIUS;

    private int mFrostingDuration = DEFAULT_FROSTING_DURATION;

    private Paint mOverlayPaint = new Paint();

    @ColorInt
    private int mFrostOverlay = DEFAULT_OVERLAY_COLOR;

    private Canvas mFrostedBitmapCanvas;

    private FrostEngine.FrostMode mFrostMode = FrostEngine.FrostMode.ORIGINAL;

    public FrostGlass(Activity context) {

        FrostEngine.init(context);
        mContext = context;
        mFrostEngine = FrostEngine.getInstance();

        mOverlayPaint.setColor(Color.TRANSPARENT);

        Choreographer.getInstance().postFrameCallback(this);

    }

    public void setOverlayColor(@ColorInt int color) {
        mFrostOverlay = color;
        mOverlayPaint.setColor(mFrostOverlay);
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
        frostScreen(blurRadius, true);
    }

    private void frostScreen(final int blurRadius, boolean isLive) {

        mBlurRadius = blurRadius;

        mIsLiveFrostEnabled = isLive;

        if (mContext == null) {
            Logger.debug("Activity is null. Cannot frost.");
            return;
        }

        final FrameLayout view = getContentView();

        if (view == null) {
            Logger.debug("Activity content view null.");
            return;
        }

        final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(view.getWidth() > 0 && view.getHeight() > 0) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (mIsLiveFrostEnabled)
                        mActivityView = view;

                    final View v = view.findViewById(R.id.blur_view_id);

                    final View blurView;

                    //Screen hasn't been frosted before.
                    if (v == null) {
                        blurView = new View(mContext);
                        blurView.setId(R.id.blur_view_id);
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup

                                        .LayoutParams.MATCH_PARENT);
                        blurView.setLayoutParams(lp);
                    } else {
                        blurView = v;

                        //Screen has been frosted before, so hide the frost view before getting another screen shot.
                        blurView.setAlpha(0);
                    }

                    final Bitmap[] blurredContent = new Bitmap[1];
                    final Bitmap sourceBitmap = mFrostEngine.getBitmapForView(view, mDownsampleFactor);
                    ValueAnimator animator = ValueAnimator.ofInt(1, blurRadius);
                    animator.setDuration(mFrostingDuration);
                    final int[] averageFrameTime = new int[1];
                    final int[] framesRendered = new int[1];


                    // If the background of the blurred view is a color drawable, we use it to clear
                    // the blurring canvas, which ensures that edges of the child views are blurred
                    // as well; otherwise we clear the blurring canvas with a transparent color.
                    if (view.getBackground() != null && view.getBackground() instanceof ColorDrawable) {
                        sourceBitmap.eraseColor(((ColorDrawable) view.getBackground()).getColor());
                    } else {
                        sourceBitmap.eraseColor(Color.TRANSPARENT);
                    }

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {

                            long startTime = System.currentTimeMillis();
                            Bitmap bitmapToBlur;
                            if (!mIsLiveFrostEnabled) {
                                if(mFrostMode == FrostEngine.FrostMode.REFROST) {
                                    bitmapToBlur = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap
                                            .getHeight());
                                } else {
                                    blurView.setAlpha(0);
                                    bitmapToBlur = mFrostEngine.getBitmapForView(view, mDownsampleFactor);
                                    blurView.setAlpha(1);
                                }

                            } else {
                                blurView.setAlpha(0);
                                bitmapToBlur = mFrostEngine.getBitmapForView(view, mDownsampleFactor);
                                blurView.setAlpha(1);
                            }

                            blurredContent[0] = mFrostEngine.frost(bitmapToBlur, (int) animation.getAnimatedValue());

//                            mFrostedBitmapCanvas = new Canvas(blurredContent[0]);
//                            mFrostedBitmapCanvas.drawPaint(mOverlayPaint);

                            blurView.setBackground(new BitmapDrawable(mContext.getResources(), blurredContent[0]));
                            long frameTime = System.currentTimeMillis() - startTime;
                            Logger.debug("Frame drawn in : " + (frameTime) + " millis");
                            framesRendered[0]++;
                            averageFrameTime[0] = (int) (averageFrameTime[0] + frameTime);

                        }
                    });

                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            //Do nothing.
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            averageFrameTime[0] /= framesRendered[0];
                            Logger.info("======================================");
                            Logger.info("| Average Frame draw time : " + averageFrameTime[0]);
                            Logger.info("======================================");

                            if (mIsLiveFrostEnabled) {
                                Choreographer.getInstance().postFrameCallback(FrostGlass.this);
                            }

                            mFrostView = blurView;
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

                    if (v == null)
                        view.addView(blurView);
                    else
                        view.setAlpha(1);

                    Logger.debug("Animator started");
                    animator.start();
                }
            }
        });


    }

    private FrameLayout getContentView() {

        return (FrameLayout) mContext.getWindow().findViewById(android.R.id.content);
    }

    public void defrost() {
        final FrameLayout view = getContentView();

        if (mIsLiveFrostEnabled) {
            mActivityView = view;
        } else {
            Logger.info("Activity not frosted. Not defrosting.");
            return;
        }


        if (view == null) {
            Logger.debug("Activity content view null.");
            return;
        }

        final Bitmap[] blurredContent = new Bitmap[1];
        final Bitmap sourceBitmap = mFrostEngine.getBitmapForView(view, mDownsampleFactor);
        ValueAnimator animator = ValueAnimator.ofInt(mBlurRadius, 1);
        animator.setDuration(mFrostingDuration);
        final int[] averageFrameTime = new int[1];
        final int[] framesRendered = new int[1];

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                long startTime = System.currentTimeMillis();
                Bitmap bitmapToBlur;
                mFrostView.setAlpha(0);
                bitmapToBlur = mFrostEngine.getBitmapForView(view, mDownsampleFactor);
                mFrostView.setAlpha(1);

                blurredContent[0] = mFrostEngine.frost(bitmapToBlur, (int) animation.getAnimatedValue());

                mFrostView.setBackground(new BitmapDrawable(mContext.getResources(), blurredContent[0]));
                long frameTime = System.currentTimeMillis() - startTime;
                Logger.debug("Frame drawn in : " + (frameTime) + " millis");
                framesRendered[0]++;
                averageFrameTime[0] = (int) (averageFrameTime[0] + frameTime);

            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Do nothing.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                averageFrameTime[0] /= framesRendered[0];
                Logger.info("======================================");
                Logger.info("| Average Frame draw time : " + averageFrameTime[0]);
                Logger.info("======================================");

                mIsLiveFrostEnabled = false;
                Choreographer.getInstance().removeFrameCallback(FrostGlass.this);

                view.removeView(mFrostView);
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

        view.setAlpha(1);

        animator.start();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (mFrostView != null && mActivityView != null) {
            mFrostView.setAlpha(0);
            mFrostedBitmap = mFrostEngine.fastFrost(mActivityView, mBlurRadius, mDownsampleFactor);
            mFrostView.setAlpha(1);

            mFrostedBitmapCanvas = new Canvas(mFrostedBitmap);
            mFrostedBitmapCanvas.drawPaint(mOverlayPaint);

            mFrostView.setBackground(new BitmapDrawable(mActivityView.getResources(), mFrostedBitmap));
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    public boolean isLive() {
        return mIsLiveFrostEnabled;
    }

    public void resumeFrost() {
        Choreographer.getInstance().postFrameCallback(this);
    }

    public void pauseFrost() {
        Choreographer.getInstance().removeFrameCallback(this);
    }

    public void destroy() {
        mFrostEngine.shutdown();
    }
}
