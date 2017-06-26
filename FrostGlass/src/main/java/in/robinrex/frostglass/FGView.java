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

/**
 * A custom view for presenting a dynamically blurred version of another view's content.
 * <p/>
 * Use {@link #frostWith(View)} to set up the reference to the view to be blurred.
 * After that, call {@link #invalidate()} to trigger blurring whenever necessary.
 */
public class FGView extends View implements Choreographer.FrameCallback {

    private boolean mLive = false;

    private boolean mEdgePaddingEnabled = false;

    public FGView(Context context) {
        this(context, null);
    }

    public FGView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final int defaultBlurRadius = FrostGlass.DEFAULT_BLUR_RADIUS;
        final int defaultDownsampleFactor = FrostGlass.DEFAULT_DOWNSAMPLE_FACTOR;
        final int defaultOverlayColor = Color.TRANSPARENT;

        FrostEngine.init(getContext());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FGView);
        setBlurRadius(a.getInt(R.styleable.FGView_blurRadius, defaultBlurRadius));
        setDownsampleFactor(a.getInt(R.styleable.FGView_downSampleFactor,
                defaultDownsampleFactor));
        setOverlayColor(a.getColor(R.styleable.FGView_overlayColor, defaultOverlayColor));
        a.recycle();

    }

    public void frostWith(View blurredView) {
        mBlurredView = blurredView;
    }

    public void setEdgePadding(boolean enabled) {
        mEdgePaddingEnabled = enabled;
    }

    public void setBlurRadius(int radius) {
        mBlurRadius = radius;
    }

    public void setDownsampleFactor(@IntRange(from = 1, to = 100) int factor) {
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

    public void enableLiveMode() {
        setLiveMode(true);
    }

    public void disableLiveMode() {
        setLiveMode(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBlurredView != null) {
            if (prepare()) {

                // If the background of the blurred view is a color drawable, we use it to clear
                // the blurring canvas, which ensures that edges of the child views are blurred
                // as well; otherwise we clear the blurring canvas with a transparent color.
                if (mBlurredView.getBackground() != null && mBlurredView.getBackground() instanceof ColorDrawable) {
                    mBitmapToBlur.eraseColor(((ColorDrawable) mBlurredView.getBackground()).getColor());
                } else {
                    mBitmapToBlur.eraseColor(Color.TRANSPARENT);
                }

                mBlurredView.draw(mBlurringCanvas);
                blur();

                canvas.save();
                canvas.translate(mBlurredView.getX() - getX(), mBlurredView.getY() - getY());
                canvas.scale(mDownsampleFactor, mDownsampleFactor);
                canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
                canvas.restore();
            }
            canvas.drawColor(mOverlayColor);
        }
    }

    private void setLiveMode(boolean enabled) {

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

    protected boolean prepare() {
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

    protected void blur() {
        mBlurredBitmap = FrostEngine.getInstance().frost(mBitmapToBlur, mBlurRadius);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // TODO: 25/6/17 Destroy renderscript if possible, here
    }

    private int mDownsampleFactor;

    private int mOverlayColor;

    private View mBlurredView;

    private int mBlurredViewWidth, mBlurredViewHeight;

    private boolean mDownsampleFactorChanged;

    private Bitmap mBitmapToBlur, mBlurredBitmap;

    private Canvas mBlurringCanvas;

    private int mBlurRadius = 1;

    @Override
    public void doFrame(long frameTimeNanos) {
        invalidate();
        if (mLive)
            Choreographer.getInstance().postFrameCallback(this);
    }
}