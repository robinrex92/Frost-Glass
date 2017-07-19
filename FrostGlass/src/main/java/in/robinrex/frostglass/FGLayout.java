package in.robinrex.frostglass;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by robin on 24/6/17.
 */

public class FGLayout extends FrameLayout implements Choreographer.FrameCallback {

    private boolean mLive = false;

    private boolean mEdgePaddingEnabled = false;

    private int mDownsampleFactor;

    private int mOverlayColor;

    private View mBlurredView;

    private int mBlurredViewWidth, mBlurredViewHeight;

    private boolean mDownsampleFactorChanged;

    private Bitmap mBitmapToBlur, mBlurredBitmap;

    private Canvas mBlurringCanvas;

    private int mBlurRadius = 1;

    private boolean mIsPaused = false
            ;

    public FGLayout(Context context) {
        this(context, null);
    }

    public FGLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        final int defaultBlurRadius = FrostGlass.DEFAULT_BLUR_RADIUS;
        final int defaultDownsampleFactor = FrostGlass.DEFAULT_DOWNSAMPLE_FACTOR;
        final int defaultOverlayColor = Color.TRANSPARENT;

        FrostEngine.init(getContext());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FGLayout);
        setBlurRadius(a.getInt(R.styleable.FGLayout_blurRadius, defaultBlurRadius));
        setFrostQuality(a.getInt(R.styleable.FGLayout_downSampleFactor,
                defaultDownsampleFactor));
        setOverlayColor(a.getColor(R.styleable.FGLayout_overlayColor, defaultOverlayColor));
        a.recycle();

    }

    public void frostWith(View blurredView) {
        mBlurredView = blurredView;
        invalidate();
    }

    public void setEdgePadding(boolean enabled) {
        mEdgePaddingEnabled = enabled;
        invalidate();
    }

    public void setBlurRadius(int radius) {
        mBlurRadius = radius;
        invalidate();
    }

    public void setDownsampleFactor(int factor) {
        mDownsampleFactor = factor;
        invalidate();
    }

    public void setFrostQuality(@IntRange(from = 1, to = 100) int factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Downsample factor must be in the range 1-100." +
                    "" + factor);
        }

        if (factor != mDownsampleFactor) {
            mDownsampleFactor = factor;
            mDownsampleFactorChanged = true;
        }
    }

    public void setOverlayColor(int color) {
        mOverlayColor = color;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (mBlurredView != null) {
            if (prepareToFrost()) {

                // If the background of the blurred view is a color drawable, we use it to clear
                // the blurring canvas, which ensures that edges of the child views are blurred
                // as well; otherwise we clear the blurring canvas with a transparent color.
                if (mBlurredView.getBackground() != null && mBlurredView.getBackground() instanceof ColorDrawable) {
                    mBitmapToBlur.eraseColor(((ColorDrawable) mBlurredView.getBackground()).getColor());
                } else {
                    mBitmapToBlur.eraseColor(Color.TRANSPARENT);
                }

                mBlurredView.draw(mBlurringCanvas);
                frost();

                canvas.save();
                canvas.translate(mBlurredView.getX() - getX(), mBlurredView.getY() - getY());
                canvas.scale(mDownsampleFactor, mDownsampleFactor);
                canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
                canvas.restore();
            }
            canvas.drawColor(mOverlayColor);
        }

        super.dispatchDraw(canvas);
    }

    /**
     * Enables or disables live mode on the Frosted view.
     *
     * @param enabled True, if live mode should be enabled. When enabled, the frosted content will be updated for
     *                each frame.
     * */
    public void setLiveMode(boolean enabled) {

        if (enabled) {
            Choreographer.getInstance().postFrameCallback(this);
            invalidate();
        } else {
            Choreographer.getInstance().removeFrameCallback(this);
        }

        mLive = enabled;
    }

    public boolean isLive() {
        return mLive;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // TODO: 25/6/17 Destroy the renderscript here if possible
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        invalidate();
        if (mLive)
            Choreographer.getInstance().postFrameCallback(this);
    }

    public boolean prepareToFrost() {
        final int width = mBlurredView.getWidth();
        final int height = mBlurredView.getHeight();

        if (mBlurringCanvas == null || mDownsampleFactorChanged
                || mBlurredViewWidth != width || mBlurredViewHeight != height) {
            mDownsampleFactorChanged = false;

            mBlurredViewWidth = width;
            mBlurredViewHeight = height;

            int scaledWidth = width / mDownsampleFactor;
            int scaledHeight = height / mDownsampleFactor;

            // The following manipulation is to avoid some RenderScript artifacts at the edge.
            if (mEdgePaddingEnabled) {
                scaledWidth = scaledWidth - scaledWidth % 4 + 4;
                scaledHeight = scaledHeight - scaledHeight % 4 + 4;
            }

            if (mBlurredBitmap == null
                    || mBlurredBitmap.getWidth() != scaledWidth
                    || mBlurredBitmap.getHeight() != scaledHeight) {
                mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight,
                        Bitmap.Config.ARGB_8888);
                if (mBitmapToBlur == null) {
                    return false;
                }

                mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight,
                        Bitmap.Config.ARGB_8888);
                if (mBlurredBitmap == null) {
                    return false;
                }
            }

            mBlurringCanvas = new Canvas(mBitmapToBlur);
            mBlurringCanvas.scale(1f / mDownsampleFactor, 1f / mDownsampleFactor);
        }
        return true;
    }

    private void frost() {
        mBlurredBitmap = FrostEngine.getInstance().frost(mBitmapToBlur, mBlurRadius);
    }

    public void pause() {
        this.mIsPaused = true;
        Choreographer.getInstance().removeFrameCallback(this);
    }

    public void resume() {
        this.mIsPaused = false;
        Choreographer.getInstance().postFrameCallback(this);
    }
}
