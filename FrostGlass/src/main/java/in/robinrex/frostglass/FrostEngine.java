package in.robinrex.frostglass;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * This is the core class that handles Renderscript functions and bitmap allocations.
 *
 * @author Robin Rex G.
 */
public class FrostEngine {

    private static FrostEngine mFrostEngine;

    private RenderScript rs;

    private FrostEngine() {
        //Prevent object creation. Has to be accessed through FrostGlass.
    }

    public enum FrostMode {
        REFROST,
        ORIGINAL
    }

    /**
     * Initializes the Frost engine.
     */
    public static void init(Context context) {
        if (mFrostEngine != null) {
            return;
        }

        mFrostEngine = new FrostEngine();
        mFrostEngine.rs = RenderScript.create(context);
    }

    /**
     * Shuts down the Frost engine. And destroys the renderscript associated with it.
     */
    public void shutdown() {
        Logger.info("Shutting down Frost Engine");
        rs.destroy();
    }

    /**
     * Applies frosting effect to the given bitmap passed, with the given radius.
     *
     * @param src    The bitmap on which frosting has to be applied.
     * @param radius The radius of the frosting effect. (Blur radius).
     * @return The frosted bimap.
     */
    public Bitmap frost(Bitmap src, int radius) {

        final Allocation input = Allocation.createFromBitmap(rs, src);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script;
        script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(src);
        return src;
    }

    /**
     * Applies frosting effect the given view by extracting a bitmap from the view. Then frosting is done on the
     * bitmap  with the given radius.
     *
     * @param src    The source view on which the frosting effect has to be applied.
     * @param radius The radius of the frosting effect. (Blur radius).
     * @return The frosted bimap.
     */
    public Bitmap frost(View src, int radius) {
        Bitmap bitmap = getBitmapForView(src, 1f);
        return frost(bitmap, radius);
    }

    /**
     * Same as {@link #frost(View, int)} and {@link #frost(View, int)}, but with a downscale option. The
     * bitmap created for the passed in view, will be down scaled for faster processing.
     *
     * @param src             The source view to which frosting effect has to be applied.
     * @param radius          The radius of the frosting effect. (Blur radius).
     * @param downscaleFactor The factor with which thr bitmap must be downscaled before frosting.
     * @return The frosted bimap.
     */
    public Bitmap fastFrost(View src, int radius, float downscaleFactor) {
        Bitmap bitmap = getBitmapForView(src, downscaleFactor);
        return frost(bitmap, radius);
    }

    /**
     * This method tries to extract a bitmap from the given view.
     *
     * @param src             The view from which a bitmap has to be created.
     * @param downscaleFactor The factor with which the bitmap has to be down scaled.
     * @return The bitmap created from the view.
     */
    public Bitmap getBitmapForView(View src, float downscaleFactor) {

        int scaledWidth = (int) (src.getWidth() / downscaleFactor);
        int scaledHeight = (int) (src.getHeight() / downscaleFactor);

        //Edge artifact workaround.
        //scaledWidth = scaledWidth - scaledWidth % 4 + 4;
        //scaledHeight = scaledHeight - scaledHeight % 4 + 4;

        Bitmap bitmap = Bitmap.createBitmap(
                scaledWidth,
                scaledHeight,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        Matrix matrix = new Matrix();
        matrix.preScale(1f / downscaleFactor, 1f / downscaleFactor);
        canvas.setMatrix(matrix);
        src.draw(canvas);

        return bitmap;
    }

    /**
     * Returns the existing mFrostEngine of the Frost Engine. Throws IllegalStateException if the engine has not been
     * initialized before calling this method.
     *
     * @return An mFrostEngine of {@link FrostEngine}.
     */
    public static FrostEngine getInstance() {
        if (mFrostEngine == null) {
            throw new IllegalStateException("FrostEngine not initialized!");
        }

        return mFrostEngine;
    }

    public static abstract class GradualFroster {

        private int mEndRadius = 1;

        private int mStartRadius = 1;

        private ValueAnimator mValueAnimator;

        private long mFrostDuration = 0;

        public GradualFroster(int startRadius, int endRadius) {
            this.mStartRadius = startRadius;
            this.mEndRadius = endRadius;
        }

        public void setDuration(int frostingDurationMillis) {
            this.mFrostDuration = frostingDurationMillis;
        }

        public void startFrosting() {

            onFrostingStarted();

            mValueAnimator = ValueAnimator.ofInt(mStartRadius, mEndRadius);
            mValueAnimator.setDuration(mFrostDuration);

            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    onFrostNextFrame((int) animation.getAnimatedValue());
                }
            });

            mValueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    //Do nothing.
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    onFrostingComplete();

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

            mValueAnimator.start();
        }

        public abstract void onFrostNextFrame(int currentFrostRadius);

        public abstract void onFrostingComplete();

        public abstract void onFrostingStarted();

    }
}
